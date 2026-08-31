package com.first.train.batch.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class TestJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(TestJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("TestJob 定时调度任务执行开始");
        LOG.info("TestJob 定时调度任务执行结束");
    }
}
