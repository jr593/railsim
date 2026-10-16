package uk.co.raphel.trainsimserver.exception;

public class InvalidTimeTableEntryException extends Exception {

    public InvalidTimeTableEntryException(String reason) {
        super(reason);
    }
}
