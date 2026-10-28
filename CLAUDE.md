# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Состояние проекта

Учебная практика УП.01.02, вариант 27: фитнес-приложение по макету Figma (https://www.figma.com/design/vEpiBBAhA81bS69refp7nd/; рабочая копия автора — https://www.figma.com/design/KzjT695IXxVbVkacY8FAM5/). Экраны в макете называются так же, как в issue: «Register Page - 1», «Register Page - 2», «Register Page - 3.1…3.3», «Login Page» и т. д. Один модуль `:app`. Репозиторий: https://github.com/ilya55tr/didpoolfit-android, план работ — в GitHub Issues.

Код пишет автор проекта сам. ИИ-ассистент выступает наставником: объясняет подход, называет классы и атрибуты, готовый код даёт только по прямой просьбе.

Поток экранов: `SplashActivity` → `WelcomeScreenActivity` → `OnboardingActivity` → `RegisterActivity` → `RegisterSecondActivity`; из регистрации есть ссылка на `LoginActivity`.

Что уже есть:
- `SplashActivity` — стартовый экран (LAUNCHER), 2 секунды на градиентном фоне, затем Welcome. Индикатора загрузки пока нет.
- `WelcomeScreenActivity` — кнопка Get Started ведёт в онбординг.
- `OnboardingActivity` — 4 шага в одной активити (массивы drawable + `showStep`), «назад» через `OnBackPressedCallback`, после последнего шага — регистрация.
- `RegisterActivity` (Register Page - 1) — поля имени, телефона (`PhoneNumberFormattingTextWatcher("RU")`, при фокусе подставляется `+7`), email, пароля и повтора пароля, чекбокс соглашения. Валидация в `validate()`: ошибки через `TextInputLayout.setError` на всех полях сразу; пароль — `PASSWORD_REGEX` (ровно 8 цифр/ASCII-спецсимволов); телефон — 11 цифр. После валидации данные пишутся в SharedPreferences `"user"` прямо из активити (ключи `fullName`, `phoneNumber`, `email`, `password`, `registered`), класса `UserStorage` ещё нет.
- `LoginActivity` — только вёрстка, логики нет.
- `RegisterSecondActivity` (Register Page - 2) — пустая заготовка, ресурсы (иконки, плашки KG/CM) уже в `drawable`.

Пока нет: `UserStorage`, логики входа, двух сценариев запуска, главного экрана, профиля. Тесты — шаблонные заглушки `ExampleUnitTest` / `ExampleInstrumentedTest`.

## Особенности вёрстки

- Многие элементы макета — не вёрстка, а экспортированные из Figma векторные drawable: шаги онбординга (`onboarding_frame_N`, `onboarding_text_section_N`, `onboarding_button_N`), заголовки экранов (`register_title_section_1`, `login_title_section`), разделитель «Or» (`or_register_line`), кнопки соцсетей (`social_media.png`). Чтобы поменять в них текст, нужен новый экспорт, а не правка `strings.xml`.
- Шрифт — Poppins, все начертания в `res/font/` (`@font/poppins_regular`, `poppins_medium`, `poppins_bold`…).
- Кнопки с градиентом: `android:background="@drawable/bg_button_gradient"` + `app:backgroundTint="@null"`. Фон не зависит от состояния, поэтому выключенная (`enabled=false`) кнопка выглядит так же, как включённая — проверку делать по нажатию.
- `<Button>` в теме Material3 создаётся как `MaterialButton`: иконка рядом с текстом — `app:icon` + `app:iconGravity="textStart"` + `app:iconTint`.
- Поля ввода — `TextInputLayout` стиля `FilledBox` с одинаковым набором `app:box*`-атрибутов, скопированным в каждое поле (в общий стиль пока не вынесено).

## Стек

- Язык — **Java** (не Kotlin), `sourceCompatibility`/`targetCompatibility` = Java 11. Kotlin-плагин не подключён.
- UI — XML-вёрстка (Views), не Compose. Тема `Theme.DIDPOOLFit` наследуется от `Theme.Material3.DayNight.NoActionBar` (план — сменить на Light; тогда же поправить или удалить `values-night/themes.xml`, где тоже переопределяется `Base.Theme.DIDPOOLFit`). У `SplashActivity` своя тема `Theme.DIDPOOLFit.Splash`; в `values-v31` она задаёт `windowSplashScreenBackground`, поэтому на Android 12+ сначала показывается системный сплэш, затем наш — это ожидаемо. Каждая активити включает edge-to-edge и вручную раздаёт отступы системных панелей на view `@id/main`.
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
- `GRADLE_USER_HOME` не задан → кэши Gradle лежат в `C:\Users\ilya\.gradle`, их же использует Android Studio. Сборка из консоли переиспользует эти кэши; переносить их на F: (по глобальному правилу пользователя) — только по согласованию.
- GitHub CLI (`gh`) не установлен: issue из консоли не создать и не закрыть, только читать через публичный API (`https://api.github.com/repos/ilya55tr/didpoolfit-android/issues`).
