package com.paypal.notification_service.Controller;

import com.paypal.notification_service.entity.Notification;
import com.paypal.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @PostMapping
    public Notification sendNotification(@RequestBody Notification notification){
        return notificationService.sendNotification(notification);
    }

    @GetMapping("/{userId}")
    public List<Notification> getNotifcationByUser(@PathVariable String userId){
        return  notificationService.getNotifcationByUserId(userId);
    }
}
