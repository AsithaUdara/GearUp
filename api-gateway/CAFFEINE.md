# Caffeine cache for LoadBalancer

## What this does

Adding Caffeine to the classpath improves Spring Cloud LoadBalancer's internal caching performance. In production it's recommended to use a Caffeine-backed cache to avoid the default in-memory cache limitations.

## What I changed

- Added `com.github.ben-manes.caffeine:caffeine:3.1.8` to `api-gateway/pom.xml`.

## How to enable in production

1. Add Caffeine dependency (already added).
2. Provide a `CacheManager` bean that uses Caffeine. Example:

```java
@Configuration
public class CacheConfig {
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cm = new CaffeineCacheManager();
        cm.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(10)));
        return cm;
    }
}
```

3. Optionally tune Spring Cloud LoadBalancer caching behavior via properties or provide a custom `Cache` bean if you need more control.

## Notes

- The dependency is lightweight and safe to include. If you'd like, I can add the `CacheConfig` class above into the `api-gateway` module.
