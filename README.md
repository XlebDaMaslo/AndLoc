# Geolocation WebSocket App

## Описание

Geolocation WebSocket App — это Android-приложение, которое использует геолокацию устройства и отправляет данные о местоположении через WebSocket-соединение на сервер. Приложение построено с использованием Jetpack Compose для интерфейса пользователя и OkHttp для работы с WebSocket.

## Функциональные возможности

- Получение текущей геолокации устройства (широта и долгота).
- Отправка данных о местоположении (RSRP, широта и долгота) на сервер через WebSocket.

## Структура проекта

- **MainActivity.kt**: Главный класс приложения, который управляет жизненным циклом и инициализирует компоненты.
- **LocationAct.kt**: Класс, отвечающий за получение геолокации устройства.
- **WebSocketAct.kt**: Класс, управляющий WebSocket-соединением и отправляющий данные на сервер.
- **Interface.kt**: Компонент пользовательского интерфейса, отображающий данные о местоположении и кнопку для отправки этих данных.
