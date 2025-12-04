# Codespaces quickstart for NoteSmith with Postgres

## Run in Codespaces
1. Create a Codespace (Code -> Codespaces -> New). Wait for setup; Postgres starts automatically via docker-compose.
2. In terminal: `./mvnw spring-boot:run` (or `./gradlew bootRun`).
3. When prompted, open forwarded port 8080 to view the app.
4. DB connection: `jdbc:postgresql://localhost:5432/notesmith_db`, user `app`, password `app`.

## Local dev (optional)
- Start Postgres locally with the same creds, or edit `config.properties` / env vars `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Run the app as above.

## Notes
- `config.properties` is loaded by `AppConfig`; env vars override it (keys uppercased with underscores).
- DB data in Codespaces persists per Codespace; delete the Codespace to reset.
