# Habit Tracker

Приложение для отслеживания привычек с детальной кастомизацией и глубокой аналитикой. Доступно на iOS и в браузере. Дизайн и UX вдохновлены книгой "Атомные привычки" Джеймса Клира.

## Возможности

- Создание привычек с RGB-палитрой, иконками и категориями
- Отметка выполнения в один тап с анимациями
- Streak-система с freeze days
- Календарь выполнения и годовой heatmap
- Детальная аналитика: тренды, radar chart, лучшие/худшие дни
- Фичи из "Атомных привычек": правило 2 минут, habit stacking, идентичность
- Milestone-достижения (7, 21, 30, 66, 100 дней)
- Push-уведомления и мотивация
- Синхронизация данных между iOS и Web

## Стек технологий

| Слой | Технология |
|------|-----------|
| iOS | React Native (Expo), TypeScript |
| Web | React (React Native Web), TypeScript |
| Backend | Java, Spring Boot |
| БД | PostgreSQL (H2 для разработки) |
| ORM | Spring Data JPA, Hibernate |
| Auth | Spring Security, JWT, OAuth2 (Google) |
| Миграции | Flyway |
| Хостинг | Render (backend), Vercel (web) |

## Быстрый старт

### Backend

```bash
cd backend
./gradlew bootRun
```

API доступен на `http://localhost:8080`. H2 консоль: `http://localhost:8080/h2-console`.

### API-эндпоинты

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Вход |
| GET | `/api/habits` | Список привычек |
| POST | `/api/habits` | Создать привычку |
| POST | `/api/habits/{id}/complete` | Отметить выполнение |
| GET | `/api/habits/{id}/stats` | Статистика привычки |
| GET | `/api/habits/{id}/heatmap` | Данные heatmap |
| GET | `/api/achievements` | Достижения |

## Структура проекта

```
tracker/
├── backend/                 # Spring Boot API
│   ├── src/main/java/com/habittracker/
│   │   ├── controller/      # REST-контроллеры
│   │   ├── service/         # Бизнес-логика
│   │   ├── repository/      # JPA-репозитории
│   │   ├── model/           # Сущности
│   │   ├── dto/             # Request/Response DTO
│   │   ├── security/        # JWT
│   │   ├── config/          # Spring конфигурация
│   │   └── exception/       # Обработка ошибок
│   └── src/main/resources/
│       ├── db/migration/    # Flyway-миграции
│       └── application*.properties
└── Проект/                  # Документация (Obsidian)
    ├── Roadmap.md
    ├── Техзадания/ТЗ.md
    ├── Документация/
    └── Архитектура/
```

## Статус разработки

- [x] Определить требования (ТЗ)
- [x] Выбрать стек технологий
- [x] Спроектировать архитектуру
- [x] Backend: модели, JWT-авторизация, CRUD привычек
- [x] Backend: streak, аналитика, достижения
- [ ] Frontend: React Native (Expo)
- [ ] Деплой на Render + Vercel

## Документация

Подробная документация в папке [Проект/](Проект/):

- [Техническое задание](Проект/Техзадания/ТЗ.md)
- [Стек технологий](Проект/Документация/Стек%20технологий.md)
- [Обзор архитектуры](Проект/Архитектура/Обзор%20архитектуры.md)
- [Идеи из Атомных привычек](Проект/Документация/Идеи%20из%20Атомных%20привычек.md)
- [Сравнение хостингов](Проект/Документация/Сравнение%20хостингов.md)
- [Push-уведомления](Проект/Документация/Push-уведомления.md)
- [Roadmap](Проект/Roadmap.md)
