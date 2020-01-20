package com.cbhb.cbhbdetect;

import com.cbhb.job.CronJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

@Configuration
public class QuartzConfiguration {

//    @Bean
//    public JobDetail pdfJobDetail(){
//        return JobBuilder.newJob(InvokePDFJob.class).withIdentity("pdfjob").storeDurably().build();
//    }

//    @Bean
//    public Trigger pdfTrigger(){
//        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10).repeatForever();
//        return TriggerBuilder.newTrigger().forJob(pdfJobDetail()).withIdentity("pdfjob").withSchedule(scheduleBuilder).build();
//    }
    @Bean
    public JobDetail cronJobDetail(){
        return JobBuilder.newJob(CronJob.class).withIdentity("cronJob").storeDurably().build();
    }

    @Bean
    public Trigger cronTrigger(){
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 30 23 * * ? *").withMisfireHandlingInstructionDoNothing();
        return TriggerBuilder.newTrigger().forJob(cronJobDetail()).withIdentity("crontrigger").withSchedule(scheduleBuilder).build();
    }




}
