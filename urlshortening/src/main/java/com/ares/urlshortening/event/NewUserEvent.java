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
    private Long urlId;

    public NewUserEvent(Long userId,EventType type) {
        super(userId);
        this.type = type;
        this.userId = userId;
    }

    public NewUserEvent(EventType type, Long userId, Long urlId) {
        super(userId);
        this.type = type;
        this.userId = userId;
        this.urlId = urlId;
    }
}
