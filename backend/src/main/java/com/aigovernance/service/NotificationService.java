package com.aigovernance.service;

import com.aigovernance.model.Notification;
import com.aigovernance.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    public NotificationService(NotificationRepository repository){this.repository=repository;}
    public Notification create(Long userId,String type,String title,String message){
        Notification n=new Notification();n.userId=userId;n.type=type;n.title=title;n.message=message;return repository.save(n);
    }
}
