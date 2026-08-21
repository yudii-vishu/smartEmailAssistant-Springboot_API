Smart email assistant is a Spring Boot service that generate email replies using Google’s Generative Language (Gemini) API.
Tech
•
Java 17, Spring Boot 4 (WebMVC + WebFlux), WebClient
Config (env or application.properties)
•
GEMINI_API_KEY — <PUT YOUR API KEY>
•
GEMINI_API_URL — default: https://generativelanguage.googleapis.com
•
GEMINI_MODEL_NAME — default: gemini-3.6-flash
Run
•
mvnw spring-boot:run
•
Tests: ./mvnw test
API
•
POST /api/email/generate
•
Body: {"emailContent":"<original email>","tone":"professional"}
•
Response: generated email text
Notes
•
Provide a valid GEMINI_API_KEY before calling the AI endpoint.
•
License: MIT (change as needed)
