package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author 东哥
 * @since 2023-06-02
 */
public interface ICouponService extends IService<Coupon> {
    void saveCoupon(CouponFormDTO couponDTO);

    PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query);

    void beginIssue(CouponIssueFormDTO couponIssueDTO);

    List<CouponVO> queryIssuingCoupons();

    void deleteById(Long id);

    void pauseIssue(Long id);
}
