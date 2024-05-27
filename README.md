### How to run

#### Pre-requirements
- `Docker` must be installed before run application

#### Steps to do
- Run `runme.sh` and wait `http://localhost:8080/index.html` to be opened
- Or run application via `docker compose` and open url in browser manually

#### NOTE
- Solution is not finished yet, it's draft. Must be finished till 1 June 2024.

### Application logic short description

#### How to check game state
- You may click `Connect` in opened page to establish web sockets connection for realtime updates
- Also, you may update page manually by clicking `refresh` in browser

#### UI logic description
- ui сейчас запрашивает список существующих активных игр, если такие есть, то отображает состояние активной игры
- так же на ui можно указывать параметр  http://localhost:8080/index.html?id=7777_roomId_7777, тогда будет отображаться состояние конкретной игры

#### Server logic description
- есть РЕСТ АПИ для создания комнат и присоединения к ним
- есть РЕСТ АПИ для совершения хода
- есть WebSockets нотификации, что бы уведомлять подписанных по вебсокетам на топик по игре о апдейтах по игре
- данные хранятся в In Memory SQL БД

#### Bot logic description
- при старте приложения можно указать параметр startRobotsCount, который будет означать сколько игровых ботов сразу запустить
- игровые боты присоединяются к доступным играм и играют в них, а если доступных игр нет, то создают новую игру и присоединяются к ней
- игровая логика бота: с некоторой периодичностью бот запрашивает состояние сервера и в зависимости от состояния сервера делает ход
- есть АПИ метод player_bot/start чтобы запустить еще одного игрового бота
