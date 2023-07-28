package com.example.quartsdemo.service.impl;

import com.example.quartsdemo.entity.JobEntity;
import com.example.quartsdemo.job.DynamicJob;
import com.example.quartsdemo.repository.JobEntityRepository;
import com.example.quartsdemo.service.QuartzService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuartzServiceImpl implements QuartzService {

    @Autowired
    private JobEntityRepository repository;

    @Override
    public JobEntity getById(int id) {
        return repository.getById(id);
    }

    //通过Id获取Job
    public JobEntity getJobEntityById(Integer id) {
        return repository.getById(id);
    }
    //从数据库中加载获取到所有Job
    public List<JobEntity> loadJobs() {
        List<JobEntity> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }
    //获取JobDataMap.(Job参数对象)
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
    // JobDetail을 가져오는데, JobDetail은 작업의 정의이며, Job은 작업의 실행 로직입니다. JobDetail에서는 작업을 정의하기 위해 Job Class를 참조합니다.
    // 작업(Job)에 대한 메타데이터(정보)를 포함, 작업(Job)을 식별하고 실행할 때 필요한 정보를 담고 있습니다.
    public JobDetail geJobDetail(JobKey jobKey, String description, JobDataMap map) {
        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobKey)
                .withDescription(description)
                .setJobData(map)
                .storeDurably()
                .build();
    }
    //Trigger(작업의 트리거, 실행 규칙), 스케줄러에게 언제 작업을 실행해야 하는지를 알려주는 역할
    public Trigger getTrigger(JobEntity job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getName(), job.getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()))
                .build();
    }
    //JobKey(작업 키), 이름(Name)과 그룹(Group)을 포함합니다.
    public JobKey getJobKey(JobEntity job) {

        return JobKey.jobKey(job.getName(), job.getGroup());
    }

    /**
     * 다음은 데이터를 가져오는 작업 코드입니다.
     */


}
