package org.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.redis.constant.CacheConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

import static java.util.Collections.singletonMap;

/**
 * redis配置类
 */
@Configuration
@EnableCaching // 开启缓存支持
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Bean
    public LettuceConnectionFactory getLettuceConnectFactory() {
        return new LettuceConnectionFactory();
    }
    /**
     * redisTemplate配置
     * @param lettuceConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate( LettuceConnectionFactory lettuceConnectionFactory) {
        log.info("-----redis config init----");
        // 创建Jackson2JsonRedisSerializer序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);

        // 使用ObjectMapper添加Json读写功能
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        // 指定序列化输入类型 取代了enableDefaultTyping(DefaultTyping.NON_FINAL)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance ,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        // 配置ObjectMapper
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接厂
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        // 创建String序列化器
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        // 使用stringSerializer完成key序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // 使用jackson2JsonRedisSerializer完成value序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // 使用stringSerializer完成Hash key序列化
        redisTemplate.setHashKeySerializer(stringSerializer);
        // 使用jackson2JsonRedisSerializer完成Hash value序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        // 完成属性设置
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }


    /**
	 * @description 自定义的缓存key的生成策略 若想使用这个key
	 *              只需要讲注解上keyGenerator的值设置为keyGenerator即可</br>
	 * @return 自定义策略生成的key
	 */
	@Override
	@Bean
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getDeclaringClass().getName());
				Arrays.stream(params).map(Object::toString).forEach(sb::append);
				return sb.toString();
			}
		};
	}

    /**
     * 缓存配置管理器
     * @param factory
     * @return
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        // 配置序列化（缓存默认有效期 6小时）
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6));
        RedisCacheConfiguration redisCache = cacheConfig.serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        // 以锁写入的方式创建RedisCacheWriter对象
        //RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
        // 创建默认缓存配置对象
        /* 默认配置，设置缓存有效期 1小时*/
        //RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1));
        /* 自定义配置test:demo 的超时时间为 5分钟*/
        RedisCacheManager cacheManager = RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(lettuceConnectionFactory)).cacheDefaults(redisCache)
                .withInitialCacheConfigurations(singletonMap(CacheConstant.TEST_DEMO_CACHE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)).disableCachingNullValues()))
                .transactionAware().build();
        return cacheManager;
    }
}
