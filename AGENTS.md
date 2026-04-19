# MyBill Agent Notes

## Commands
- Use plain `mvn`; there is no Maven wrapper.
- Run the app: `mvn spring-boot:run`.
- Run prod profile: `mvn spring-boot:run -Dspring-boot.run.profiles=prod`.
- Run all tests: `mvn test`.
- Run one test class: `mvn test -Dtest=PromptTemplateTest`.
- Run one test method: `mvn test -Dtest=PromptTemplateTest#testPromptTemplateReplacement`.
- Build a jar: `mvn clean package`.
- `just` is installed at `D:\Program Files\just\just.exe`.
- Run the app with the default `dev` profile: `just run`.
- Run the app with an explicit profile: `just run prod`.
- `just run` starts MyBill in the background and returns the final Java PID plus `MyBill started` when it can detect the Java process within about 3 seconds.
- If the Java process is not detected within about 3 seconds, `just run` returns the launcher PID plus `MyBill starting`.
- `just run` writes stdout/stderr to `target/just-run.out.log` and `target/just-run.err.log`; use `just stop` to end the background app.
- Stop the current MyBill Java process started through `just`: `just stop`.
- Check whether the current MyBill Java process started through `just` is running: `just status`.
- Run all tests through `just`: `just test`.
- Build a jar through `just`: `just package`.
- Connect to MySQL with the fixed local script target: `just db-connect`.
- Switch the system-level Java configuration to JDK 8 for new terminals: `just java-use 8`.
- Switch the system-level Java configuration to JDK 21 for new terminals: `just java-use 21`.
- `just java-use` elevates with UAC when needed and updates machine-level `JAVA_HOME` plus machine-level `Path`; open a new terminal to pick up the change and verify `java -version`.
- The `just` Maven entrypoints prefer the current `JAVA_HOME` and fall back to `D:\Program\Java\java-21` on this machine.
- First startup may spend time downloading Maven dependencies before the HTTP port is ready.
- When validating endpoints, start the app in a background process and poll readiness with visible progress instead of waiting silently.
- Avoid long silent waits during startup checks; prefer short polling with explicit progress updates.

## Runtime And Test Gotchas
- `application-dev.yaml` and `application-prod.yaml` both use mandatory `spring.config.import` entries from Nacos at `192.168.31.100:8848`; app startup and any `@SpringBootTest` will fail unless that Nacos server is reachable and serving the expected configs.
- The only `@SpringBootTest` is `src/test/java/PromptTemplateTest.java`; it depends on the real Spring context and comments say the prompt template row must already exist in the database.
- `src/test/java/ApiTest.java` is not a safe unit test: it makes live external HTTP calls and includes hard-coded auth material.
- `src/test/java/Codegen.java` is a live MyBatis-Flex generator utility, not a test; it connects to MySQL at `192.168.31.100:3306` and is currently configured to generate only `tt_ai_retry_task`.

## App Shape
- Single-package Spring Boot app rooted at `org.lemon`; main entrypoint is `src/main/java/org/lemon/App.java`.
- `App` also enables scheduling and scans MyBatis mappers with `@MapperScan("org.lemon.mapper")`.
- Main HTTP surface is in `controller/`: `UserController`, `AppController`, `CommonApiController`, `OpenApiController`, plus schedule-related controllers.
- Scheduled jobs are declared in controllers, not services: `ScheduleController` runs monthly statistics at `0 0 3 1 * ?`, and `RetryScheduleController` processes retry tasks every 10 minutes.

## API Conventions That Matter
- This codebase wraps nearly all request bodies as `ApiReq<T>` and returns `ApiResp<T>` or `PageResp<T>`; even reads are mostly `POST` endpoints.
- Public routes are only `/openApi/**`, `/user/register`, and `/user/login` per `SecurityConfig`; everything else requires JWT auth.
- JWT auth is header-only: `Authorization: Bearer <token>`. `UserController` also writes refreshed access tokens back to that same header.
- Service methods usually resolve the acting user through `UserUtil.getCurrentUserId()` instead of taking a user id from the request.

## Persistence Conventions
- Services commonly extend MyBatis-Flex `ServiceImpl<Mapper, Entity>` and use `queryChain()` / `updateChain()` rather than handwritten repository layers.
- Mapper interfaces live under `org.lemon.mapper` and extend `BaseMapper<T>`.

## Existing Repo Instructions
- `.lingma/rules/java.md` reinforces the actual house style: layered controller/service/mapper/entity structure, `ApiReq`/`ApiResp` wrappers, validation on request bodies, and SLF4J logging.
