# Neural Vault API

REST API built with Spring Boot 3.2+ to serve AI tool data and provide intelligent recommendations using Google Gemini. Features enterprise-grade security protections against abuse, bots, and API overconsumption.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Primary language |
| Spring Boot | 3.2.2 | Backend framework |
| Spring Data JPA | 3.2.2 | Data access layer |
| Caffeine | 3.x | In-memory caching |
| Bucket4j | 8.x | Rate limiting |
| H2 Database | 2.x | In-memory database |
| Lombok | 1.18.x | Boilerplate reduction |
| Maven | 3.9+ | Dependency management |
| Google GenAI | Latest | Gemini API integration |

## Architecture

```
neural-vault-api/
├── src/main/java/com/neuralvault/api/
│   ├── NeuralVaultApiApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── CacheConfig.java
│   │   ├── RateLimitConfig.java
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
│   ├── filter/
│   │   └── RateLimitFilter.java
│   ├── repository/
│   │   └── AiToolRepository.java
│   └── service/
│       ├── ApiUsageMonitor.java
│       ├── BotDetectionService.java
│       ├── GeminiCircuitBreaker.java
│       ├── GeminiClient.java
│       ├── IpBlocklistService.java
│       ├── PromptSanitizer.java
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

**Response Headers:**
```
X-RateLimit-Remaining: 8
X-RateLimit-Suspicious: true  (if request detected as suspicious)
```

## Security Features

### Multi-Layer Rate Limiting

| Level | Rate | Description |
|-------|------|-------------|
| Normal Users | 10 req/min | Standard rate for legitimate users |
| Suspicious Users | 3 req/min | Reduced rate for detected bots/suspicious behavior |
| Daily Global Limit | 1000 req/day | Maximum API calls to Gemini to prevent overconsumption |

### Bot Detection & Blocking

**Automatic detection of:**
- Known bot user agents (`curl`, `python-requests`, `selenium`, `scrapy`, etc.)
- Missing browser headers (`Accept-Language`, `Accept-Encoding`)
- Security scanner signatures (`Burp`, `SQLMap`, `Nmap`, `Nessus`)
- Suspicious request patterns

**Actions:**
- Immediate blocking for high-risk bots (score >= 7)
- IP blocklist with 60-minute auto-expiry
- Suspicion score tracking per IP
- Progressive penalties for repeated violations

### Circuit Breaker Protection

Protects the Gemini API integration from cascading failures:
- **CLOSED:** Normal operation
- **OPEN:** Blocks requests after 5 consecutive failures (60s cooldown)
- **HALF_OPEN:** Tests recovery with limited traffic
- **Daily Quota Protection:** Stops requests when approaching 1000/day limit

### Prompt Injection Protection

**Input sanitization removes:**
- Dangerous characters: `< > " ' ` ; { } [ ] | \`
- Injection patterns: `ignore previous`, `disregard above`, `system:`, `developer:`
- Script tags and JavaScript protocols
- Maximum query length: 500 characters

**Response validation:**
- Validates Gemini responses against whitelist of valid tool IDs
- Rejects invented or injected IDs
- Sanitizes output before database queries

### IP Blocklist System

**Automatic blocking triggers:**
- Rate limit abuse (+2 suspicion points)
- Bot behavior detected (+1-5 points based on severity)
- Suspicious content patterns (+2 points)
- Security scanner headers (+5 points)
- Auto-block when score >= 5
- 60-minute block duration with auto-cleanup

### API Usage Monitoring

**Hourly reports include:**
- Total requests, blocked requests, suspicious requests
- Percentage of blocked/suspicious traffic
- Daily API quota remaining
- Circuit breaker state

**Automated alerts when:**
- > 100 requests/hour (high volume warning)
- > 20 blocked requests/hour (potential attack)
- < 200 daily requests remaining (quota warning)

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
gemini.api.key=${GEMINI_API_KEY}
gemini.api.model=gemini-2.5-flash-lite

# Cache
app.cache.recommendation.ttl-hours=1
app.cache.recommendation.max-size=100

# Rate Limiting - Normal Users
ratelimit.capacity=10
ratelimit.refill.tokens=10
ratelimit.refill.duration=1

# Rate Limiting - Suspicious Users
ratelimit.suspicious.capacity=3
ratelimit.suspicious.refill.tokens=3

# Rate Limiting - Global Protection
ratelimit.global.daily.max=1000
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

## Security Summary

| Feature | Protection Level | Description |
|---------|------------------|-------------|
| Rate Limiting | High | Multi-tier limits prevent abuse |
| Bot Detection | High | Automatic bot/scanner blocking |
| Circuit Breaker | Medium | API failure protection |
| Prompt Injection | High | Input/output sanitization |
| IP Blocklist | High | Temporary blocking with scoring |
| Response Caching | Medium | Cost reduction & performance |
| CORS | Medium | Origin restriction |
| Input Validation | Medium | DTO validation |

## Frontend Integration

This API is designed to work with `neural-vault-web` (Angular 17+).

The frontend expects JSON responses conforming to the `AiTool` interface.

**For Angular applications:**
```typescript
// No special headers required for legitimate browser requests
// Rate limiting is automatic based on IP and behavior
```

## Troubleshooting

| Error | Solution |
|-------|----------|
| GEMINI_API_KEY not set | Create `.env` file with valid key |
| 403 Forbidden | Your IP may be blocked due to suspicious activity. Wait 60 minutes. |
| 429 Too Many Requests | Rate limit exceeded. Check `X-RateLimit-Remaining` header. |
| Circuit Breaker OPEN | Gemini API may be down. Wait 60s for auto-recovery. |
| Daily quota exceeded | Maximum 1000 requests/day reached. Wait for next day. |
| CORS errors | Ensure frontend runs on allowed origin |
| Port 8080 in use | Change `server.port` in properties |

## License

MIT
