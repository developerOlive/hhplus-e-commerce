package com.hhplusecommerce.support.lock;

@FunctionalInterface
public interface LockCallback<T> {

    T doInLock() throws Throwable;
}
