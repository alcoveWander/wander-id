package org.alcove.wander.id.spring.redis;

import org.alcove.wander.id.common.WanderDistributeId;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/** spring wander id test
 * @author alcoveWander
 * @date 2022/04/16
 */
public class WanderIdTest {

    @Test
    public void test(){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[] {"classpath:/applicationContext.xml"});
        WanderDistributeId wanderDistributeId = (WanderDistributeId)applicationContext.getBean("wanderDistributeId");
        System.out.println(wanderDistributeId.nextId());
    }
}
