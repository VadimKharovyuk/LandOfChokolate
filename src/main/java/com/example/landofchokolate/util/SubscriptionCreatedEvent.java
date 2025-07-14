package com.example.landofchokolate.util;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubscriptionCreatedEvent extends ApplicationEvent {
    private final String email;

    public SubscriptionCreatedEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}
