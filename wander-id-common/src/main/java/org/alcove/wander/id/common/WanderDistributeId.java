package org.alcove.wander.id.common;

import java.util.UUID;

/**
 * * Wander distributeId <br>
 *  * Wander distributeId的结构如下(每部分用-分开):<br>
 *  * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 0000000000 - 000000000000 <br>
 *  * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 *  * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 *  * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 *  * 10位的数据机器位，可以部署在1024个节点<br>
 *  * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 *  * 加起来刚好64位，为一个Long型。<br>
 *  * Wander distributeId的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由机器ID作区分)，并且效率较高，经测试，每秒能够产生26万ID左右。
 *
 *  alcove wander distribute id
 *  @author alcoveWander
 *  @date 2022/04/16
 */
public class WanderDistributeId {


    // ==============================Fields===========================================
    /**
     * 开始时间截 (2022-04-16)
     */
    private final long twepoch = 1650097169896L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 10L;

    /**
     * 支持的最大机器id，结果是1023 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 时间截向左移22位(10+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~1023)
     */
    private long workerId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * constructor with one WanderIdGenerator
     * @param workerIdGenerator
     */
    public WanderDistributeId(WorkerIdGenerator workerIdGenerator){
        this(workerIdGenerator.getWorkerId());
    }

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~1023)
     */
    public WanderDistributeId(long workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
        System.out.println("wander worker id:"+workerId);
    }

    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return WanderId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate org.alcove.wander.id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        WanderDistributeId distributeId = new WanderDistributeId(1023);
        long l = System.currentTimeMillis();
        String id = null;
        for (int i =0;i<50000;i++){
            id = String.valueOf(distributeId.nextId());
        }
        System.out.println(System.currentTimeMillis() - l);
        System.out.println(id);
        long k = System.currentTimeMillis();
        for (int i =0;i<50000;i++){
            id = UUID.randomUUID().toString().replaceAll("-","");
        }
        System.out.println(System.currentTimeMillis() - k);
        System.out.println(id);
    }
}
