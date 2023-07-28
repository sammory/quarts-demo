# Quartz에서 jar 패키지의 실행을 예약합니다.Demo

#### 1.Quartz 소개

​	Quartz 프레임워크의 핵심은 스케줄러입니다.스케줄러는 Quartz 애플리케이션 런타임 환경을 관리합니다.스케줄러는 스스로 모든 작업을 수행하는 것이 아니라 프레임워크 내에서 매우 중요한 부품에 의존합니다.Quartz는 스레드와 스레드 관리만 하는 것이 아닙니다.확장성을 보장하기 위해 Quartz는 다중 스레드 기반 아키텍처를 사용합니다.시작할 때 프레임은 스케줄러가 예약된 작업을 수행하는 데 사용하는 워크러 스레드 세트를 초기화합니다.쿼츠가 여러 작업을 동시에 실행할 수 있는 원리다.Quartz는 스레드 환경을 관리하기 위해 느슨하게 결합된 스레드 풀 관리 위젯 세트에 의존합니다.

####  2.프로젝트 관련

​	이 타이머 Demo는 정해진 경로에서 jar 패킷의 컴파일을 정기적으로 실행하기 위해 사용되며 일반적인 작업 스케줄에도 사용될 수 있다.전체 목록 파일은 작업에 대한 쿼리를 수정하여 삭제함으로써 관리됩니다.jar의 시작 실행 상태를 변경하기 위해 스위치를 켜고 끌 수 있다(켜고 나서 정지할 필요가 있으면 서버를 종료하거나 재시작해야 효력이 발생하며, 구체적인 해결책은 아직 개선 중이다).관련 UI 인터페이스 세트는 비교적 간략하여 2차 개발이 가능하며, 주로 JPA 페이지 쿼리를 통합하여 관련 코드 부분을 볼 수 있습니다.

![test.gif](https://upload-images.jianshu.io/upload_images/12057079-45628ea79747f5e4.gif?imageMogr2/auto-orient/strip)

##### 3.테스트 경로

```java
//홈페이지 경로:
http://localhost:9090/
//jar를 작동시키는 경로 (여기 있는 나는 간단한 springboot의 helloword의 Jar 패키지를 만들었을 뿐이며, 관련 jar 패키지는 프로젝트에서 찾을 수 있습니다)
http://localhost:8088/sayHello
```

##### 4.코드관련

##### QuartzServiceImpl:

```java
@Service
public class QuartzServiceImpl implements QuartzService {

    @Autowired
    private JobEntityRepository repository;

    @Override
    public JobEntity getById(int id) {
        return repository.getById(id);
    }

    //Id로 Job 가져오기
    public JobEntity getJobEntityById(Integer id) {
        return repository.getById(id);
    }
    // 데이터베이스에서 모든 Job을 불러옵니다
    public List<JobEntity> loadJobs() {
        List<JobEntity> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }
    // JobDataMap을 가져옵니다.(Job 매개 변수 개체)
    public JobDataMap getJobDataMap(JobEntity job) {
        JobDataMap map = new JobDataMap();
        map.put("name", job.getName());
        map.put("group", job.getGroup());
        map.put("cronExpression", job.getCron());
        map.put("parameter", job.getParameter());
        map.put("JobDescription", job.getDescription());
        map.put("vmParam", job.getVmParam());
        map.put("jarPath", job.getJarPath());
        map.put("status", job.getStatus());
        return map;
    }
    // JobDetail을 얻으면, JobDetail은 작업의 정의이고, Job은 작업의 실행 논리이며, JobDetail에서는 Job Class를 인용하여 정의한다.
    public JobDetail geJobDetail(JobKey jobKey, String description, JobDataMap map) {
        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobKey)
                .withDescription(description)
                .setJobData(map)
                .storeDurably()
                .build();
    }
    // Trigger 가져오기 (Job의 트리거, 실행 규칙)
    public Trigger getTrigger(JobEntity job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getName(), job.getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()))
                .build();
    }
    // JobKey 가져오기Name과 Group 포함
    public JobKey getJobKey(JobEntity job) {
        return JobKey.jobKey(job.getName(), job.getGroup());
    }
}
```

##### DynamicJob:

```java
/**
 * :@DisallowConcurrentExecution:이 태그는 Job을 구현하는 클래스에 사용되며 동시 실행은 허용되지 않습니다.
 * :org.quartz.threadPool.threadCount 스레드 풀의 스레드 수는 적어도 여러 개여야 합니다. 그렇지 않으면 @DisallowConcurrentExecution이 적용되지 않습니다.
 * :만약 Job의 설정 시간 간격이 3초이지만 Job 실행 시간이 5초라면, @DisallowConcurrentExecution을 설정한 후 프로그램이 작업 수행이 완료된 후에 실행되며, 그렇지 않으면 3초에 새로운 스레드를 다시 실행할 수 있습니다.
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution //이상 없이 데이터 업데이트 가능?
@Component
@Slf4j
public class DynamicJob implements Job {
    private Logger logger = LoggerFactory.getLogger(DynamicJob.class);

    /**
     * 핵심 접근 방식,Quartz Job의 진정한 실행 논리
     *   executorContext JobExecutionContext에 Quartz 실행에 필요한 모든 정보가 들어 있습니다.
     * @throws JobExecutionException execute() 메서드는 JobExecutionException 예외만 던질 수 있습니다.
     */
    @Override
    public void execute(JobExecutionContext executionContext) throws JobExecutionException {

        JobDataMap map = executionContext.getMergedJobDataMap();

        String jarPath = map.getString("jarPath");
        String parameter = map.getString("parameter");
        String vmParam = map.getString("vmParam");
        logger.info("Running Job name : {} ", map.getString("name"));
        logger.info("Running Job description : " + map.getString("JobDescription"));
        logger.info("Running Job group: {} ", map.getString("group"));
        logger.info("Running Job cron : " + map.getString("cronExpression"));
        logger.info("Running Job jar path : {} ", jarPath);
        logger.info("Running Job parameter : {} ", parameter);
        logger.info("Running Job vmParam : {} ", vmParam);

        long startTime = System.currentTimeMillis();
        if (!StringUtils.getStringUtil.isEmpty(jarPath)) {

            File jar = new File(jarPath);
            if (jar.exists()) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(jar.getParentFile());
                /**
                 * 이것은 자바의 실행 명령이다
                 * java -jar
                 */
                List<String> commands = new ArrayList<>();
                commands.add("java");
                if (!StringUtils.getStringUtil.isEmpty(vmParam)) {
                    commands.add(vmParam);
                }
                commands.add("-jar");
                commands.add(jarPath);
                commands.add(" &");
                System.out.println("commands->\n"+commands);

                if (!StringUtils.getStringUtil.isEmpty(parameter)) {

                    commands.add(parameter);
                    processBuilder.command(commands);
                    logger.info("Running Job details as follows >>>>>>>>>>>>>>>>>>>>: ");
                    logger.info("Running Job commands : {}  ", StringUtils.getStringUtil.getListString(commands));
                    try {
                        Process process = processBuilder.start();
                        logProcess(process.getInputStream(), process.getErrorStream());

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                } else {

                    throw new JobExecutionException("Job Jar not found >>  " + jarPath);
                }

                long endTime = System.currentTimeMillis();
                logger.info(">>>>>>>>>>>>> Running Job has been completed , cost time :  " + (endTime - startTime) + "ms\n");
            }


        }


    }

    //Job 실행 로그 출력
    private void logProcess(InputStream inputStream, InputStream errorStream) throws IOException {
        String inputLine;
        String errorLine;
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        while ((inputLine = inputReader.readLine()) != null){
            logger.info(inputLine);
        }
        while ((errorLine = errorReader.readLine()) != null) {
            logger.error(errorLine);
        }
    }

}

```

