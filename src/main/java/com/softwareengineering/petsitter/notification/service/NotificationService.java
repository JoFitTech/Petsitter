package com.softwareengineering.petsitter.notification.service;

import com.softwareengineering.petsitter.notification.domain.Notification;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public List<Notification> getInbox(UUID userId) {
        return Collections.emptyList();
    }
}

