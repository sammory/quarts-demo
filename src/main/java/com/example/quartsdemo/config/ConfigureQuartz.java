package com.example.quartsdemo.config;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by   on 2019/5/22 11:02
 * Quartz의 핵심 구성 클래스
 */
@Configuration
public class ConfigureQuartz {

    // JobFactory 구성
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * SchedulerFactoryBean은 org.quartz.Scheduler의 생성과 구성을 제공하며 라이프사이클을 Spring과 동기화합니다.
     * org.quartz.Scheduler: 모든 스케줄링은 스케줄러에 의해 제어됩니다.
     * @param dataSource 데이터 소스 구성에 사용됩니다.
     * @param jobFactory JobFactory 구성에 사용됩니다.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // 선택 사항: QuartzScheduler가 시작될 때 기존에 존재하는 Job을 업데이트합니다.
        // 이렇게 하면 targetObject를 수정한 후에 qrtz_job_details 테이블의 해당 레코드를 삭제할 필요가 없습니다.
        factory.setOverwriteExistingJobs(true); // 기존 job업데이트 or 새로운 job추가
        factory.setAutoStartup(true); // 자동 시작 설정
        factory.setDataSource(dataSource); //Quartz Scheduler가 데이터베이스를 사용하여 Job과 Trigger를 저장하고 관리하는데, 이 데이터 소스를 지정
        factory.setJobFactory(jobFactory); //  Quartz에서 Job을 생성하는 역할
        factory.setQuartzProperties(quartzProperties()); // Quartz의 설정을 가져와 SchedulerFactoryBean에 설정
        return factory;
    }

    // quartz.properties 파일에서 Quartz 구성 속성 읽기
    @Bean
    public Properties quartzProperties() throws IOException {

        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));

        propertiesFactoryBean.afterPropertiesSet();

        return propertiesFactoryBean.getObject();
    }

    // JobFactory 구성, Quartz 작업에 자동 연결 지원 추가
    public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements
            ApplicationContextAware {

        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory(); // Spring의 빈(Bean)을 관리하고 의존성 주입을 담당
        }

        // JobFactory는 Spring의 의존성 주입을 지원하지 않습니다.
        // 따라서 이를 해결하기 위해 위의 코드처럼 JobFactory를 재정의하여 Spring의 beanFactory를 사용하여 Job 인스턴스에 의존성을 주입하는 것입니다.
        //  Quartz 스케줄러가 Job 인스턴스를 생성할 때 Spring의 의존성 주입 기능을 활용할 수 있게 되어, Job 클래스에서도 Spring의 기능과 서비스를 이용할 수 있게 됩니다.
        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }

}
