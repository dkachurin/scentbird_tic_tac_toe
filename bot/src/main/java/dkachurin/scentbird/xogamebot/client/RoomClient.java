package dkachurin.scentbird.xogamebot.client;

import dkachurin.scentbird.xogamebot.model.request.CreateRoomRequest;
import dkachurin.scentbird.xogamebot.model.request.FindRoomsRequest;
import dkachurin.scentbird.xogamebot.model.request.JoinRoomRequest;
import dkachurin.scentbird.xogamebot.model.response.RoomResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomsListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "room", url="${game.server.url}")
public interface RoomClient {

    @RequestMapping(method = RequestMethod.POST, value = "/room/create")
    RoomResponse create(@RequestBody CreateRoomRequest params);

    @RequestMapping(method = RequestMethod.POST, value = "/room/join")
    RoomResponse join(@RequestBody JoinRoomRequest params);

    @RequestMapping(method = RequestMethod.POST, value = "/room/list")
    RoomsListResponse list(@RequestBody FindRoomsRequest params);

}
