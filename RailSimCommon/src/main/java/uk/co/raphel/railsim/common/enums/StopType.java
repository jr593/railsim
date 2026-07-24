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

    public static StopType getFromTimeTable(String timetable) {
        switch(timetable.toUpperCase().charAt(0)) {
            case 'P': return StopType.PASS;
            case 'T': return StopType.TERMINATE;
            case 'S': return StopType.STOP;
            default: return StopType.INVALID;
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
