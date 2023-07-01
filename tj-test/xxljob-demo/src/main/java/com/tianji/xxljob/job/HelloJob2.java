package com.tianji.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;


//SpringTask局限性
//1、多实例部署时，无法感知对方，同一个任务同一时间多次执行，更无法实现多实例之间的负载均衡
//2、没有控制台，无法监控任务执行的情况，无法跟踪任务执行日志
//3、无法对同一任务中的大量子任务进行切片处理
//4、无法对任务进行编排（对任务进行依赖顺序处理）
@Slf4j
//@Component
public class HelloJob2 {
    @Value("${server.port}")
    private String port;

    @Scheduled(cron = "0/5 * * * * ?")
    public void demoJobHandler(){
        log.warn("简单任务执行了。。。。端口：{}",port);
    }

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