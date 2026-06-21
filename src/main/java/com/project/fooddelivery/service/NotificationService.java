package com.project.fooddelivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info(
            "Notify order {} status {} to customer={}, owner={}, partner={}",
            event.orderId(),
            event.status(),
            event.customerUsername(),
            event.restaurantOwnerUsername(),
            event.deliveryPartnerUsername()
        );
    }
}
