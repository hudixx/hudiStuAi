# Repository Guidelines

## Project Structure & Module Organization

`hudiStu` is a Maven multi-module Spring Boot 3 / Spring AI learning repo (Java 17).

- Root `pom.xml` — parent BOM, shared deps (Web, Test, Hutool), module list
- `quick-start/` — minimal ChatModel demos (DeepSeek, DashScope, Ollama)
  - Code: `src/main/java/com/hudi/spingai/quickstart/`
  - Config/assets: `src/main/resources/` (`application.yml`, `files/`)
  - Tests: `src/test/java/com/hudi/spingai/quickstart/`
- `more-platform-and-model/` — multi-model, advisors, chat memory (JDBC/Redis), tools
  - Code: `src/main/java/com/hudi/springai/more/` (`controller/`, `config/`, `service/`, `advisors/`)
  - Resources: `application.yml`, `files/prompt.st`, `static/`

Do not commit `target/`, IDE metadata, or secrets.

## Build, Test, and Development Commands

```bash
mvn -pl quick-start spring-boot:run
mvn -pl more-platform-and-model spring-boot:run
mvn -DskipTests=false -pl quick-start test
mvn -pl more-platform-and-model -Dtest=TestStructuredOut test
mvn clean package -DskipTests
```

Root Surefire sets `skipTests=true`; pass `-DskipTests=false` when you need tests.

## Coding Style & Naming Conventions

- Java 17, 4-space indent, Spring Boot defaults.
- Packages: `com.hudi.spingai.quickstart` / `com.hudi.springai.more` (keep existing names).
- Classes: PascalCase (`ModelApplication`, `ToolService`); methods/fields: camelCase.
- Prefer constructor or `@Autowired` injection; keep AI options in `application.yml` or config beans.
- No enforced formatter/linter yet; match neighboring files.

## Testing Guidelines

- Framework: JUnit 5 + `@SpringBootTest` (integration-style, needs live API/env).
- Name classes `*Test` (e.g. `TestDeepseek`, `OllamaTest`, `TestStructuredOut`).
- Cover model calls, streaming, memory, tools, and structured output as you add features.
- Avoid hardcoding API keys in tests; rely on env vars.

## Commit & Pull Request Guidelines

History uses short Chinese summaries of the change, for example:

- `优化，使用动态tools`
- `增加数据库的方式记忆存储`
- `ChatClien 系统提示词`

PRs should state module impacted, behavior change, required env vars, and how you verified (command + result). Link issues when applicable; include screenshots only for UI (`static/`).

## Security & Configuration Tips

Set keys/hosts via environment variables before run:

- `ai_key_ds`, `ai_key_qw` — model providers
- `ai_key_anthropic`, `ai_base_anthropic` — Anthropic-compatible gateway (optional)
- `a_aly_ads`, `a_mysql_password` — MySQL/Redis host and DB password (more-platform module)

Never commit real keys or passwords. Prefer env placeholders in YAML over literals.
