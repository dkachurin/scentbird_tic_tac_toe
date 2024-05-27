const gameServerAddress = "localhost:8080"
const wsGameServerBrokerURL = 'ws://' + gameServerAddress + '/ws-api';
const gameBotServerAddress = "localhost:8083"

const stompClient = new StompJs.Client({
    brokerURL: wsGameServerBrokerURL
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);

    let searchParams = new URLSearchParams(window.location.search)
    if (searchParams.has('id')) {
        let gameId = searchParams.get('id')
        let topicName = '/topic/game-state-updated-' + gameId;
        stompClient.subscribe(topicName, (payload) => {
            let parsedPayload = JSON.parse(payload.body);
            refreshGameView(parsedPayload.playingArea, parsedPayload.winingCells)
        });
    }
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);

    showError("Error: " + error)
    disconnect()
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);

    showError("Error: " + frame.headers['message'] + ";\nDetails: " + frame.body)
    disconnect()
};

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
}

function connectToWebSockets() {
    stompClient.activate();
}

function showError(err) {
    $("#errors").html("<div class='errors'>Last occurred error: " + err + "</div>");
    console.log('Err: ' + err);
}

//markType: enum[NONE, CROSS, ZERO]
function markTypeToValue(markType, green) {
    if (markType === "NONE") {
        return ""
    } else if (markType === "CROSS") {
        return green ? "<div class=\"green\">X</div>" : "X"
    } else if (markType === "ZERO") {
        return green ? "<div class=\"green\">O</div>" : "O"
    }
    console.log('Error, unknown mark type: ' + markType);
    return "?"
}

// allCellsMap: Map<String, MarkType>
// winningCells: List<String>
function refreshGameView(allCellsMap, winningCells) {
    let playingAreaKeyList = ["A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"];

    for (const playingAreaKey of playingAreaKeyList) {
        let markType = allCellsMap[playingAreaKey];
        let green = winningCells.includes(playingAreaKey);
        let cellValue = markTypeToValue(markType, green);
        $("#playing-area-" + playingAreaKey.toLowerCase()).html(cellValue);
    }
}

function initParticularGamePageState(gameId) {
    $.ajax({
        type:"POST",
        url: "http://" + gameServerAddress + "/game/find",
        data: JSON.stringify({"id":gameId}),
        contentType: 'application/json',
        success: function(game) { refreshGameView(game.cells.state, game.winningCells) }.bind(this),
        error: function(xhr, status, err) { showError(err) }.bind(this)
    });
}

function addOneMoreBot() {
    $.ajax({
        type:"POST",
        url: "http://" + gameBotServerAddress + "/player_bot/start",
        contentType: 'application/json',
        success: function() { initListAvailableGamesPageState() }.bind(this),
        error: function(xhr, status, err) { showError(err) }.bind(this)
    });
}

function refreshAvailableRoomsList(roomsList) {
    let hasElements = roomsList && roomsList.length
    let availableGamesSelector = $("#available-games-list");
    if (hasElements) {
        function generateTable(rooms) {
            let table = '<table class="available-games-table">';
            table += '<thead><tr><th>Room Name</th><th>Created At</th><th>Status</th><th>Link</th></tr></thead>';
            table += '<tbody>';
            rooms.forEach(room => {
                const url = `http://${gameServerAddress}/index.html?id=${room.id}`;
                table += `<tr>
                        <td>${room.roomName}</td>
                        <td>${room.createdAt}</td>
                        <td>${room.status}</td>
                        <td><a href="${url}">Join</a></td>
                      </tr>`;
            });
            table += '</tbody></table>';
            return table;
        }

        $("#available-games-list").html(generateTable(roomsList));
    } else {
        let zeroStateMessage = "No active games yet, you can click 'Add bot' to initiate more games.";
        availableGamesSelector.html(zeroStateMessage);
    }
}

function initListAvailableGamesPageState() {
    $.ajax({
        type:"POST",
        url:"http://" + gameServerAddress + "/room/list",
        data: "{ \"statuses\": [\"IN_PROGRESS\", \"DONE\"] }",
        contentType: 'application/json',
        success: function(response) { refreshAvailableRoomsList(response.rooms) }.bind(this),
        error: function(xhr, status, err) { showError(err) }.bind(this)
    });
}

function init() {
    let searchParams = new URLSearchParams(window.location.search)
    if (searchParams.has('id')) {
        $("#available-games-list").remove();
        $("#add-bots-container").remove();

        $("#connect" ).click(() => connectToWebSockets());
        $("#disconnect" ).click(() => disconnect());

        let gameId = searchParams.get('id')
        initParticularGamePageState(gameId)
    } else {
        $("#active-game-container").remove();

        $("#add-one-more-bot" ).click(() => addOneMoreBot());

        initListAvailableGamesPageState()
    }
}
$(document).ready(function() {
    init()
});