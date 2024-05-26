package dkachurin.scentbird.xogamebot.model.exception;

public class GameBusinessLogicException extends RuntimeException {

    public GameBusinessLogicException(final GameErrorType errorType) {
        super(errorType.name());
    }

}
