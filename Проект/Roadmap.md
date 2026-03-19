# Roadmap — Habit Tracker

> Этот документ отражает текущий прогресс разработки. Обновляется по мере продвижения.
> Последнее обновление: 2026-03-19 (анимации: pulse checkbox + Perfect Day! баннер)

---

## Фаза 1 — Планирование ✅

- [x] Сформулировать цели проекта
- [x] Определить целевую аудиторию
- [x] Составить техническое задание → [[Техзадания/ТЗ]]
- [x] Выбрать стек технологий → [[Документация/Стек технологий]]
- [x] Проанализировать конкурентов и источники идей
- [x] Анализ книги "Атомные привычки" → [[Документация/Идеи из Атомных привычек]]
- [x] Сравнить варианты хостинга → [[Документация/Сравнение хостингов]]
- [x] Разобраться с push-уведомлениями → [[Документация/Push-уведомления]]

---

## Фаза 2 — Проектирование ✅

- [x] Спроектировать архитектуру (backend + frontend + БД) → [[Архитектура/Обзор архитектуры]]
- [x] Описать REST API (22 эндпоинта)
- [x] Спроектировать схему БД (7 таблиц)
- [x] Определить пакетную структуру backend
- [x] Определить структуру frontend (Expo Router)
- [ ] Создать макеты UI / wireframes

---

## Фаза 3 — Backend (Spring Boot) ✅

### 3.1 Инфраструктура ✅
- [x] Инициализировать Git-репозиторий
- [x] Создать Spring Boot проект (Gradle, Java 24)
- [x] Настроить зависимости (JPA, Security, Flyway, JWT, Lombok, H2)
- [x] Настроить профили (dev — H2, prod — PostgreSQL)
- [x] Настроить CORS

### 3.2 Модели и БД ✅
- [x] JPA-сущности: User, Habit, HabitCompletion, FreezeDay, Achievement
- [x] Flyway-миграция V1 (создание всех таблиц)
- [x] JPA-репозитории (5 штук) с кастомными запросами

### 3.3 Авторизация ✅
- [x] JWT Token Provider (access + refresh токены)
- [x] JWT Auth Filter
- [x] Spring Security конфигурация (stateless, JWT)
- [x] AuthService: регистрация, логин, refresh
- [x] AuthController: POST /api/auth/register, login, refresh
- [ ] OAuth2 Google (вход через Google)
- [x] Восстановление пароля (reset-request + reset по токену)

### 3.4 CRUD привычек ✅
- [x] HabitService: create, read, update, delete, archive, reorder
- [x] HabitController: все CRUD-эндпоинты
- [x] Валидация: RGB hex-код цвета, обязательные поля
- [x] Поддержка полей из "Атомных привычек" (identityText, miniVersion)

### 3.5 Отметка выполнения ✅
- [x] CompletionService: complete, uncomplete, getCompletions
- [x] CompletionController
- [x] Проверка дубликатов (нельзя отметить дважды)
- [x] Backfill (отметка за прошлые дни)

### 3.6 Streak и аналитика ✅
- [x] StreakService: currentStreak, bestStreak (с учётом freeze days)
- [x] AnalyticsService: stats (%, дни недели), heatmap
- [x] AnalyticsController: GET /stats, /heatmap
- [x] Dashboard API: GET /api/analytics/dashboard (общая аналитика, perfect days, top/attention habits)
- [x] Trends API: GET /api/analytics/trends (сравнение периодов, +1% метрика, daily trend)

### 3.7 Достижения ✅
- [x] AchievementService: milestone-проверки (7, 21, 30, 66, 100, 365 дней)
- [x] Perfect Day проверка
- [x] GET /api/achievements

### 3.8 Freeze Days ✅
- [x] FreezeDayService: freeze, getFreezeDays, getRemainingFreezes
- [x] FreezeDayController: POST /api/habits/{id}/freeze
- [x] Лимит freeze days — 2 в неделю (Пн–Вс)
- [x] GET /api/freeze-days (список + remainingThisWeek)
- [x] Валидация: нельзя заморозить будущий день, проверка дубликатов

### 3.9 Цепочки привычек (Habit Stacking) ✅
- [x] Модели: HabitChain, HabitChainItem (JPA + cascade)
- [x] Flyway-миграция V2 (таблицы chains + chain_items + password reset columns)
- [x] HabitChainRepository
- [x] ChainService: CRUD цепочек с валидацией привычек пользователя
- [x] ChainController: GET/POST/PUT/DELETE /api/chains
- [x] Логика порядка привычек в цепочке (position)

### 3.10 Тестирование ✅
- [x] Юнит-тесты сервисов (AuthService, HabitService, CompletionService, StreakService, AchievementService, AnalyticsService)
- [x] Юнит-тесты Security (JwtTokenProvider, JwtAuthFilter)
- [x] Юнит-тесты DTO (UserResponse, HabitResponse, AchievementResponse)
- [x] Юнит-тесты контроллеров (AuthController — WebMvcTest)
- [x] Cucumber интеграционные тесты (auth, habits, completions, analytics)
- [x] Cucumber E2E тесты (полный путь пользователя, изоляция данных)
- [x] Все 156 тестов зелёные (было 115, добавлено 41)
- [x] Unit-тесты: FreezeDayService (5 тестов), ChainService (7 тестов), Dashboard+Trends (5 тестов), AuthService password reset (6 тестов)
- [x] Cucumber: freeze_days.feature (4 сценария), chains.feature (4 сценария), dashboard.feature (5 сценариев — dashboard, trends, password reset)

---

## Фаза 4 — Frontend (React Native + Expo) 🔄

### 4.1 Инфраструктура ✅
- [x] Инициализировать Expo-проект (TypeScript, tabs template)
- [x] Настроить Expo Router (file-based routing)
- [x] Настроить Zustand (useAuthStore, useHabitStore)
- [x] Настроить Axios + interceptors (JWT auto-attach, auto-refresh)
- [x] Настроить TanStack Query (QueryClientProvider)
- [x] Настроить тему (Colors.ts: primary, light/dark palette)
- [x] Web export build проходит без ошибок
- [x] TypeScript — 0 ошибок

### 4.2 Авторизация ✅
- [x] Экран входа (email + пароль)
- [x] Экран регистрации
- [x] Хранение JWT в SecureStore
- [x] Auto-refresh токена (Axios interceptor)
- [x] AuthGate (автоматический redirect на login/tabs)
- [ ] Google OAuth

### 4.3 Главный экран ✅
- [x] Список привычек на сегодня (FlatList)
- [x] Чекбокс — отметка в один тап (optimistic update)
- [x] Микроанимация при отметке (pulse в цвете привычки)
- [x] Прогресс-бар дня ("5 из 7")
- [x] Анимация "Perfect Day!"
- [x] Streak badge возле каждой привычки (🔥)
- [x] FAB-кнопка "+" для создания
- [x] Empty state
- [x] HabitMonthStrip — мини-календарь месяца на карточке привычки (% выполнения)

### 4.4 Создание / редактирование привычки ✅
- [x] Форма создания: название, описание, цвет, частота, identity, mini version
- [x] Форма редактирования (app/habit/edit.tsx) — с подгрузкой данных
- [x] RGB Color Picker (полный спектр: presets + RGB inputs + hex)
- [ ] Выбор иконки
- [ ] Категория
- [x] Частота (Daily / Weekly / Custom)
- [ ] Время напоминания
- [x] Поле "Кем я хочу стать?" (идентичность)
- [x] Поле "Мини-версия" (правило 2 минут)

### 4.5 Детальный экран привычки ✅
- [x] Месячный календарь с отметками (MonthCalendar)
- [x] Streak: текущий + лучший
- [ ] Fire-анимация streak
- [ ] Годовой heatmap (стиль GitHub)
- [x] Цвет = цвет привычки
- [x] Статистика (% за неделю / месяц)
- [x] Bar chart по дням недели
- [x] Идентичность-карточка
- [x] Отметка/снятие прямо из календаря

### 4.6 Аналитика ✅ (MVP)
- [x] Статистика привычки (%, лучшие дни) — в детальном экране
- [x] Bar chart — по дням недели — в детальном экране и analytics
- [x] Общий dashboard — экран Analytics с Summary, Trends, Top/Needs Attention
- [x] Достижения — grid в экране Analytics
- [ ] Line chart — тренд по дням (расширенный, с осями)
- [ ] Radar chart — профиль дисциплины
- [ ] Progress rings

### 4.7 Уведомления
- [ ] expo-notifications — локальные напоминания
- [ ] Настройка времени напоминания
- [ ] Milestone-уведомления (7, 21, 30 дней)
- [ ] Утренний / вечерний push

### 4.8 Дополнительно
- [x] Онбординг (приветствие — 3 слайда, Skip, Get Started)
- [ ] Экран достижений (отдельный)
- [ ] Drag & drop сортировка
- [ ] Тёмная / светлая тема
- [ ] Звуковые эффекты (с toggle)

### 4.9 Web-версия
- [ ] React Native Web — адаптация под браузер
- [ ] Responsive layout
- [ ] Деплой на Vercel

---

## Фаза 5 — Деплой и запуск

- [ ] Настроить PostgreSQL на Render
- [ ] Деплой backend на Render
- [ ] Переменные окружения (DATABASE_URL, JWT_SECRET, CORS_ORIGINS)
- [ ] Деплой web на Vercel
- [ ] Сборка iOS через Expo EAS Build
- [ ] Тестирование на реальном устройстве
- [ ] Apple Developer Account ($99/год)
- [ ] Публикация в App Store

---

## Фаза 6 — Post-MVP (v1.1+)

- [ ] **OAuth2 Google** — бэкенд: Spring Security OAuth2 Client, Google Cloud Console credentials, эндпоинт `POST /api/auth/google`; фронтенд: expo-auth-session + Google Sign-In кнопка
- [ ] **Apple Sign-In** — Sign in with Apple для iOS
- [ ] Серверные push-уведомления (FCM)
- [ ] Freeze Days — UI на фронтенде (API уже готов ✅)
- [ ] Цепочки привычек — UI на фронтенде (API уже готов ✅)
- [ ] Dashboard + Trends — UI на фронтенде (API уже готов ✅)
- [ ] Расширенная аналитика (radar chart)
- [ ] Оффлайн-режим
- [ ] Экспорт данных (CSV, PDF)
- [ ] iOS-виджеты (Home Screen, Lock Screen)

---

## Фаза 7 — Будущее (v2.0)

- [ ] Партнёр по ответственности (accountability partner)
- [ ] Контракт привычки
- [ ] Правило Златовласки (автоусложнение)
- [ ] Избавление от плохих привычек
- [ ] Социальные фичи (челленджи)
- [ ] Количественные привычки (стаканы воды, минуты)
- [ ] Геймификация (уровни, разблокировка тем)
- [ ] Android-версия
