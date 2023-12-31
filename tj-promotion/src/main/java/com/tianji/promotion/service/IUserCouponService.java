package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.UserCoupon;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 * @author 东哥
 * @since 2023-06-04
 */
public interface IUserCouponService extends IService<UserCoupon> {

    void receiveCoupon(Long couponId) throws InterruptedException;

    void exchangeCoupon(String code);

    void checkAndCreateUserCoupon(Coupon coupon, Long userId);
}
