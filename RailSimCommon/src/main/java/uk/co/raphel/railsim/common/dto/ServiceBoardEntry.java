package uk.co.raphel.railsim.common.dto;

import java.time.LocalTime;

/*
  Flattened view of a RunningService, sent over SSE to the web dashboard.
  Times that have not happened yet are null (RunningService uses LocalTime.MAX for these).
 */
public record ServiceBoardEntry(
        Long serviceId,
        String origin,
        LocalTime departure,
        String destination,
        LocalTime scheduledArrival,
        int currentStopNumber,
        String currentStop,
        LocalTime stopScheduledArrival,
        LocalTime stopActualArrival,
        LocalTime stopScheduledDeparture,
        LocalTime stopActualDeparture,
        String nextStop,
        LocalTime nextStopScheduledArrival,
        boolean blocked) {

    public static ServiceBoardEntry from(RunningService s) {
        RunningRouteStop[] stops = s.getRouteStops();
        int idx = s.getCurrentRouteStop();
        RunningRouteStop current = stops != null && idx < stops.length ? stops[idx] : null;
        RunningRouteStop next = stops != null && idx + 1 < stops.length ? stops[idx + 1] : null;

        return new ServiceBoardEntry(
                s.getServiceId(),
                s.getOrigin(),
                s.getStartTime(),
                s.getDestination(),
                s.getScheduledArrivalTime(),
                idx + 1,
                current == null || current.getBerth() == null ? null : current.getBerth().getBerthName(),
                current == null ? null : happened(current.getScheduledArrivalTime()),
                current == null ? null : happened(current.getActualArrivalTime()),
                current == null ? null : happened(current.getScheduledDepartureTime()),
                current == null ? null : happened(current.getActualDepartureTime()),
                next == null || next.getBerth() == null ? null : next.getBerth().getBerthName(),
                next == null ? null : happened(next.getScheduledArrivalTime()),
                s.isBlocked());
    }

    private static LocalTime happened(LocalTime t) {
        return LocalTime.MAX.equals(t) ? null : t;
    }
}
