package dkachurin.scentbird.xogamebot.api;

import dkachurin.scentbird.xogamebot.model.request.CreateRoomRequest;
import dkachurin.scentbird.xogamebot.model.request.FindRoomsRequest;
import dkachurin.scentbird.xogamebot.model.request.JoinRoomRequest;
import dkachurin.scentbird.xogamebot.model.response.RoomResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomsListResponse;
import dkachurin.scentbird.xogamebot.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(final RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    @ResponseBody
    public RoomResponse create(@RequestBody final CreateRoomRequest params) {
        return roomService.createRoom(params.title());
    }

    @PostMapping("/join")
    @ResponseBody
    public RoomResponse join(@RequestBody final JoinRoomRequest params) {
        return roomService.joinRoom(params.roomId(), params.playerId(), params.markType());
    }

    @PostMapping("/list")
    @ResponseBody
    public RoomsListResponse list(@RequestBody final FindRoomsRequest params) {
        return roomService.findRoomsList(params.statuses());
    }

}
