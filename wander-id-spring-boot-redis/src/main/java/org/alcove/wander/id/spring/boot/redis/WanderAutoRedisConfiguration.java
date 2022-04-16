package org.alcove.wander.id.spring.boot.redis;

import org.alcove.wander.id.common.WanderDistributeId;
import org.alcove.wander.id.common.WorkerIdGenerator;
import org.alcove.wander.id.spring.redis.WorkerIdRedisGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class WanderAutoRedisConfiguration {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(){
        return new StringRedisTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(WorkerIdGenerator.class)
    public WorkerIdGenerator wanderIdRedisGenerator(StringRedisTemplate stringRedisTemplate, @Value("${spring.wander.worker.id.key:DEFAULT_WORKER_ID_KEY}") String workerIdRedisKey){
        return new WorkerIdRedisGenerator(stringRedisTemplate,workerIdRedisKey);
    }

    @Bean
    @ConditionalOnMissingBean(WanderDistributeId.class)
    public WanderDistributeId wanderDistributeId(WorkerIdGenerator wanderIdRedisGenerator){
        return new WanderDistributeId(wanderIdRedisGenerator);
    }
}
