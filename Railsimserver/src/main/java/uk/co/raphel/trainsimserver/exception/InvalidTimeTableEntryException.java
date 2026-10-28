package uk.co.raphel.trainsimserver.exception;

@Deprecated
public class InvalidTimeTableEntryException extends Exception {

    public InvalidTimeTableEntryException(String reason) {
        super(reason);
    }
}
