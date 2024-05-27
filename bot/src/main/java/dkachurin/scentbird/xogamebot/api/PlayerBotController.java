package dkachurin.scentbird.xogamebot.api;

import dkachurin.scentbird.xogamebot.model.response.OkResponse;
import dkachurin.scentbird.xogamebot.service.PlayerBotService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/player_bot")
public class PlayerBotController {

    private final PlayerBotService playerBotService;

    @Autowired
    public PlayerBotController(final PlayerBotService playerBotService) {
        this.playerBotService = playerBotService;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/start")
    @ResponseBody
    public OkResponse start() {
        final UUID roomId = playerBotService.startPlayerBot();
        return new OkResponse("OK", roomId.toString());
    }

}
