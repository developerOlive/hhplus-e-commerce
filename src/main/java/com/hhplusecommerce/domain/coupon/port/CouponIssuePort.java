package com.hhplusecommerce.domain.coupon.port;

import java.util.Set;

public interface CouponIssuePort {

    boolean isIssued(String couponIssuedKey, String userId);

    void addToRequestQueue(String couponRequestKey, String userId, long scoreTimestamp);

    Set<String> popRequests(String couponRequestKey, int batchSize);

    Long decrementStock(String couponStockKey);

    void addIssuedUser(String couponIssuedKey, String userId);

    void incrementStock(String couponStockKey);

    void removeIssuedUser(String couponIssuedKey, String userId);

    String getRequestQueueKey(Long couponId);

    String getIssuedKey(Long couponId);

    String getStockKey(Long couponId);
}
