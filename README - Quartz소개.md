# Quarts관련 내용

```java
문서 참조 주소:
https://www.w3cschool.cn/quartz_doc/quartz_doc-1xbu2clr.html
```

###  제1장 Quarts의 Demo

#### 1.의존성 삽입

주의: 여기서 인용하는 것은 버전의 문제에 주의해야 하는데, 만약 너무 낮은 버전을 이용한다면<br> 
        예를 들면: 1.8과 같이 어떤 부류는 찾을 수 없을 것이다.
    

```java
<!-- https://mvnrepository.com/artifact/org.quartz-scheduler/quartz -->
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
            <version>2.3.0</version>
        </dependency>
```

#### 2.관련 클래스

\HelloJob

```java
//구체적인 job의 내용
public class HelloJob   implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        System.out.println("job1실행");

    }
}

```

\QuartzTest

```java
public class QuartzTest {

    public static void main(String[] args) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        // 우리가 정의한 HelloJob 을 사용하여 관련된 호출
        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .withIdentity("job1", "group1")
                .build();

        //트리거 설정

        /**
         * 연결된 트리거 설정
         */
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(
                        SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(2)
                                .repeatForever()
                ).build();

        // 마지막으로 저희 Job 이 트리거 사양에 맞게 작동하도록 하겠습니다.
        scheduler.scheduleJob(job, trigger);
        // scheduler.shutdown();
    }
}

```

### 제2장 Quartz API

Quartz API키 인터페이스：

- Scheduler - 스케줄러와 상호 작용하는 API。
- Job - 스케줄러에서 실행하려는 구성요소에 의해 구현 되는 인터페이스。
- JobDetail - 작업을 정의하는 데 사용되는 예제。
- Trigger（트리거） - 주어진 작업을 실행할 계획을 정의하는 구성 요소。
- JobBuilder - JobDetail 인스턴스를 정의/구축하는 데 사용되며 작업을 정의하는 데 사용됩니다。
- TriggerBuilder - 트리거 인스턴스를 정의/ 빌드하는 데 사용。

Scheduler의 수명은 SchedulerFactory가 그것을 만들 때 시작하여 Scheduler가 shutdown() 메서드를 호출할 때 종료됩니다. Scheduler가 만들어진 후에는 Job과 Trigger를 추가, 삭제 및 열거하고 스케줄과 관련된 다른 작업(예: Trigger 일시 중단)을 수행할 수 있습니다.그러나 Scheduler는 start() 메서드를 호출한 후에만 trigger를 트리거합니다(즉, job 실행).。

쿼츠가 제공하는 빌더 클래스는 일종의 영역 특정 언어(DSL, Domain Specific Language)로 볼 수 있다.튜토리얼 1에 관련 예시가 있습니다. 여기 코드 조각이 있습니다. (교정 참고: 이런 캐스케이드 API는 사용자가 사용하기 매우 편리합니다. 여러분은 나중에 외부 인터페이스를 쓸 때도 이런 방식을 사용할 수 있습니다)

```java
execute() 방법은 Job의 trigger가 트리거되면 스케줄러의 작업 쓰레드에 의해 호출됩니다.execute() 메서드에 전달된 JobExecutionContext 객체는 작업 인스턴스에 "실행 중" 링 job에 대한 트리거를 제공한 후(나중에 설명될 수 있음), execute() 메서드는 scheduler의 작업 스레드에 호출됩니다. execute() 메서드에 전달된 JobExecutionContext 객체는 job이 실행될 때 몇 가지 정보를 저장합니다. job의 scheduler 참조, job의 trigger 참조를 트리거합니다.

        JobDetail 객체는 job을 scheduler에 가입할 때 클라이언트 프로그램(당신의 프로그램)에 의해 만들어집니다.여기에는 잡의 다양한 속성 설정과 잡 인스턴스 상태 정보를 저장하는 데 사용되는 JobDataMap이 포함됩니다.이 섹션은 Job 인스턴스에 대한 간략한 소개이며 자세한 내용은 다음 섹션에서 설명합니다.

        Trigger는 Job의 실행을 트리거하는 데 사용됩니다.잡(job)을 예약하려고 할 때 Trigger의 인스턴스를 만든 다음 예약 관련 속성을 설정합니다.Trigger에는 Job에 트리거 관련 매개변수를 전달하는 데 사용되는 관련 Job DataMap도 있습니다.Quartz는 다양한 유형의 Trigger를 가지고 있으며 가장 일반적으로 사용되는 것은 SimpleTrigger와 CronTrigger입니다.

        SimpleTrigger는 주로 한 번에 실행되는 Job(특정 시점에 한 번만 실행) 또는 특정 시점에 Job을 실행하고 N회 반복하며 매번 실행 간격 T개의 시간 단위로 수행됩니다.CronTrigger는 '매주 금요일 정오' 또는 '매월 10일 오전 10:15'와 같은 달력 기반 스케줄링에 매우 유용합니다.

        왜 Job도 있고 Trigger도 있는거죠?많은 작업 스케줄러는 Job과 Trigger를 구별하지 않습니다.일부 스케줄러는 단순히 실행 시간과 일부 job 식별자를 통해 Job을 정의하는 반면, 다른 스케줄러는 Quartz의 Job과 Trigger 개체를 통합합니다.Quartz를 개발할 때 우리는 스케줄링할 작업을 분리하는 것이 합리적이라고 생각합니다.우리가 보기에 이것은 많은 이점을 가져올 수 있습니다.

        예를 들어, Job이 생성된 후 Scheduler에 보관할 수 있으며 Trigger와 독립적이며 동일한 Job에 여러 Trigger가 있을 수 있으며, 이러한 느슨한 결합의 또 다른 이점은 Scheduler의 Job과 관련된 trigger가 모두 만료되었을 때 Job을 나중에 재스케쥴링하도록 구성할 수 있고, 또한 관련 Job을 재정의하지 않고 트리거를 수정하거나 교체할 수 있다는 것입니다.。
```

### 제3장 Job과 JobDetail 소개

우리는 scheduler에게 JobDetail 인스턴스를 전달했습니다. 왜냐하면 우리가 JobDetail을 만들 때 실행할 job의 클래스 이름이 JobDetail로 전달되었기 때문에 scheduler는 어떤 종류의 job을 수행해야 하는지 알았기 때문입니다. scheduler가 job을 실행할 때마다 execute(...)를 호출합니다.) 메서드 이전에 이 클래스의 새로운 인스턴스를 만듭니다. 실행이 완료되면 인스턴스에 대한 참조가 버려지고 인스턴스가 스팸으로 재활용됩니다. 이 실행 정책의 한 가지 결과는 job에 무참조 생성자가 있어야 한다는 것입니다(기본 JobFactory를 사용할 때). 또 다른 결과는 job 클래스에서 상태 있는 데이터 속성이 정의되지 않아야 한다는 것입니다. 왜냐하면 job의 여러 실행 중에 이러한 속성의 값이 유지되지 않기 때문입니다.

그렇다면 job 인스턴스에 속성이나 설정을 어떻게 추가할 수 있을까요? 어떻게 잡의 여러 실행 중에 잡의 상태를 추적할 수 있습니까?<br> 정답은 JobDataMap, JobDetail 대상의 일부입니다.。

#### JobDataMap

JobDataMap에는 무제한(시퀀스화된) 데이터 객체를 포함할 수 있으며, Job 인스턴스가 실행될 때 데이터를 사용할 수 있습니다. JobDataMap은 자바 Map 인터페이스의 구현이며 기본 유형의 데이터에 쉽게 액세스할 수 있는 몇 가지 방법이 추가되었습니다.

job을 scheduler에 추가하기 전에 JobDetail을 구성할 때 데이터를 다음과 같은 예와 같이 JobDataMap에 넣을 수 있습니다：

```java
// 이 부분은 작업(Job)의 구체적인 내용입니다.
public class HelloJob   implements Job {
    public HelloJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        /**
         * JobExecutionContext에 쌍 포함
         * trigger 참조, JobDetail 객체 참조 및 기타 정보입니다.
         */
        System.out.println("job1실행 시작");

        JobKey key = context.getJobDetail().getKey();

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        String jobSays = jobDataMap.getString("myJob");

        float myFloatValue = jobDataMap.getFloat("myFloatValue");

        System.err.println("Instance " + key + " of HelloJob says: " + jobSays + ", and val is: " + myFloatValue);
    }
}
```

#### Job 상태 및 동시성

Job의 상태 데이터(즉, Job DataMap) 및 동시성과 관련하여 주의해야 할 부분이 몇 가지 있습니다.<br>
job 클래스에 일부 주해를 추가할 수 있으며, 이러한 어노테이션은 job의 상태와 동시성에 영향을 미칩니다.。

@DisallowConcurrentExecution: 이 어노테이션을 job 클래스에 추가하고 Quartz에게 동일한 job 정의(여기서는 특정 job 클래스를 지칭)를 동시에 수행하지 말라고 말합니다.여기 용어에 주의하세요.이전 섹션의 예를 들어, 'Sales Report Job' 클래스에 이 어노테이션이 있는 경우 동일한 시점에 하나의 'Sales Report For Joe' 인스턴스만 허용되지만 'Sales Report For Mike' 클래스의 인스턴스는 동시에 실행될 수 있습니다.따라서 이 제한은 Job Detail이 아닌 Job Detail을 대상으로 합니다.그러나 우리는 (Quartz를 설계할 때) 이 어노테이션을 job 클래스에 배치해야 한다고 생각합니다.

@PersistJobDataAfterExecution: 이 어노테이션을 job 클래스에 추가하고 Quartz에게 job 클래스의 execute 메서드가 성공적으로 실행된 후(아무런 이상이 발생하지 않음) JobDetail의 JobDataMap 데이터를 업데이트하여 다음 실행 시 JobDetail이 업데이트 이전의 오래된 데이터가 아닌 업데이트된 데이터임을 알립니다.@DisallowConcurrentExecution 어노테이션과 마찬가지로 어노테이션은 job 클래스에 추가되지만 제한 효과는 job 클래스가 아닌 job 인스턴스에 대한 것입니다.job의 내용은 종종 행동 상태에 영향을 미치기 때문에 job 클래스에 의해 주석이 전달됩니다(예: job 클래스의 execute 방법은 명시적으로 '상태'를 '이해'해야 함).

만약 당신이 @PersistJobDataAfterExecution 주석을 사용했다면, 우리는 동일한 job(JobDetail)의 두 가지 인스턴스가 동시에 실행될 때 경쟁으로 인해 JobDataMap에 저장된 데이터가 불확실할 가능성이 높기 때문에 @DisallowConcurrentExecution 주석을 함께 사용하는 것을 강력히 권장합니다.。

### 제4장 Quartz에서 Triggers 소개

### Trigger의 공통 속성

모든 유형의 트리거에는 Trigger Key라는 속성이 있으며, 이는 trigger의 정체성을 나타냅니다.이러한 속성은 Trigger를 구성할 때 Trigger Builder를 통해 설정할 수 있습니다.

트리거의 공통 속성은 다음과 같습니다：

- jobKey 속성: trigger가 트리거될 때 실행되는 job의 ID입니다.
- startTime 속성: trigger가 처음 트리거할 시간을 설정합니다. 이 속성의 값은 java입니다.util.Date 유형은 지정된 시점을 나타냅니다. 일부 trigger는 startTime을 설정할 때 즉시 트리거하며, 일부 trigger는 startTime 이후에 트리거가 시작됨을 나타냅니다.예를 들어, 지금이 1월이고 '매달 5일째에 실행'이라는 트리거를 설정한 다음 startTime 속성을 4월 1일로 설정하면 트리거의 첫 번째 트리거는 몇 개월 후(4월 5일)가 됩니다.
- endTime 속성: 트리거가 만료되는 시점을 나타냅니다.<br>예를 들어, "매월 5일째에 실행"하는 트리거의 경우 엔드타임이 7월 1일이라면 마지막 실행은 6월 5일입니다。

```java
/**
  * 연결된 트리거 설정
   */
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(  dailyAtHourAndMinute(15,44))
                .build();

        // 마지막으로 저희 Job이 트리거 사양에 맞게 작동하도록 하겠습니다.
        scheduler.scheduleJob(job, trigger);
```

시간별 트리거 지정

```java
JobDetail job = JobBuilder.newJob(DumbJob.class)
                .withIdentity("job1", "group1")
                .usingJobData("myJob","Hello world!")
                .usingJobData("jobSays","Hello world!")
                .usingJobData("myFloatValue",3.14F )

                .build();
//관련 제외 날짜 설정
 HolidayCalendar cal =  new HolidayCalendar();
        cal.addExcludedDate(new Date("2019/5/13"));

        scheduler.addCalendar("myHolidays",cal,false,false);

/**
  * simpleTrigger
  */
        SimpleTrigger simpleTrigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity("simpleTriger","group1")
                .startAt(parse)
                .forJob("job1","group1")
                .build()
                ;

```

SimpleTrigger의 Misfire 정책 상수:

```java
MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
MISFIRE_INSTRUCTION_FIRE_NOW
MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
```



### 제4장CronTrigger

Simple Trigger의 정확한 간격에 따라 재시작을 수행하는 작업 시작 계획이 필요한 경우 CronTrigger는 Simple Trigger보다 일반적으로 더 유용합니다.

CronTrigger를 사용하면 '매주 금요일 정오' 또는 '매 근무일 및 오전 9:30', 심지어 '매주 월요일부터 금요일 오전 9:00~10시 사이 매 5분' 및 1월 금요일과 같은 번호 일정을 지정할 수 있습니다.

그래도 SimpleTrigger와 마찬가지로 CronTrigger는 언제 실행되는지 지정하는 startTime과 계획을 중지할 시기를 지정하는 (옵션)endTime을 가집니다.。

### Cron Expressions

Cron-Expressions는 CronTrigger 인스턴스를 구성하는 데 사용됩니다. Cron Expressions는 스케줄의 세부 사항을 설명하는 7개의 하위 표현식으로 구성된 문자열입니다.이 하위 표현식은 공백으로 구분되며, 표시됨：

1. Seconds
2. Minutes
3. Hours
4. Day-of-Month
5. Month
6. Day-of-Week
7. Year (optional field)

### JDBC JobStore

JDBC JobStore도 적절하게 명명되었으며 JDBC를 통해 모든 데이터를 데이터베이스에 저장합니다.그래서 RAMJobStore보다 사양이 조금 더 복잡하고 빠르지도 않습니다.그러나 특히 주 키에 인덱스가 있는 데이터베이스 테이블을 구성하면 성능 저하가 그리 나쁘지 않습니다.근사한 LAN(스케줄링 프로그램과 데이터베이스 사이)이 있는 상당히 현대적인 시스템에서 검색 및 업데이트가 트리거하는 시간은 일반적으로 10밀리초 미만입니다.

JDBC JobStore는 거의 모든 데이터베이스와 함께 사용되며 Oracle, PostgreSQL, MySQL, MS SQLServer, HSQLDB 및 DB2에 널리 사용되었습니다.JDBC JobStore를 사용하려면 먼저 Quartz에서 사용할 데이터베이스 테이블 세트를 생성해야 합니다.Quartz 배포판의 "docs/dbTables" 카탈로그에서 표를 찾아 SQL 스크립트를 작성할 수 있습니다.데이터베이스 유형에 아직 스크립트가 없으면 스크립트 중 하나를 보고 데이터베이스에 필요한 대로 수정하십시오.이러한 스크립트에서 모든 테이블은 접두사 'QRTZ_'로 시작된다는 점에 유의해야 합니다(예: 테이블 'QRTZ_TRIGGERS' 및 'QRTZ_JOB_DETAIL'.JDBC JobStore 접두사가 무엇인지(Quartz 속성에서) 알려주면 이 접두사는 실제로 원하는 것이 될 수 있습니다.여러 스케줄러 인스턴스의 경우 다른 접두사를 사용하면 여러 테이블을 만드는 데 도움이 될 수 있습니다.

표를 만든 후 JDBC JobStore를 구성하고 시작하기 전에 중요한 결정이 하나 더 있습니다.앱에 필요한 트랜잭션의 종류를 결정해야 합니다.triggers 추가 및 삭제와 같은 스케줄링 명령을 다른 트랜잭션으로 바인딩할 필요가 없는 경우 JobStoreTX를 JobStore로 사용하여 트랜잭션을 관리할 수 있습니다(가장 일반적인 옵션).

Quartz가 다른 트랜잭션(즉, J2EE 응용 프로그램 서버)과 함께 작업해야 하는 경우 JobStoreCMT를 사용해야 합니다. 이 경우 Quartz는 응용 프로그램 서버 컨테이너에서 트랜잭션을 관리합니다.

마지막 문제는 JDBC JobStore가 데이터베이스에 대한 연결을 얻을 수 있는 DataSource를 설정하는 것입니다.DataSources는 Quartz 속성에서 몇 가지 다른 방법 중 하나를 사용하여 정의됩니다.한 가지 방법은 데이터베이스에 대한 모든 연결 정보를 제공함으로써 Quartz가 DataSource 자체를 만들고 관리할 수 있도록 하는 것입니다.또 다른 방법은 Quartz가 실행 중인 응용 프로그램 서버에서 관리하는 DataSource를 Quartz가 JDBCJobStore DataSource의 JNDI 이름을 제공함으로써 사용할 수 있도록 하는 것입니다.속성에 대한 자세한 내용은 "docs/config" 폴더의 샘플 프로필을 참조하십시오.

JDBCJobStore를 사용하려면 먼저 Quartz에서 설정한 JobStore 클래스 속성을 org로 설정해야 합니다.quartz.impl.jdbcjobstore.JobStoreTX나 org.quartz.impl.jdbcjobstore.JobStoreCMT - 위의 단락의 해석에 따라 선택됩니다.。

#### TerracottaJobStore

Terracotta JobStore는 데이터베이스를 사용하지 않고 스케일링 및 견고성을 위한 수단을 제공합니다.이것은 당신의 데이터베이스가 Quartz의 부하로부터 자유로워지고 그 모든 자원을 애플리케이션의 나머지 부분으로 저장할 수 있다는 것을 의미합니다.

Terracotta JobStore는 클러스터링 또는 비클러스터링을 실행할 수 있으며 두 경우 모두 데이터가 Terracotta 서버에 저장되기 때문에 응용 프로그램 재시작 사이에 지속적인 작업 데이터를 위한 저장 매체를 제공합니다.JDBC JobStore를 통해 데이터베이스를 사용하는 것보다 훨씬 우수하지만(약 한 자릿수 더 좋음), RAMJobStore보다는 느립니다.

TerracottaJobStore를 사용하려면 클래스 이름을 org.quartz.잡스토어class = org.테라코타quartz.TerracottaJobStore는 쿼츠 설정을 위한 JobStore 클래스 속성을 지정하고 추가 행 구성을 추가하여 Terracotta 서버의 위치를 지정합니다：
