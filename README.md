# API-KREDITKU

AI-powered credit card recommendation API.

## Setup

1. Install Java 24 (or compatible version)

2. For local development (default):
   - Install Ollama: https://ollama.com
   - Start Ollama: `ollama serve`
   - Pull the model: `ollama pull llama3.1`
   - Run the app: `./mvnw spring-boot:run`

3. For cloud deployment with OpenAI:
   - Create `src/main/resources/application-local.properties`
   - Add your key: `openai.api.key=sk-your-key-here`
   - Change provider: `llm.provider=openai`
   
   Or set environment variable:
```bash
   export OPENAI_API_KEY=sk-your-key
   ./mvnw spring-boot:run
```

4. Test it:
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health check: http://localhost:8080/api/health

## Tech Stack

- Java 24
- Spring Boot 3.2
- Apache POI (Excel parsing)
- Ollama / OpenAI (LLM providers)