package com.ares.urlshortening.service;

import com.ares.urlshortening.domain.UrlEvent;
import com.ares.urlshortening.domain.UserEvent;
import com.ares.urlshortening.enumeration.EventType;

import java.util.Collection;

public interface EventService {

    Collection<UserEvent> getEventsByUserId(Long userId);
    void addUserEvent(Long id, EventType eventType, String device, String browser, String ipAddress);
    void addUserEvent(String email, EventType eventType, String device, String browser, String ipAddress);
    void addVisitLinkEvent(Long userId, EventType eventType,Long urlId, String device, String browser, String ipAddress);
    Collection<UrlEvent>getUrlEventsByUserId(Long userId);
    Collection<UrlEvent>getUrlEventsByUrlId(Long urlId);

}
