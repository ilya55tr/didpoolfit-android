# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Состояние проекта

Свежий шаблон Android Studio «Empty Views Activity»: один модуль `:app`, одна `MainActivity` с layout `activity_main.xml` (ConstraintLayout + «Hello World»). Бизнес-логики пока нет, тесты — заглушки `ExampleUnitTest` / `ExampleInstrumentedTest`. Git-репозиторий не инициализирован.

## Стек

- Язык — **Java** (не Kotlin), `sourceCompatibility`/`targetCompatibility` = Java 11. Kotlin-плагин не подключён.
- UI — XML-вёрстка (Views), не Compose. Тема `Theme.DIDPOOLFit` наследуется от `Theme.Material3.DayNight.NoActionBar`; `MainActivity` включает edge-to-edge и вручную раздаёт отступы системных панелей на view `@id/main`.
- AGP 9.4.1, Gradle 9.6.0 (wrapper), `compileSdk`/`targetSdk` = 37, `minSdk` = 24.
- Пакет / `applicationId` — `by.ilyatr.didpoolfit`.
- Версии зависимостей — только через version catalog `gradle/libs.versions.toml` (в `app/build.gradle.kts` ссылки вида `libs.appcompat`).
- Репозитории объявлены только в `settings.gradle.kts` (`FAIL_ON_PROJECT_REPOS`) — в модулях репозитории не добавлять.
- Включён configuration cache (`gradle.properties`).
- R8-правила лежат в `app/src/main/keepRules/*.keep` (новый механизм AGP 9, не `proguard-rules.pro`). Release-сборка использует блок `optimization { enable = true }`.

## Команды

Запускать из корня проекта (Windows: `gradlew.bat` или `./gradlew` в Git Bash):

```bash
./gradlew assembleDebug                 # собрать debug APK
./gradlew installDebug                  # установить на подключённое устройство/эмулятор
./gradlew testDebugUnitTest             # локальные unit-тесты (JUnit 4)
./gradlew testDebugUnitTest --tests "by.ilyatr.didpoolfit.ExampleUnitTest"            # один класс
./gradlew testDebugUnitTest --tests "by.ilyatr.didpoolfit.ExampleUnitTest.addition_isCorrect"  # один тест
./gradlew connectedDebugAndroidTest     # инструментальные тесты (нужно устройство)
./gradlew lintDebug                     # Android Lint
```

## Окружение

- Android SDK: `D:\Android\Sdk` (задан в `local.properties`, файл не коммитить).
- JVM демона Gradle — toolchain версии 25, скачивается автоматически через foojay (`gradle/gradle-daemon-jvm.properties`).
- `GRADLE_USER_HOME` не задан → кэши Gradle и скачанные JDK по умолчанию попадут на C:. По глобальному правилу пользователя всё ставится на F:, поэтому перед первой сборкой из консоли уточнить у пользователя путь (например, `GRADLE_USER_HOME=F:\...`).
