### How to run

#### Pre-requirements
- `Docker` must be installed before run application

#### Steps to do
- Run `runme.sh` and wait `http://localhost:8080/index.html` to be opened
- Or run application via `docker compose` and open url in browser manually

#### NOTE
- If you see error messages in `index.html`, ensure both `server` and `bot` docker containers started. 
- `bot` module is not finished, need to add logs and add unit tests

### Requirements and answers
- Приложение должно быть разработано на JVM языке.
  - done: used java 21
- Можно использовать любые технологии на выбор разработчика.
  - done: used simple for setup technologies for app  
- Приложение должно быть разработано с учетом лучших практик написания кода: форматирование, тестирование, комментарии по необходимости.
  - done: formatted code and add tests in `server` module
- Соединение между инстансами может быть в любой момент разорвано и восстановлено.
  - done: client and server modules separated, you can reconnect server WS any time
- Приложение должно иметь интерфейс (REST или HTML), который позволяет пользователю получить состояние игрового поля в любой момент.
  - done: simple html UI accessible using `index.html` from `server` app
- В любой момент оба инстанса должны показывать одинаковое состояние игрового поля или явно указывать что состояние неконсистентно, в независимости от состояния соединения между инстансами.
  - done: `server` instances are independent, client can show 'not synced' warning if there are connection problems
- Необходимо предусмотреть задержку, чтобы можно было отслеживать ходы инстансов.
  - done: `bot` module used random delay to be able to see actions
- Приложение должно делать ходы в соответствии с правилами.
  - done: `server` validates action correctness. `bot` also do allowed actions 
- Приложение не должно позволять другому инстансу совершать ходы не по правилам.
  - done: `server` validates action correctness.
- Можно выбрать любую другую пошаговую игру с 2мя или более игроками: морской бой, шахматы, палочки (каждый игрок отнимает из кучи 1,2,3 палочки, кто взял последнюю тот проиграл)
  - done: not actual for me
- Алгоритм игры не важен, можно использовать рандомную стратегию. Важно, как происходит синхронизация между инстансами.
  - done: used random for actions of bots

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
