package uk.co.raphel.railsim.webapp.feed;

import com.vaadin.flow.component.UI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.raphel.railsim.common.dto.ServiceBoardEntry;
import uk.co.raphel.railsim.common.enums.MessageType;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/*
  Current state of running services as received from Railsimserver, shared by all browser sessions.
  Listeners are notified inside ui.access() so views can update components directly (pushed via @Push).
 */
@Service
@Slf4j(topic = "ServiceBoardStore")
public class ServiceBoardStore {

    private final Map<Long, ServiceBoardEntry> services = new ConcurrentHashMap<>();
    private final List<ServiceBoardListener> listeners = new CopyOnWriteArrayList<>();

    private final NatsService natsService;

    @Getter
    private volatile LocalTime simulatorTime;
    @Getter
    private volatile boolean connected;

    public ServiceBoardStore(NatsService natsService) {
        this.natsService = natsService;
    }

    public void register(ServiceBoardListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregister(ServiceBoardListener listener) {
        listeners.remove(listener);
    }

    public List<ServiceBoardEntry> getServices() {
        return services.values().stream()
                .sorted(Comparator.comparing(ServiceBoardEntry::departure,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    void applyService(MessageType type, ServiceBoardEntry entry) {
        if (entry.serviceId() == null) {
            return;
        }
        switch (type) {
            case SERVICESTART, MOVEMENT, BLOCKING -> services.put(entry.serviceId(), entry);
            // The server has finished with this service, so it leaves the board.
            case COMPLETION -> services.remove(entry.serviceId());
            default -> {
                return;
            }
        }
        notifyListeners(l -> l.onServiceUpdate(type, entry));
    }

    void applyClock(LocalTime time) {
        simulatorTime = time;
        notifyListeners(l -> l.onClockTick(time));
    }

    void setConnected(boolean connected) {
        this.connected = connected;
        if (!connected) {
            services.clear();
        }
        notifyListeners(l -> l.onConnectionChanged(connected));
    }

    private void notifyListeners(Consumer<ServiceBoardListener> action) {
        for (ServiceBoardListener listener : listeners) {
            Optional<UI> ui = listener.retrieveUI();
            if (ui.isPresent()) {
                ui.get().access(() -> action.accept(listener));
            } else {
                action.accept(listener);
            }
        }
    }
}
