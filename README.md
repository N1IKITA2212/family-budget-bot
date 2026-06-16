# 💰 Family Budget Bot

Telegram-бот для ведения семейного бюджета через Google Sheets. Позволяет записывать расходы и доходы прямо из Telegram, получать статистику за любой месяц и просматривать данные в красиво оформленной Google таблице.

## ✨ Функционал

- 💸 **Запись расходов** — выбор категории из таблицы, ввод суммы и комментария
- 💚 **Запись доходов** — аналогично для доходов
- 📊 **Статистика** — сводка за любой месяц: бюджет, остаток, расходы по категориям, доходы, баланс
- 🔄 **Обновление категорий** — команда `/reload` обновляет список категорий из таблицы без перезапуска
- ⏰ **Ежедневные напоминания** — напоминание записать траты каждый вечер
- 🔒 **Whitelist авторизация** — бот отвечает только разрешённым пользователям

## 🛠 Стек технологий

| Слой | Технология |
|------|-----------|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.x |
| Telegram API | TelegramBots 7.x (Long Polling) |
| Хранилище | Google Sheets API v4 |
| Авторизация | Service Account (Google Cloud) |
| Планировщик | Spring Scheduler |
| Утилиты | Lombok |
| Инфраструктура | Docker, Docker Compose |

## 🏗 Архитектура

```
Telegram User
     ↓
FamilyBudgetBot (Long Polling)
     ↓
CommandHandler (Strategy Pattern)
     ├── StartHandler       — /start
     ├── ExpenseHandler     — запись расходов
     ├── IncomeHandler      — запись доходов
     ├── StatsHandler       — статистика
     └── ReloadHandler      — /reload
     ↓
Service Layer
     ├── SessionService     — состояние диалога (ConcurrentHashMap)
     ├── CategoryService    — кеш категорий (@PostConstruct)
     ├── GoogleSheetsService — работа с Sheets API
     ├── StatsService       — формирование статистики
     ├── MessageService     — отправка сообщений
     └── ReminderService    — @Scheduled напоминания
     ↓
Google Sheets API
     ├── 📝 Журнал трат
     ├── 💚 Журнал доходов
     └── 01_Январь ... 12_Декабрь (агрегация через формулы)
```

## 🚀 Запуск локально

### Предварительные требования

- Java 21
- Maven 3.9+
- Docker и Docker Compose
- Аккаунт Google Cloud с включённым Sheets API
- Telegram Bot Token (от @BotFather)

### 1. Клонируй репозиторий

```bash
git clone https://github.com/N1IKITA2212/family-budget-bot.git
cd family-budget-bot
```

### 2. Настрой Google Sheets

1. Создай проект в [Google Cloud Console](https://console.cloud.google.com)
2. Включи **Google Sheets API**
3. Создай **Service Account** и скачай JSON ключ
4. Переименуй файл в `credentials.json` и положи в корень проекта
5. Поделись своей Google таблицей с email Service Account (роль — Редактор)

### 3. Создай `.env` файл

```env
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_USERNAME=your_bot_username
TELEGRAM_USER_ID_NIKITA=your_telegram_id
TELEGRAM_USER_ID_LIZA=wife_telegram_id

GOOGLE_CREDENTIALS_PATH=/app/credentials.json
GOOGLE_SPREADSHEET_ID=your_spreadsheet_id
```

> Узнать Telegram ID можно через @userinfobot

### 4. Запусти через Docker Compose

```bash
mvn clean package -DskipTests
docker-compose up --build
```

## 🌐 Деплой на VPS

### 1. Собери и запушь образ на Docker Hub

```bash
docker build -t n1ikita23/family-budget-bot:latest .
docker push n1ikita23/family-budget-bot:latest
```

### 2. На сервере

```bash
mkdir ~/family-budget-bot && cd ~/family-budget-bot

# Скопируй с локальной машины
scp docker-compose.yml root@YOUR_SERVER_IP:/root/family-budget-bot/
scp .env root@YOUR_SERVER_IP:/root/family-budget-bot/
scp credentials.json root@YOUR_SERVER_IP:/root/family-budget-bot/

# Запусти
docker-compose up -d
```

### docker-compose.yml для продакшна

```yaml
services:
  app:
    image: n1ikita23/family-budget-bot:latest
    env_file: .env
    volumes:
      - ./credentials.json:/app/credentials.json
    restart: unless-stopped
```

## 📁 Структура проекта

```
src/main/java/com/example/familybudgetbot/
├── bot/
│   ├── FamilyBudgetBot.java
│   └── handler/
│       ├── CommandHandler.java      # Interface
│       ├── StartHandler.java
│       ├── ExpenseHandler.java
│       ├── IncomeHandler.java
│       ├── StatsHandler.java
│       └── ReloadHandler.java
├── config/
│   ├── TelegramConfig.java
│   ├── TelegramClientConfig.java
│   └── GoogleSheetsConfig.java
├── service/
│   ├── SessionService.java
│   ├── CategoryService.java
│   ├── GoogleSheetsService.java
│   ├── StatsService.java
│   ├── MessageService.java
│   ├── ReminderService.java
│   ├── UserSession.java
│   └── UserState.java
└── exception/
    └── GoogleSheetsException.java
```

## 💡 Паттерны и решения

- **Strategy Pattern** — каждая команда бота реализует `CommandHandler`. Spring автоматически инжектирует все реализации как `List<CommandHandler>`
- **Stateful диалог** — `UserState` enum + `ConcurrentHashMap` для хранения состояния каждого пользователя
- **Кеширование категорий** — `@PostConstruct` загружает категории при старте, `/reload` обновляет без перезапуска
- **Whitelist авторизация** — `allowedUserIds` в конфиге, все остальные сообщения игнорируются
