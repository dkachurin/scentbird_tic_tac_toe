package dkachurin.scentbird.xogamebot.api;

import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.PlayerActionRequest;
import dkachurin.scentbird.xogamebot.model.request.FindByIdRequest;
import dkachurin.scentbird.xogamebot.model.response.GameResponse;
import dkachurin.scentbird.xogamebot.service.GameService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(final GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/action")
    @ResponseBody
    public String action(@RequestBody final PlayerActionRequest playerActionRequest) {
        gameService.processPlayerAction(
                playerActionRequest.gameId(),
                playerActionRequest.secret(),
                playerActionRequest.playerId(),
                playerActionRequest.playingArea()
        );
        return "OK";
    }

    @PostMapping("/find")
    @ResponseBody
    public GameResponse find(@RequestBody final FindByIdRequest param) {
        return gameService.findGameByRoomId(UUID.fromString(param.id()));
    }

}
