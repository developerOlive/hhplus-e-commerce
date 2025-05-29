package com.hhplusecommerce.support.notification;

public interface AppPushNotifier {
    void sendPush(Long userId, String message);
}
