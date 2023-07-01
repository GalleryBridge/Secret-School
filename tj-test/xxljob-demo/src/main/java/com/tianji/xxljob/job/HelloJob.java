package com.tianji.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HelloJob {
    @Value("${server.port}")
    private String port;

    @XxlJob("demoJobHandler")
    public void demoJobHandler(){
        log.warn("简单任务执行了。。。。端口：{}",port);
    }

    @XxlJob("shardingJobHandler")
    public void shardingJobHandler(){
        //分片的参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //业务逻辑
        List<Integer> list = getList();
        for (Integer task : list) {
            if(task % shardTotal == shardIndex){
                log.warn("当前第{}分片执行了，任务项为：{}",shardIndex, task);
            }
        }
    }

    @XxlJob("childJobHandler")
    public void childJobHandler(){
        log.error("分片任务的子任务执行了。。。。端口：{}",port);
    }

    public List<Integer> getList(){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        return list;
    }
}