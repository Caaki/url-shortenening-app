package com.ares.urlshortening.repository;

import com.ares.urlshortening.domain.UserEvent;
import com.ares.urlshortening.enumeration.EventType;

import java.util.Collection;

public interface EventRepository {

    Collection<UserEvent> getEventsByUserId(Long userId);
    void addUserEvent(Long id, EventType eventType, String device, String browser, String ipAddress);
    void addUserEvent(String email, EventType eventType, String device, String browser, String ipAddress);


}
