# Правила работы

- Отвечай по-русски, кратко. Следуй схеме «План → Выполнение → Проверка» и перечисляй выполненные проверки.
- Не создавай коммиты и не делай push, пока пользователь прямо не попросит отправить изменения в удалённый репозиторий. git add выполняй по просьбе пользователя, только для согласованных файлов.
- Автор пишет учебный проект сам. Объясняй подход. Если пользователь просит код в чат, присылай его в чат и не меняй исходники приложения. Изменять файлы можно только в явно согласованном объёме.
- Перед удалением или перезаписью файлов спрашивай подтверждение. Прямая просьба удалить конкретный файл уже является разрешением на это действие.
- Не добавляй в Git local.properties, секреты и служебную папку .playwright-mcp/.

# Проект и объём

Учебная практика УП.01.02, вариант 27 — DIDPOOLFit.
- GitHub: https://github.com/ilya55tr/didpoolfit-android
- Figma: https://www.figma.com/design/KzjT695IXxVbVkacY8FAM5/
- План работ хранится в GitHub Issues; описания и чекбоксы могут отставать от реализации.
- Максимум 9 активити без сплэша: Welcome, Onboarding, Register 1, Register 2, Login, Home, Workout Details, Profile, Activity History.
- Register 3.x, Success Registration, Workout Tracker и Notifications исключены из согласованного объёма.

# Стек

- Java 11, XML Views (не Compose), Material3, один модуль :app, пакет by.ilyatr.didpoolfit.
- AGP 9.4.1, Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 24.
- Версии зависимостей — gradle/libs.versions.toml. Репозитории — только settings.gradle.kts.
- Включён configuration cache. R8-правила — app/src/main/keepRules/*.keep.
- Poppins из res/font. Иллюстрации и часть заголовков экспортированы из Figma в drawable.
- Градиентные кнопки: @drawable/bg_button_gradient + app:backgroundTint="@null".
- Поля: TextInputLayout; hint на TextInputEditText, ошибки через setError.
- Каждая активити обрабатывает edge-to-edge и отступы системных панелей.

# Текущее состояние

- Welcome и четыре шага Onboarding реализованы.
- Register 1: имя, телефон, email, пароль и повтор, согласие, валидация. Проверяется совпадение email и телефона с уже зарегистрированным пользователем.
- Register 2: пол, дата рождения (MaterialDatePicker, без будущих дат, форматирование UTC), вес 20–300 кг, рост 50–250 см. Сохраняет registered=true и открывает Login со сбросом стека.
- SharedPreferences "user" используются напрямую из активити. Ключи: fullName, phoneNumber, email, password, gender (String), dateOfBirth (Long, миллисекунды), weight (Float, кг), height (Int, см), registered и loggedIn (Boolean). Класса UserStorage нет. Один пользователь перезаписывает другого (#15).
- Login проверяет сохранённые email/пароль, устанавливает loggedIn=true, открывает Home со сбросом стека. Автоподстановки email нет. Forgot password — Toast.
- Splash: прогресс и проценты через ValueAnimator за 3 секунды. loggedIn → Home; иначе registered → Login; иначе Welcome.
- Home: статическое приветствие, контейнер bmiCard с фоном, текстами и View More; нижняя панель с выбранным Home. Расчёт BMI и диаграмма пока не добавлены. Код диаграммы пользователь хочет получить в чат и вставить самостоятельно.
- Продолжение: Home (#10), Profile (#11), Workout Details (#13), Activity History (#14). История планируется как JSON в SharedPreferences.
- Home по плану: BMI, RecyclerView + CardView для Fullbody/Lowerbody/AB Workout, переход к деталям, Latest Workout, рабочие иконки Home/Profile. Графики активности, воду и сон не делать.
- Отдельные задачи: темы (#12), несколько пользователей (#15), Google (#5), Facebook (#6), восстановление пароля (#9).
- Тема пока DayNight с values-night; в values-v31 windowSplashScreenBackground ссылается на drawable. Это незавершённая задача #12.

# Проверки и окружение

- Команды: gradlew.bat assembleDebug, testDebugUnitTest, lintDebug; connectedDebugAndroidTest требует устройство.
- ExampleUnitTest и ExampleInstrumentedTest — шаблоны, не проверка бизнес-логики.
- SDK задан в локальном local.properties. Gradle daemon запрашивает JDK 25. Проверять фактическое окружение до сборки.
- Не объявлять сборку успешной без завершившейся проверки. Последняя попытка отдельной сборки не подтвердила компиляцию: не хватало зависимостей в новом кэше, онлайн-попытка была остановлена на их разрешении.
