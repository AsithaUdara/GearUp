package com.gearup.templateservice.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis Configuration for Template Service
 * Configures caching with custom TTL and ObjectMapper
 * Note: RedisTemplate bean is provided by shared-libs/common-utils
 */
@Configuration
@EnableCaching
public class TemplateRedisConfig {

    /**
     * Create a dedicated ObjectMapper for Redis serialization only.
     * This is NOT exposed as a Spring-managed ObjectMapper bean because
     * that would replace the application's primary ObjectMapper and
     * enable polymorphic typing for all HTTP JSON deserialization,
     * causing Jackson to require an '@class' discriminator on incoming
     * request bodies.
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Enable polymorphic type handling for Redis values where heterogeneous types may be stored
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return mapper;
    }

    /**
     * Redis Cache Configuration with TTL
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper redisObjectMapper = createRedisObjectMapper();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default 1 hour TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();
    }

    /**
     * Redis Cache Manager with custom cache configurations
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, 
                                     RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration("serviceTemplates", 
                    cacheConfiguration.entryTtl(Duration.ofHours(2))) // Templates rarely change
                .withCacheConfiguration("activeTemplates", 
                    cacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .transactionAware()
                .build();
    }
}
