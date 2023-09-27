package com.ares.urlshortening.service.implementation;

import com.ares.urlshortening.domain.UrlEvent;
import com.ares.urlshortening.domain.UserEvent;
import com.ares.urlshortening.enumeration.EventType;
import com.ares.urlshortening.repository.EventRepository;
import com.ares.urlshortening.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;


    @Override
    public Collection<UserEvent> getEventsByUserId(Long userId) {
        return eventRepository.getEventsByUserId(userId);
    }

    @Override
    public void addUserEvent(Long id, EventType eventType, String device, String browser, String ipAddress) {
        eventRepository.addUserEvent(id,eventType,device,browser,ipAddress);
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String browser, String ipAddress) {
        eventRepository.addUserEvent(email,eventType,device,browser,ipAddress);

    }
    @Override
    public void addVisitLinkEvent(Long userId, EventType eventType, Long urlId, String device, String browser, String ipAddress) {
        eventRepository.addVisitLinkEvent(userId, eventType,urlId, device,  browser,  ipAddress);
    }

    @Override
    public Collection<UrlEvent> getUrlEventsByUserId(Long userId) {
        return eventRepository.getUrlEventsByUserId(userId);
    }

    @Override
    public Collection<UrlEvent> getUrlEventsByUrlId(Long urlId) {
        return eventRepository.getUrlEventsByUrlId(urlId);
    }
}
