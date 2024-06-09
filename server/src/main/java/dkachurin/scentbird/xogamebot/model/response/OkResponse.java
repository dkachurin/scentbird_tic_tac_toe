package dkachurin.scentbird.xogamebot.model.response;

public record OkResponse(String message) {
    public OkResponse() {
        this("OK");
    }
}