# Backend Changes Before Frontend Integration

Three things needed before the frontend can connect to the backend.

---

## 1. CORS

Create `src/main/java/org/jones/licklibrary/config/CorsConfig.java`:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST");
    }
}
```

---

## 2. Input validation on POST /api/lick

`LickService.uploadLick` NPEs if `rawTab` is null or blank. Add a guard at the top:

```java
if (request.rawTab() == null || request.rawTab().isBlank()) {
    throw new IllegalArgumentException("rawTab must not be blank");
}
```

Map `IllegalArgumentException` → 400 in the controller (same pattern as invalid key).

---

## 3. Add rendered tab string to position response

`GET /api/lick/{id}?key=A` currently returns `List<Position>` where each Position serializes as `{ notes: [...] }`. The frontend can't re-implement `toTabString()` easily.

Add a `PositionResponse` record:

```java
public record PositionResponse(String tabString) {}
```

Update `LickResponse` to use `List<PositionResponse>` instead of `List<Position>`.
Update `toLickResponse` in `LickService` to map each `Position` → `new PositionResponse(p.toTabString())`.

---

## Verification

- `mvn test` still passes
- `curl -X POST http://localhost:8080/api/lick -H 'Content-Type: application/json' -d '{"rawTab":"..."}' ` returns 200 with `positions: null`
- `curl http://localhost:8080/api/lick/{id}?key=A` returns positions each with a `tabString` field
- Requests from `http://localhost:5173` do not get CORS errors
