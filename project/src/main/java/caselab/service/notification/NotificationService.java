package caselab.service.notification;

public interface NotificationService<T extends NotificationDetails> {
    void sendNotification(T notificationDetails);
}
