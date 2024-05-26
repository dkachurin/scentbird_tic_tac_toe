const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/ws-api'
});
stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        // $("#connected").show();
    }
    else {
        // $("#disconnect").hide();
    }
}

function connectToServer() {
    stompClient.activate();
    stompClient.onConnect = (frame) => {
        setConnected(true);
        console.log('Connected: ' + frame);
        let searchParams = new URLSearchParams(window.location.search)
        if (searchParams.has('id')) {
            let gameId = searchParams.get('id')
            $.ajax({
                type:"POST",
                url:"http://localhost:8080/game/find",
                data: JSON.stringify({"id":gameId}),
                contentType: 'application/json',
                success: function(payload) {
                    initialState(payload.roomId)
                    let topicName = '/topic/game-state-updated-' + payload.roomId;
                    stompClient.subscribe(topicName, (payload) => {
                        updatePlayingArea(JSON.parse(payload.body).playingArea);
                    });
                }.bind(this),
                error: function(xhr, status, err) {}.bind(this)
            });
        } else {
            $.ajax({
                type: "POST",
                url: "http://localhost:8080/room/list",
                data: "{ \"statuses\": [\"IN_PROGRESS\", \"DONE\"] }",
                contentType: 'application/json',
                success: function (response) {
                    let roomId = response.rooms[0].id;
                    initialState(roomId)
                    let topicName = '/topic/game-state-updated-' + roomId;
                    stompClient.subscribe(topicName, (payload) => {
                        updatePlayingArea(JSON.parse(payload.body).playingArea);
                    });
                }.bind(this),
                error: function (xhr, status, err) {
                }.bind(this)
            });
        }
    };
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function initialState(roomId) {
    $.ajax({
        type:"POST",
        url:"http://localhost:8080/game/find",
        data: JSON.stringify({"id":roomId}),
        contentType: 'application/json',
        success: function(gameState) { updatePlayingArea(gameState.cells.state) }.bind(this),
        error: function(xhr, status, err) { }.bind(this)
    });
}

function markTypeToValue(markType) {
    if (markType === "NONE") {
        return ""
    } else if (markType === "CROSS") {
        return "X"
    } else if (markType === "ZERO") {
        return "O"
    }
}

function markTypeToGreen(markType) {
    console.log('win mark');
    if (markType === "NONE") {
        return ""
    } else if (markType === "CROSS") {
        return "X"
    } else if (markType === "ZERO") {
        return "O"
    }
}

function updatePlayingArea(stateMap) {
    let playingAreaKeyList = ["A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"];
    for (const playingAreaKey of playingAreaKeyList) {
        let value = stateMap[playingAreaKey].toUpperCase();
        $("#playing-area-" + playingAreaKey.toLowerCase()).html(markTypeToValue(value));
    }
    checkIfSomebodyWin(stateMap);
}

function checkIfSomebodyWin(stateMap) {
    let cellChars = ["A", "B", "C"];
    for (const cell of cellChars) {
        let cell1 = cell + "1";
        let cell2 = cell + "2";
        let cell3 = cell + "3";
        if (stateMap[cell1] !== "NONE" && stateMap[cell1] === stateMap[cell2] && stateMap[cell2] === stateMap[cell3]) {
            let html = "<div class=\"green\">" + markTypeToGreen(stateMap[cell1]) + "</div>"
            $("#playing-area-" + cell1.toLowerCase()).html(html);
            $("#playing-area-" + cell2.toLowerCase()).html(html);
            $("#playing-area-" + cell3.toLowerCase()).html(html);
            console.log('win 1');
            return
        }
    }

    let cellDigits = ["1", "2", "3"];
    for (const digit of cellDigits) {
        let cell1 = "A" + digit;
        let cell2 = "B" + digit;
        let cell3 = "C" + digit;
        if (stateMap[cell1] !== "NONE" && stateMap[cell1] === stateMap[cell2] && stateMap[cell2] === stateMap[cell3]) {
            let html = "<div class=\"green\">" + markTypeToGreen(stateMap[cell1]) + "</div>"
            $("#playing-area-" + cell1.toLowerCase()).html(html);
            $("#playing-area-" + cell2.toLowerCase()).html(html);
            $("#playing-area-" + cell3.toLowerCase()).html(html);
            console.log('win 2');
            return
        }
    }

    let a1 = stateMap["A1"];
    let b2 = stateMap["B2"];
    let c3 = stateMap["C3"];
    if (a1 !== "NONE" && a1 === b2 && b2 === c3) {
        let html = "<div class=\"green\">" + markTypeToGreen(a1) + "</div>"
        $("#playing-area-a1").html(html);
        $("#playing-area-b2").html(html);
        $("#playing-area-c3").html(html);
        console.log('win 3');
        return
    }

    let a3 = stateMap["A3"];
    let c1 = stateMap["C1"];
    if (a3 !== "NONE" && a3 === b2 && b2 === c1) {
        let html = "<div class=\"green\">" + markTypeToGreen(a3) + "</div>"
        $("#playing-area-a3").html(html);
        $("#playing-area-b2").html(html);
        $("#playing-area-c1").html(html);
        console.log('win 4');
        return;
    }
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connectToServer());
    $( "#disconnect" ).click(() => disconnect());

    let searchParams = new URLSearchParams(window.location.search)
    if (searchParams.has('id')) {
        let gameId = searchParams.get('id')
        $.ajax({
            type:"POST",
            url:"http://localhost:8080/game/find",
            data: JSON.stringify({"id":gameId}),
            contentType: 'application/json',
            success: function(payload) { initialState(payload.roomId) }.bind(this),
            error: function(xhr, status, err) {}.bind(this)
        });
    } else {
        $.ajax({
            type:"POST",
            url:"http://localhost:8080/room/list",
            data: "{ \"statuses\": [\"IN_PROGRESS\", \"DONE\"] }",
            contentType: 'application/json',
            success: function(response) {
                let roomId = response.rooms[0].id;
                initialState(roomId)
            }.bind(this),
            error: function(xhr, status, err) { }.bind(this)
        });
    }
});
