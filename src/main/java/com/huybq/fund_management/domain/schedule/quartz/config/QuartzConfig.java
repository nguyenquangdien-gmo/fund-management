package com.huybq.fund_management.domain.schedule.quartz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();

        // Set the JobFactory to allow injection in your job classes
        factoryBean.setJobFactory(springBeanJobFactory());

        // Use the Spring-provided dataSource
        factoryBean.setDataSource(dataSource);

        Properties quartzProperties = new Properties();
        quartzProperties.put("org.quartz.scheduler.instanceName", "ClusteredScheduler");
        quartzProperties.put("org.quartz.scheduler.instanceId", "AUTO");
        
        // Configure JobStore
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        quartzProperties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        quartzProperties.put("org.quartz.jobStore.useProperties", "false");
        quartzProperties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");

        // Add the dataSource name property - this is the critical fix
        quartzProperties.put("org.quartz.jobStore.dataSource", "myDS");
        quartzProperties.put("org.quartz.dataSource.myDS.provider", "hikaricp");
        quartzProperties.put("org.quartz.dataSource.myDS.driver", "com.mysql.cj.jdbc.Driver");
        quartzProperties.put("org.quartz.dataSource.myDS.URL", "jdbc:mysql://localhost:3306/fund_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        quartzProperties.put("org.quartz.dataSource.myDS.user", "root");
        quartzProperties.put("org.quartz.dataSource.myDS.password", "huybq");

        // Enable clustering
        quartzProperties.put("org.quartz.jobStore.isClustered", "true");
        quartzProperties.put("org.quartz.jobStore.clusterCheckinInterval", "20000");

        factoryBean.setQuartzProperties(quartzProperties);
        factoryBean.setOverwriteExistingJobs(true);
        factoryBean.setAutoStartup(true);
        factoryBean.setApplicationContextSchedulerContextKey("applicationContext");

        return factoryBean;
    }
}
