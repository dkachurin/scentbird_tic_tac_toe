package dkachurin.scentbird.xogamebot.client;

import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.PlayerActionRequest;
import dkachurin.scentbird.xogamebot.model.request.FindByIdRequest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "game", url="${game.server.url}")
public interface GameClient {

    @RequestMapping(method = RequestMethod.POST, value = "/game/action")
    String action(@RequestBody final PlayerActionRequest playerActionRequest);

    @RequestMapping(method = RequestMethod.POST, value = "/game/find")
    Game find(@RequestBody final FindByIdRequest param);
}
