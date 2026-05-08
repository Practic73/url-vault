Промпт для ИИ-ассистента: разработка Personal URL Vault
Роль и контекст
Ты — senior Java-разработчик. Тебе нужно реализовать веб-приложение Personal URL Vault — простое хранилище ссылок с заметками. Это демонстрационное приложение для магистерской диссертации; оно будет интегрировано с внешним ML-шлюзом безопасности, который анализирует входящие URL на предмет вредоносности. Твоя задача — реализовать само приложение; интеграцию со шлюзом описывает отдельное ТЗ, её здесь делать не нужно.
Главный принцип: простота важнее красоты. Это демо для защиты диплома, а не продакшен. Никакого Spring Security, JWT, миграций Liquibase, Docker-инфраструктуры. Минимум кода — максимум работающего функционала.
Технологический стек (фиксирован, обсуждению не подлежит)
КомпонентВерсия / выборЯзыкJava 21СборкаMavenФреймворкSpring Boot 3.3.xORMSpring Data JPAСУБДSQLite (через xerial:sqlite-jdbc + hibernate-community-dialects)ШаблонизаторThymeleafCSSBootstrap 5 через CDN (никакого webpack, никакого npm)JSМинимальный, ванильный, только если совсем нужноЛогированиестандартный SLF4J/LogbackТестыНЕ писать (экономим время)
Назначение приложения
Зарегистрированный пользователь может:

Сохранять интересные URL-адреса с метаданными (название, описание, тег).
Просматривать список своих сохранённых ссылок.
Искать по тегу или названию.
Удалять ненужные записи.
Переходить по сохранённой ссылке.

Авторизация упрощённая: один глобальный пользователь, без логина и регистрации. Хранилище общее. Это нормально для демо — всё внимание защищающихся будет на работе шлюза.
Структура проекта
url-vault/
├── pom.xml
├── url_vault.db                          # создаётся автоматически при старте
├── src/
│   └── main/
│       ├── java/
│       │   └── ru/kniturkai/urlvault/
│       │       ├── UrlVaultApplication.java
│       │       ├── controller/
│       │       │   ├── WebController.java         # GET-страницы (Thymeleaf)
│       │       │   └── ApiController.java         # REST API (JSON)
│       │       ├── service/
│       │       │   └── BookmarkService.java
│       │       ├── repository/
│       │       │   └── BookmarkRepository.java
│       │       ├── model/
│       │       │   └── Bookmark.java               # JPA-сущность
│       │       └── dto/
│       │           ├── BookmarkRequest.java
│       │           └── BookmarkResponse.java
│       └── resources/
│           ├── application.yml
│           ├── templates/
│           │   ├── layout.html                    # общий layout (Thymeleaf fragment)
│           │   ├── index.html                     # список закладок
│           │   ├── add.html                       # форма добавления
│           │   └── error.html                     # страница ошибок (для 403 от шлюза)
│           └── static/
│               └── css/
│                   └── custom.css                 # минимальные правки поверх Bootstrap
└── README.md
Модель данных
Одна сущность — Bookmark:
ПолеТип JavaSQLiteОграниченияidLongINTEGER PK AUTOINCREMENTnot nulltitleStringTEXTnot null, 1–200 символовurlStringTEXTnot null, 1–2000 символов, валидный URLdescriptionStringTEXTnullable, до 500 символовtagStringTEXTnullable, до 50 символовcreatedAtInstantTEXT (ISO-8601)not null, заполняется автоматически
Никаких пользователей, никаких связей. Одна таблица.
Конфигурация (application.yml)
yamlserver:
  port: 8001

spring:
  application:
    name: url-vault
  datasource:
    url: jdbc:sqlite:url_vault.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    ru.kniturkai.urlvault: INFO
    org.hibernate.SQL: WARN
Порт 8001 фиксирован — это договорённость с шлюзом, который будет проксировать на этот порт.
Веб-эндпоинты (Thymeleaf)
МетодПутьНазначениеШаблонGET/Главная: список всех закладок, поиск, кнопка «Добавить»index.htmlGET/addФорма добавления новой закладкиadd.htmlPOST/addГлавный эндпоинт для демо. Создание закладки из формы. После успеха redirect на /.—POST/delete/{id}Удаление закладки. Redirect на /.—GET/go/{id}Редирект (HTTP 302) на сохранённый URL по id.—GET/errorСтраница ошибки (для 403 от шлюза или внутренних ошибок)error.html
Параметры поиска на главной
GET /?q=...&tag=... — фильтрация по подстроке в title или по точному совпадению tag. Если параметры пустые — показать всё.
REST API (JSON)
Параллельно с web-эндпоинтами — REST API под префиксом /api:
МетодПутьНазначениеGET/api/bookmarksСписок всех закладок (JSON)GET/api/bookmarks/{id}Одна закладка по idPOST/api/bookmarksСоздать закладку. Тело: BookmarkRequest. Ответ: BookmarkResponse + 201 Created.DELETE/api/bookmarks/{id}Удалить. Ответ: 204 No Content.GET/api/health{"status": "UP"} — для проверки шлюзом.
BookmarkRequest (DTO)
javapublic record BookmarkRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 2000) String url,
    @Size(max = 500) String description,
    @Size(max = 50) String tag
) {}
BookmarkResponse (DTO)
javapublic record BookmarkResponse(
    Long id,
    String title,
    String url,
    String description,
    String tag,
    Instant createdAt
) {}
Валидация — через jakarta.validation.constraints + @Valid в контроллере + @RestControllerAdvice для возврата 400 с понятными сообщениями.
Шаблоны Thymeleaf
layout.html (фрагмент с шапкой и футером)
Базовый layout: подключённый Bootstrap 5 с CDN, <nav> со ссылками на «/» и «/add», блок <th:block th:fragment="content"></th:block> посередине, минимальный футер.
index.html

Заголовок «Personal URL Vault».
Форма поиска (поле q + поле tag + кнопка «Найти»).
Кнопка «➕ Добавить ссылку» (ссылка на /add).
Таблица или список карточек с закладками. На каждой карточке:
Название (clickable → ведёт на /go/{id}).
URL (тёмно-серый, обрезанный до 80 символов).
Тег (badge).
Описание.
Дата создания.
Кнопка «Удалить» (POST на /delete/{id} через мини-форму).
Если список пустой — текст «Ничего не сохранено. Добавьте первую ссылку».

add.html

Заголовок «Добавить ссылку».
Форма с полями: title (input), url (input type=url), tag (input), description (textarea).
Кнопка «Сохранить» (POST на /add).
Кнопка «Отмена» (ссылка на /).
Под формой подсказка: «Перед сохранением URL будет проверен системой безопасности».

error.html

Большая надпись «🛡️ Запрос заблокирован».
Подзаголовок: «Система безопасности определила введённый URL как потенциально опасный».
Кнопка «Назад» (на /).

Это специально для демо: когда шлюз вернёт 403, пользователь увидит понятную страницу.
Демо-данные при старте
При первом запуске (когда таблица пустая) — заполнить 3 безопасных закладки:

https://habr.com/ru/articles/750000/ — «Статья на Хабре», тег «articles».
https://github.com/spring-projects/spring-boot — «Spring Boot на GitHub», тег «dev».
https://kai.ru/ — «КНИТУ-КАИ», тег «university».

Реализовать через CommandLineRunner или @PostConstruct в BookmarkService. Никаких SQL-скриптов.
Зависимости в pom.xml
xml<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.46.1.0</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-community-dialects</artifactId>
    </dependency>
</dependencies>
Java 21, Spring Boot 3.3.x — последние стабильные на момент написания.
Требования к качеству кода

Использовать конструкторную инъекцию зависимостей (final поля + конструктор, без @Autowired на полях).
DTO — это record-ы.
Сервисный слой содержит логику; контроллеры тонкие.
Логировать ключевые действия на уровне INFO: «Сохранена закладка id=X, url=Y», «Удалена закладка id=X».
Никакой бизнес-логики в шаблонах.
Никаких System.out.println.
Имена пакетов в нижнем регистре, классы в PascalCase, методы в camelCase.

README.md
Финальный README.md должен содержать:

Краткое описание приложения и его назначения.
Требования: Java 21, Maven 3.9+.
Команды:

bash mvn clean package
 java -jar target/url-vault-*.jar
 # или
 mvn spring-boot:run

Адрес: http://localhost:8001/.
Список ключевых эндпоинтов (web и REST).
Раздел «Интеграция со шлюзом безопасности»: одной строкой «Приложение разрабатывается для работы за прокси-шлюзом ML Security Gateway на порту 8080».

Критерии готовности
Приложение считается готовым, когда:

mvn clean package собирает проект без ошибок.
mvn spring-boot:run запускает сервер на порту 8001.
На http://localhost:8001/ открывается главная с тремя демо-закладками.
Можно добавить новую закладку через форму и через REST API.
Поиск по ?q= и ?tag= фильтрует список.
Удаление работает.
GET /go/1 редиректит на habr.com.
GET /api/health возвращает {"status":"UP"}.
SQLite-файл url_vault.db создаётся в корне проекта при первом запуске.

Что НЕ нужно делать

❌ Spring Security, авторизация, регистрация пользователей.
❌ Liquibase / Flyway миграции.
❌ Docker, docker-compose.
❌ Тесты (юнит, интеграционные — никакие).
❌ Реактивщина (WebFlux). Только классический MVC.
❌ JWT, сессии, cookies.
❌ Кэширование (Redis, Caffeine).
❌ Собственная обработка валидации URL — пусть это делает Bean Validation @URL или @Pattern.
❌ Прокси-логика, обращение к внешним сервисам, ML — этим занимается отдельный шлюз.
❌ React, Vue, любые SPA — только Thymeleaf.
❌ JS-фреймворки. Бутстрап и чистый HTML.

Последовательность реализации
Чтобы упростить отладку — реализуй в таком порядке:

Создай Maven-проект, pom.xml, главный класс UrlVaultApplication.
Настрой application.yml с SQLite.
Создай модель Bookmark и репозиторий.
Создай BookmarkService с CRUD-методами + инициализация демо-данных.
Создай ApiController (REST) — проверь через Postman / curl.
Создай WebController (Thymeleaf) и шаблоны.
Доведи до ума error.html и стилизацию.
Напиши README.md.

После каждого шага проверяй, что приложение запускается.