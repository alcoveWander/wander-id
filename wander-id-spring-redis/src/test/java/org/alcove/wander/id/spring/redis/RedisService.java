package org.alcove.wander.id.spring.redis;

import org.alcove.wander.id.common.WanderDistributeId;
import org.alcove.wander.id.common.WorkerIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

/** redis service
 * @author alcoveWander
 * @date 2022/04/16
 */
@Component
public class RedisService {

    @Resource
    private RedisConfig redisConfig;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(redisConfig.getDatabase());
        redisStandaloneConfiguration.setHostName(redisConfig.getHost());
        redisStandaloneConfiguration.setPort(redisConfig.getPort());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisConfig.getPassword()));
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisConfig.getTimeout()))
                .shutdownTimeout(Duration.ofMillis(redisConfig.getShutDownTimeout()))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
        // 这个属性默认是true,允许多个连接公用一个物理连接。如果设置false ,每一个连接的操作都会开启和关闭socket连接。如果设置为false,会导致性能下降
        factory.setShareNativeConnection(true);
        //每次获取连接时，校验连接是否可用。默认false,不去校验。默认情况下，lettuce开启一个共享的物理连接，是一个长连接，所以默认情况下是不会校验连接是否可用的。如果设置true,会导致性能下降
        factory.setValidateConnection(false);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory){
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return stringRedisTemplate;
    }


    @Bean
    public WorkerIdGenerator wanderIdRedisGenerator(StringRedisTemplate stringRedisTemplate, @Value("${spring.wander.worker.id.key:DEFAULT_WORKER_ID_KEY}") String workerIdRedisKey){
        return new WorkerIdRedisGenerator(stringRedisTemplate,workerIdRedisKey);
    }

    @Bean
    public WanderDistributeId wanderDistributeId(WorkerIdGenerator wanderIdRedisGenerator){
        return new WanderDistributeId(wanderIdRedisGenerator);
    }
}
