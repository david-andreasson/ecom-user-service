# Molnbaserad E‑commerce Integration — **Del 1: User Service**

> Grupp 2: **Ali, Angelica, David, Ismete, Johan**  
> Status: Del 1 (User Service) färdig för deployment. Azure-delar kommer i nästa etapp.

## Översikt
User Service är en fristående Spring Boot‑applikation som hanterar **registrering**, **inloggning (JWT)**, profildata och grundläggande aktivitetsloggning. Tjänsten exponerar ett REST‑API och dokumenteras via **OpenAPI/Swagger**. Den byggs och publiceras som Docker‑image via GitHub Actions.

## Teknikstack
- Java 21, Spring Boot (Web, Security, Data JPA, Validation)
- JWT (io.jsonwebtoken) för access‑token
- Springdoc OpenAPI (Swagger UI)
- H2 (dev) / valfri SQL för prod
- Maven + JaCoCo
- Docker (Amazon Corretto 21 runtime)
- GitHub Actions (build, test, push till Docker Hub)

## Snabbstart

### 1) Kör lokalt (Maven)
```bash
# Bygg & kör tester
mvn clean verify

# Starta appen (default port 8080)
mvn spring-boot:run   -Dspring-boot.run.jvmArguments="-Djwt.secret=changeme-32+chars"
```

### 2) Kör med Docker
```bash
# Bygg image lokalt
docker build -t user-service:local -f dockerfile .

# Starta container (mappa valfri port på värden)
docker run -d --name user-service   -p 8085:8080   -e JWT_SECRET="changeme-32+chars"   user-service:local
```

> **Miljövariabler** (Spring relaxed binding):  
> `JWT_SECRET` (obligatorisk), `JWT_ISSUER` (valfri, default `user-service`), `JWT_ACCESS_TOKEN_MINUTES` (valfri, default 30).  
> Databas kan sättas via t.ex. `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` vid behov.

### 3) Docker Hub (färdig image)
```bash
docker run -d --name ecom_user_service   -p 8085:8080   -e JWT_SECRET="changeme-32+chars"   davidandreasson/ecom-integration-project:latest
```

## API (kortfattat)
Bas‑URL: `http(s)://host:port`

- **POST /auth/register** – skapa användare  
  Body: `{ "email": "...", "password": "...", "firstName": "...", "lastName": "..." }`

- **POST /auth/login** – logga in, returnerar `accessToken` (JWT)  
  Body: `{ "email": "...", "password": "..." }`

- **GET /me** – skyddad endpoint (kräver `Authorization: Bearer <jwt>`)

Swagger UI: `/swagger-ui/index.html`  
OpenAPI JSON: `/v3/api-docs`

## Säkerhet
- Stateless **JWT**‑autentisering via filter i Spring Security.
- Endpoints för dokumentation och auth är öppna; övriga kräver JWT.
- Lösenord lagras som **BCrypt‑hash**.

## CI/CD
- **GitHub Actions:** bygger, kör tester, laddar upp JaCoCo‑rapport och **publicerar Docker‑image** till Docker Hub på push till `main`.
- Image‑taggar: `latest` och commit‑SHA.  

## Code quality – SonarCloud

Vi kör analys i **SonarCloud** på varje push/PR mot `main`. Workflowen kör Maven‑pluginen och skickar upp **JaCoCo**‑rapporten.

**Nycklar & inställningar**
- `sonar.projectKey`: `Moln-integration_E-commerce-Integration`
- `sonar.organization`: `moln-integration`
- JaCoCo XML: `target/site/jacoco/jacoco.xml`
- Secret: `SONAR_TOKEN` (lagras i repo/org‑secrets)

**Kör lokalt (om du vill testa analysen):**
```bash
mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar   -Dsonar.projectKey=Moln-integration_E-commerce-Integration   -Dsonar.organization=moln-integration   -Dsonar.host.url=https://sonarcloud.io   -Dsonar.token=$SONAR_TOKEN
```


## Utvecklings‑tips
- Kör H2 lokalt eller peka mot valfri SQL‑instans via `SPRING_DATASOURCE_*`.
- CORS tillåter lokalt UI (t.ex. `http://localhost:3000`).

## Nästa steg (Del 2–3, utanför denna README)
- Azure SQL och deployment till Azure med automatiserad pipeline.
- Asynkron kommunikation och utökad övervakning/loggning.

---

© 2025 Grupp 2. Endast utbildningssyfte.
