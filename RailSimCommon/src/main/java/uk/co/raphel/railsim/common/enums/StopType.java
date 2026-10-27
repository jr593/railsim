package uk.co.raphel.railsim.common.enums;

public enum StopType {
    INVALID("INVALID"),
    STOP("STOP"),
    PASS("PASS"),
    TERMINATE(("TERMINATE"));

    private final String description;

    StopType(String description) {
        this.description = description;
    }

    @Deprecated
    public static StopType getFromTimeTable(String timetable) {

        return switch (timetable.toUpperCase().charAt(0)) {
            case 'P' -> StopType.PASS;
            case 'T' -> StopType.TERMINATE;
            case 'S' -> StopType.STOP;
            default -> StopType.INVALID;
        };
    }

    @Override
    public String toString() {
        return description;
    }
}
