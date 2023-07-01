package com.tianji.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.utils.CodeUtil;
import com.tianji.promotion.utils.MyLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务实现类
 * </p>
 *
 * @author 东哥
 * @since 2023-06-04
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService {
    private final CouponMapper couponMapper;

    private final IExchangeCodeService codeService;

    //private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate redisTemplate;
    private final RabbitMqHelper mqHelper;

    @Override
    @MyLock(name = "lock:coupon:#{couponId}")
//    @Lock(name = "lock:coupon:#{couponId}")
    public void receiveCoupon(Long couponId) throws InterruptedException {
        // 1.查询优惠券
        //Coupon coupon = couponMapper.selectById(couponId);
        Coupon coupon = queryCouponByCache(couponId);
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在");
        }
        // 2.校验发放时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueBeginTime()) || now.isAfter(coupon.getIssueEndTime())) {
            throw new BadRequestException("优惠券发放已经结束或尚未开始");
        }
        // 3.校验库存
       /* if (coupon.getIssueNum() >= coupon.getTotalNum()) {
            throw new BadRequestException("优惠券库存不足");
        }*/
        if (coupon.getTotalNum() <= 0) {
            throw new BadRequestException("优惠券库存不足");
        }
        Long userId = UserContext.getUser();

        //4、校验每人限数量
        //4.1  查询领取数量
        String key = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + couponId;
        Long count = redisTemplate.opsForHash().increment(key, userId.toString(), 1);
        //4.2  校验领取数量
        if(count > coupon.getUserLimit()){
            throw new BadRequestException("超出领取数量");
        }

        //5.扣减redis中优惠券库存
        redisTemplate.opsForHash().increment(
                PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId,"totalNum",-1);

        //6、发送MQ消息
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        mqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE, MqConstants.Key.COUPON_RECEIVE, uc);


        // 6.校验每人限领数量
        //6.1 version、synchronized进行业务锁定
        /*String lock = userId.toString().intern();
        synchronized (lock){
            IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
            userCouponService.checkAndCreateUserCoupon(coupon,userId);
            lock.wait();
        }*/

        //6.2 version、使用自定义的redis工具类redisLock进行业务锁定
        /*String key = "lock:coupon:uid:" + userId;
        RedisLock redisLock = new RedisLock(key, redisTemplate);
        String lockId = String.valueOf(Thread.currentThread().getId());
        try {
            boolean isLock = redisLock.tryLock(5, TimeUnit.SECONDS, lockId);
            if(!isLock){
                throw new BizIllegalException("请求太频繁");
            }
            IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
            userCouponService.checkAndCreateUserCoupon(coupon,userId);
        } finally {
            redisLock.unlock(lockId);
        }*/

        //6.3 version、使用自定义的redisson进行业务锁定
        /*String key = "lock:coupon:uid:" + userId;
        RLock lock = redissonClient.getLock(key);
        boolean isLock = lock.tryLock();
        //Thread.sleep(35000);
        if(!isLock){
            throw new BizIllegalException("请求太频繁");
        }
        try {
            IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
            userCouponService.checkAndCreateUserCoupon(coupon,userId);
        } finally {
            lock.unlock();
        }*/

        //6.4 version、使用AOP切面 + 自定义注解优化Redission进行业务锁定
        /*IUserCouponService userCouponService = (IUserCouponService)AopContext.currentProxy();
        userCouponService.checkAndCreateUserCoupon(coupon,userId);*/
    }


    @Override
    @Transactional
    public void checkAndCreateUserCoupon(Coupon coupon, Long userId){
        // 1.校验每人限领数量
        // 1.1.统计当前用户对当前优惠券的已经领取的数量
        Integer count = lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, coupon.getId())
                .count();
        // 1.2.校验限领数量
        if(count != null && count >= coupon.getUserLimit()){
            throw new BadRequestException("超出领取数量");
        }
        // 2.更新优惠券的已经发放的数量 + 1
        int r = couponMapper.incrIssueNum(coupon.getId());
        if(r == 0){
            throw new BizIllegalException("优惠券库存不足！");
        }
        // 3.新增一个用户券
        saveUserCoupon(coupon, userId);
//      throw new RuntimeException("测试抛出异常");
    }

    private Coupon queryCouponByCache(Long couponId) {
        // 1.准备KEY
        String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId;
        // 2.查询
        Map<Object, Object> objMap = redisTemplate.opsForHash().entries(key);
        if (objMap.isEmpty()) {
            return null;
        }
        // 3.数据反序列化
        return BeanUtils.mapToBean(objMap, Coupon.class, false, CopyOptions.create());
    }

    private void saveUserCoupon(Coupon coupon, Long userId) {
        // 1.基本信息
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(coupon.getId());
        // 2.有效期信息
        LocalDateTime termBeginTime = coupon.getTermBeginTime();
        LocalDateTime termEndTime = coupon.getTermEndTime();
        if (termBeginTime == null) {
            termBeginTime = LocalDateTime.now();
            termEndTime = termBeginTime.plusDays(coupon.getTermDays());
        }
        uc.setTermBeginTime(termBeginTime);
        uc.setTermEndTime(termEndTime);
        // 3.保存
        save(uc);
    }

    @Override
    public void exchangeCoupon(String code) {
        // 1.校验并解析兑换码
        long serialNum = CodeUtil.parseCode(code);
        // 2.校验是否已经兑换 SETBIT KEY 4 1 ，这里直接执行setbit，通过返回值来判断是否兑换过
        boolean exchanged = codeService.updateExchangeMark(serialNum, true);
        if (exchanged) {
            throw new BizIllegalException("兑换码已经被兑换过了");
        }
        try {
            // 3.查询兑换码对应的优惠券id
            ExchangeCode exchangeCode = codeService.getById(serialNum);
            if (exchangeCode == null) {
                throw new BizIllegalException("兑换码不存在！");
            }
            // 4.是否过期
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(exchangeCode.getExpiredTime())) {
                throw new BizIllegalException("兑换码已经过期");
            }
            // 5.校验并生成用户券
            // 5.1.查询优惠券
            Coupon coupon = couponMapper.selectById(exchangeCode.getExchangeTargetId());
            // 5.2.查询用户
            Long userId = UserContext.getUser();
            // 5.3.校验并生成用户券，更新兑换码状态
            synchronized (userId.toString().intern()){
                checkAndCreateUserCoupon(coupon,userId);
            }
            // 6.更新兑换码状态
            codeService.lambdaUpdate()
                    .set(ExchangeCode::getUserId, userId)
                    .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                    .eq(ExchangeCode::getId, serialNum)
                    .update();
        } catch (Exception e) {
            // 重置兑换的标记 0
            codeService.updateExchangeMark(serialNum, false);
            throw e;
        }
    }
}
