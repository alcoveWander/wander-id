package org.alcove.wander.id.spring.redis;

import org.alcove.wander.id.common.WorkerIdGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import java.util.Collections;

/**
 *  wander id generator by redis
 *  @author alcoveWander
 *  @date 2022/04/16
 */
public class WorkerIdRedisGenerator implements WorkerIdGenerator {

    /** spring redis template to get worker id */
    private final StringRedisTemplate redisTemplate;

    /** spring redis DefaultRedisScript to execute worker id */
    private final DefaultRedisScript<Long> redisScript;

    /**worker id key in redis to calculate worker id*/
    private final String workerIdRedisKey;

    public WorkerIdRedisGenerator(StringRedisTemplate redisTemplate, String workerIdRedisKey){
        super();
        this.redisTemplate = redisTemplate;
        this.workerIdRedisKey = workerIdRedisKey;
        DefaultRedisScript<Long> counterLuaScript = new DefaultRedisScript<>();
        counterLuaScript.setResultType(Long.class);
        counterLuaScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/counterLuaScript.lua")));
        this.redisScript = counterLuaScript;
    }

    @Override
    public long getWorkerId() {
        Long workerId = redisTemplate.execute(redisScript, Collections.singletonList(workerIdRedisKey));
        return workerId == null ? 0 : workerId;
    }
}
