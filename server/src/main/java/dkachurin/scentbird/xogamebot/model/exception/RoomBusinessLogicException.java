package dkachurin.scentbird.xogamebot.model.exception;

public class RoomBusinessLogicException extends RuntimeException {

    public RoomBusinessLogicException(RoomErrorType errorType) {
        super(errorType.name());
    }

}
