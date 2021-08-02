package com.xxl.job.executor.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisXxlJob {
    private Logger logger = LoggerFactory.getLogger(RedisXxlJob.class);

    @XxlJob("RedisInfoHandler")
    public void getInfoHandler() {
        XxlJobHelper.log("RedisXxlJob-getInfoHandler");
        System.out.println("redis-handler");
    }
    @XxlJob("RedisSetHandler")
    public void setInfoHandler() {
        XxlJobHelper.log(">>>>>>Redis-set<<<<<<");
        System.out.println("redis-set");
    }
}
