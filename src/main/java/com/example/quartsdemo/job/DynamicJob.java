package com.example.quartsdemo.job;

import com.example.quartsdemo.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * :@DisallowConcurrentExecution : 此标记用在实现Job的类上面,意思是不允许并发执行.
 * :注意org.quartz.threadPool.threadCount线程池中线程的数量至少要多个,否则@DisallowConcurrentExecution不生效
 * :假如Job的设置时间间隔为3秒,但Job执行时间是5秒,设置@DisallowConcurrentExecution以后程序会等任务执行完毕以后再去执行,否则会在3秒时再启用新的线程执行
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution //没有异常就更新数据 可用?
@Component
@Slf4j
public class DynamicJob implements Job {
    private Logger logger = LoggerFactory.getLogger(DynamicJob.class);

    /**
     핵심 메서드, Quartz Job의 실제 실행 로직입니다.
     executorContext에는 Quartz가 실행에 필요한 모든 정보가 포함되어 있습니다.
     @throws JobExecutionException execute() 메서드는 JobExecutionException 예외만 허용합니다.
     */
    @Override
    public void execute(JobExecutionContext executionContext) throws JobExecutionException {
        // JobDataMap에서 필요한 데이터를 가져옵니다.
        JobDataMap map = executionContext.getMergedJobDataMap();

        // 실행중인 Job 정보를 로그로 출력합니다.
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

        // Job 실행 시작 시간을 기록합니다.
        long startTime = System.currentTimeMillis();
        // jarPath가 비어있지 않으면 작업을 실행합니다.
        if (!StringUtils.getStringUtil.isEmpty(jarPath)) {

            File jar = new File(jarPath);
            // jar 파일이 존재하는 경우에만 실행합니다.
            if (jar.exists()) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(jar.getParentFile());
                /**
                 * 이것은 Java 실행 명령어입니다.
                 * java -jar
                 */
                // Java 실행 명령어를 설정합니다. (java -jar)
                List<String> commands = new ArrayList<>();
                commands.add("java");
                if (!StringUtils.getStringUtil.isEmpty(vmParam)) {
                    commands.add(vmParam);
                }
                commands.add("-jar");
                commands.add(jarPath);
                commands.add(" &");
                System.out.println("commands->\n"+commands);

                // 파라미터가 비어있지 않으면 명령어에 추가하고 프로세스를 실행합니다.
                if (!StringUtils.getStringUtil.isEmpty(parameter)) {

                    commands.add(parameter);
                    processBuilder.command(commands);
                    logger.info("Running Job details as follows >>>>>>>>>>>>>>>>>>>>: ");
                    logger.info("Running Job commands : {}  ", StringUtils.getStringUtil.getListString(commands));
                    // 프로세스를 시작하고, 입력과 오류 스트림을 로깅합니다.
                    try {
                        Process process = processBuilder.start();
                        logProcess(process.getInputStream(), process.getErrorStream());

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                } else {
                    // 파라미터가 비어있는 경우, Job 실행 예외를 던집니다.
                    throw new JobExecutionException("Job Jar not found >>  " + jarPath);
                }

                // Job 실행 종료 시간을 기록하고 실행 시간을 로그로 출력합니다.
                long endTime = System.currentTimeMillis();
                logger.info(">>>>>>>>>>>>> Running Job has been completed , cost time :  " + (endTime - startTime) + "ms\n");
            }


        }


    }

    //Job의 실행 내용을 로그로 출력합니다.
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
