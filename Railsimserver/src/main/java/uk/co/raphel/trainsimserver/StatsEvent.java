package uk.co.raphel.trainsimserver;

import org.springframework.context.ApplicationEvent;

public class StatsEvent extends ApplicationEvent {
    public StatsEvent(Object source) {
        super(source);
    }
}
