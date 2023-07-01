package com.tianji.promotion.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static com.tianji.promotion.utils.MyLockType.*;

@Component
public class MyLockFactory {

    private final Map<MyLockType, Function<String, RLock>> lockHandlers;

    public MyLockFactory(RedissonClient redissonClient) {
        this.lockHandlers = new EnumMap<>(MyLockType.class);
        this.lockHandlers.put(RE_ENTRANT_LOCK, redissonClient::getLock);   // ===>  this.lockHandlers.put(RE_ENTRANT_LOCK, name -> redissonClient.getLock(name));
        this.lockHandlers.put(FAIR_LOCK, redissonClient::getFairLock);  // ===>  this.lockHandlers.put(FAIR_LOCK, name -> redissonClient.getFairLock(name));
        this.lockHandlers.put(READ_LOCK, name -> redissonClient.getReadWriteLock(name).readLock());
        this.lockHandlers.put(WRITE_LOCK, name -> redissonClient.getReadWriteLock(name).writeLock());
    }

    public RLock getLock(MyLockType lockType, String name){
        return lockHandlers.get(lockType).apply(name);
    }


    /**
     * 自定义函数式接口
     */
    /*@FunctionalInterface
    interface MyFunction {
        Integer apply(Integer x, Integer y);
    }


    private static Map<String,MyFunction> map = new HashMap<>();
    {
        map.put("mutiply",(x,y) -> x * y);
        map.put("sub",(x,y) -> x - y);
        map.put("sum",(x,y) -> x + y);
    }

    @Test
    public void testFunctionInterface3() {
        Integer result = map.get("mutiply").apply(5, 6);
        System.out.println(result);

        result = map.get("sub").apply(5, 6);
        System.out.println(result);

        result = map.get("sum").apply(5, 6);
        System.out.println(result);
    }*/
}