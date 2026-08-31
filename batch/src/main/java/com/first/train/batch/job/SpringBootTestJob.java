package com.first.train.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SpringBoot 自带定时任务示例（适合单机，不适合分布式集群）
 */
@Component
@EnableScheduling
public class SpringBootTestJob {

    private static final Logger LOG = LoggerFactory.getLogger(SpringBootTestJob.class);

    // @Scheduled(cron = "0/10 * * * * ?")
    public void test() {
        LOG.info("SpringBootTestJob 单机定时任务执行");
    }
}
