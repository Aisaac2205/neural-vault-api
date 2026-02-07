# Neural Vault API

REST API built with Spring Boot 3.2+ and Spring AI to serve AI tool data and provide intelligent recommendations using Google Gemini.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Primary language |
| Spring Boot | 3.2.2 | Backend framework |
| Spring AI | 1.0.0-M5 | LLM integration (Gemini) |
| Spring Data JPA | 3.2.2 | Data access layer |
| Caffeine | 3.x | In-memory caching |
| Bucket4j | 8.x | Rate limiting |
| H2 Database | 2.x | In-memory database |
| Lombok | 1.18.x | Boilerplate reduction |
| Maven | 3.9+ | Dependency management |

## Architecture

```
neural-vault-api/
├── src/main/java/com/neuralvault/api/
│   ├── NeuralVaultApiApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── CacheConfig.java
│   │   ├── RateLimitConfig.java
│   │   ├── RateLimitFilter.java
│   │   └── seeder/
│   │       ├── DataSeeder.java
│   │       ├── GeneralToolSeeder.java
│   │       └── IdeSeeder.java
│   ├── controller/
│   │   └── AiToolController.java
│   ├── dto/
│   │   └── RecommendationRequest.java
│   ├── entity/
│   │   └── AiTool.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── RateLimitExceededException.java
│   ├── repository/
│   │   └── AiToolRepository.java
│   └── service/
│       └── RecommendationService.java
└── src/main/resources/
    └── application.properties
```

## Data Model

### AiTool Entity

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (PK) | Slug-based identifier |
| `name` | String | Tool display name |
| `specialty` | String | Main capability |
| `description` | String | Detailed description |
| `pricing` | String | Pricing model |
| `url` | String | Official website |
| `icon` | String | Phosphor Icons class |
| `category` | Enum | `GENERAL` or `IDE` |
| `tags` | List | Searchable keywords |

### Seeded Data

**GENERAL Category (9 tools):** Claude, GPT-5, Midjourney, Perplexity, Copilot, Suno, Gemini, Grok, Qwen

**IDE Category (6 tools):** Antigravity, VS Code, OpenCode, Cursor, Windsurf, IntelliJ IDEA

## API Endpoints

### Tools

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tools` | List all tools |
| GET | `/api/tools/{id}` | Get tool by ID |
| GET | `/api/tools/category/{category}` | Filter by category |

### AI Recommendations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/recommend` | Get AI-powered recommendation |

**Request Body:**
```json
{
  "query": "I need to edit professional photos"
}
```

**Response:** Returns the most suitable AiTool object based on Gemini analysis.

**Note:** Rate limited to 10 requests per minute per IP. Results cached for 1 hour.

## Configuration

### Environment Variables

```bash
GEMINI_API_KEY=your-google-aistudio-api-key
```

Get your API key from [Google AI Studio](https://aistudio.google.com/app/apikey).

### application.properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:neuralvaultdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Gemini AI
spring.ai.google.gemini.api-key=${GEMINI_API_KEY}

# Cache
app.cache.recommendation.ttl-hours=1
app.cache.recommendation.max-size=100

# Rate Limiting
app.rate-limit.requests-per-minute=10
```

## Installation

### Requirements

- Java 21+
- Maven 3.9+
- Google Gemini API Key

### Development

```bash
# Navigate to project
cd neural-vault-api

# Create environment file
echo "GEMINI_API_KEY=your-api-key" > .env

# Compile and run
mvn spring-boot:run
```

### Verification

- API: http://localhost:8080/api/tools
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:neuralvaultdb`
  - User: `sa` (no password)

### Testing

```bash
# List all tools
curl http://localhost:8080/api/tools

# Get specific tool
curl http://localhost:8080/api/tools/cursor

# Filter by category
curl http://localhost:8080/api/tools/category/IDE

# Get AI recommendation
curl -X POST http://localhost:8080/api/recommend \
  -H "Content-Type: application/json" \
  -d '{"query": "I need to generate music"}'
```

## Production Deployment

### Build JAR

```bash
mvn clean package
java -jar target/neural-vault-api-1.0.0.jar
```

### Docker

```bash
docker build -t neural-vault-api .
docker run -p 8080:8080 --env-file .env neural-vault-api
```

## Security Features

- **Rate Limiting:** 10 requests/minute per IP on recommendation endpoint
- **Response Caching:** 1-hour TTL to reduce API costs
- **CORS:** Configured for frontend origin only
- **Input Validation:** DTOs validated with Bean Validation

## Frontend Integration

This API is designed to work with `neural-vault-web` (Angular 17+).

The frontend expects JSON responses conforming to the `AiTool` interface.

## Troubleshooting

| Error | Solution |
|-------|----------|
| GEMINI_API_KEY not set | Create `.env` file with valid key |
| DataSource configuration | Verify H2 URL in properties |
| CORS errors | Ensure frontend runs on allowed origin |
| Port 8080 in use | Change `server.port` in properties |
| Rate limit exceeded | Wait 1 minute or increase limit in config |

## License

MIT
