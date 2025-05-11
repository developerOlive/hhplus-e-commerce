package com.hhplusecommerce.infrastructure.cache;

public interface CacheSupport<T> {
    T get(Object keyParams);
    void put(Object keyParams, T data);
    void evict(Object keyParams);
}
