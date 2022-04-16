package org.alcove.wander.id.spring.boot.redis;

import org.alcove.wander.id.common.WanderDistributeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@SpringBootTest
public class BootWanderIdTest {

    @Resource
    private WanderDistributeId wanderDistributeId;

    @Test
    public void test(){
        System.out.println(wanderDistributeId.nextId());
    }


}
