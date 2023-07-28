package com.example.quartsdemo.controller;

import com.example.quartsdemo.entity.JobEntity;
import com.example.quartsdemo.service.QuartzService;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * quartz
 * @author from https://www.cnblogs.com/ealenxie/p/9134602.html
 */
@Controller
@RequestMapping("/")
public class QuartzController {

    private static final Logger logger = LoggerFactory.getLogger(QuartzController.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    @Autowired
    private QuartzService jobService;

    //모든 Job 초기화 및 시작
    @PostConstruct
    public void initialize() {
        try {
            reStartAllJobs();
            logger.info("INIT SUCCESS");
        } catch (SchedulerException e) {
            logger.info("INIT EXCEPTION : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 특정 ID의 Job 다시 시작
    @RequestMapping("/refresh/{id}")
    public String refresh(@PathVariable Integer id) throws SchedulerException {
        String result;
        JobEntity entity = jobService.getJobEntityById(id);
        if (entity == null){
            return "error: id is not exist ";
        }
        synchronized (logger) {
            JobKey jobKey = jobService.getJobKey(entity);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
            scheduler.deleteJob(jobKey);
            JobDataMap map = jobService.getJobDataMap(entity);
            JobDetail jobDetail = jobService.geJobDetail(jobKey, entity.getDescription(), map);
            if (entity.getStatus().equals("OPEN")) {
                scheduler.scheduleJob(jobDetail, jobService.getTrigger(entity));
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " success !";
            } else {
                result = "Refresh Job : " + entity.getName() + "\t jarPath: " + entity.getJarPath() + " failed ! , " +
                        "Because the Job status is " + entity.getStatus();
            }
        }
        return result;
    }

    /**
     * 모든 Job을 다시 시작
     */
    private void reStartAllJobs() throws SchedulerException {

        synchronized (logger) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());

            scheduler.pauseJobs(GroupMatcher.anyGroup());//모든 JOB 일시 정지

            for (JobKey jobKey : jobKeys) {  //데이터베이스에 등록된 모든 JOB 삭제
                logger.info("jobKey==>"+jobKey+"\n");
                scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                scheduler.deleteJob(jobKey);
            }

            //데이터베이스에 등록된 모든 JOB
            for (JobEntity job : jobService.loadJobs()) {
                logger.info("Job register name : {} , group : {} , cron : {}", job.getName(), job.getGroup(), job.getCron());

                JobDataMap map = jobService.getJobDataMap(job);
                JobKey jobKey = jobService.getJobKey(job);

                JobDetail jobDetail = jobService.geJobDetail(jobKey, job.getDescription(), map);
                if (job.getStatus().equals("OPEN")) {
                    scheduler.scheduleJob(jobDetail, jobService.getTrigger(job));
                } else {
                    logger.info("Job jump name : {} , Because {} status is {}", job.getName(), job.getName(), job.getStatus());
                }
            }
        }
    }

    @RequestMapping("/test/{id}")
    @ResponseBody
    public JobEntity testList(@PathVariable("id") int id) {
        JobEntity jobEntity = jobService.getById(id);

        return jobEntity;

    }

    @RequestMapping("/tablePage")
    public String quartzList() {

        return "table_page";
    }


}
