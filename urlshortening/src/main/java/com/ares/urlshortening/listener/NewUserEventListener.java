package com.ares.urlshortening.listener;

import com.ares.urlshortening.event.NewUserEvent;
import com.ares.urlshortening.service.EventService;
import com.ares.urlshortening.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.ares.urlshortening.utils.RequestUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewUserEventListener {

    private final EventService eventService;
    private final HttpServletRequest request;

    @EventListener
    public void onNewUserEvent(NewUserEvent event){
        eventService.addUserEvent(
                event.getUserId(),event.getType(), getDevice(request),getBrowser(request),getIpAddress(request)
        );
    }
}
