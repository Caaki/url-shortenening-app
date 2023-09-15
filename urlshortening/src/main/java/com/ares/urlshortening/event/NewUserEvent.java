package com.ares.urlshortening.event;

import com.ares.urlshortening.enumeration.EventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
@Getter
@Setter
public class NewUserEvent extends ApplicationEvent {
    private EventType type;
    private Long userId;

    public NewUserEvent(Long userId,EventType type) {
        super(userId);
        this.type = type;
        this.userId = userId;
    }
}
