package com.tianji.promotion.handler;

import com.tianji.common.domain.dto.UserCouponDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.tianji.common.constants.MqConstants.Exchange.PROMOTION_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.COUPON_RECEIVE;

@RequiredArgsConstructor
@Component
public class PromotionMqHandler {

    private final IUserCouponService userCouponService;
    private final ICouponService couponService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "coupon.receive.queue", durable = "true"),
            exchange = @Exchange(name = PROMOTION_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = COUPON_RECEIVE
    ))
    public void listenCouponReceiveMessage(UserCouponDTO uc){
        Coupon coupon = couponService.getById(uc.getCouponId());
        userCouponService.checkAndCreateUserCoupon(coupon,uc.getUserId());
    }
}