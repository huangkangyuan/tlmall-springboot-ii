package fun.sherman.tlmall.task;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.RedissonManager;
import fun.sherman.tlmall.service.IOrderService;
import fun.sherman.tlmall.util.PropertiesUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author sherman
 */
@Component
@Slf4j
public class CloseOrderTask {
    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    @PreDestroy
    public void delLock() {
        ShardedRedisUtil.del(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
    }

    //@Scheduled(cron = "* */1 * * * ?")
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }

    /**
     * 分布式锁
     * 存在问题：
     * 如果setnx返回值表明获取锁成功，在即将进行关闭订单时，突然服务器关闭，会导致获取的分布式锁一直存在，没有关闭，形成死锁
     * 一种简单的解决思路：如果tomcat是使用shutdown命令关闭的，可以使用@PreDestory注解方法，关闭之前会被执行到
     * 但是如果是直接关闭tomcat服务器，使用@PreDestory注解没有作用
     */
    //@Scheduled(cron = "* */1 * * * ?")
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long result = ShardedRedisUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (result != null && result.intValue() == 1) {
            // 获取锁成功
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获取到分布式锁:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * 分布式锁：双重防死锁
     */
    @Scheduled(cron = "* */1 * * * ?")
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long result = ShardedRedisUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (result != null && result.intValue() == 1) {
            // 获取锁成功
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        } else {
            // 未获取到锁，继续判断时间戳，是否可以重置并得到锁
            String lockValueStr = ShardedRedisUtil.get(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && (System.currentTimeMillis() > Long.parseLong(lockValueStr))) {
                // 锁过期后，第一个来的线程getset后，获取的oldLockName的值一定和lockValueStr相等
                String oldLockName = ShardedRedisUtil.getSet(Const.RedisLock.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                if (oldLockName == null || StringUtils.equals(oldLockName, lockValueStr)) {
                    // 1. oldLockName==null，因为某种原因，Redis中的key已经不存在，当前线程可以获取锁
                    // 2. 在时间已经过期的情况下，两次获取key的值仍然相同（说明key未被其它线程改动过），当前线程可以获取锁
                    closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取到分布式锁:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                // 1. lockValueStr == null，说明之前的setnx失败，result==null，应该返回获取锁失败
                // 2. System.currentTimeMills() > Long.parseLong(lockValueStr)，说明tomcat在key的有效期内重启成功
                // 此时还在锁的有效期内，别的线程也不应该获取锁，返回获取锁失败
                log.info("没有获取到分布式锁:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * 分布式锁：通过Redisson完成
     */
    //@Scheduled(cron = "* */1 * * * ?")
    public void closeOrderTaskV4() {
        RLock rlock = redissonManager.getRedisson().getLock(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            // 注意tryLock的第一个参数waitTime实际中尽量设置为0，如果closeOrder()方法执行时间非常短
            // waitTime内已经执行完成并释放锁，下一个线程来就能够拿到锁，导致同一个Schedule执行时，两个
            // 线程都能拿到锁的情况
            getLock = rlock.tryLock(0, Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000")), TimeUnit.MINUTES);
            if (getLock) {
                log.info("获取锁:{},当前线程:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                iOrderService.closeOrder(hour);
            } else {
                log.info("未获取锁:{},当前线程:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (Exception e) {
            log.info("获取锁失败,当前线程:{}", Thread.currentThread().getName(), e);
        } finally {
            if (getLock) {
                rlock.unlock();
                log.info("释放锁:{},当前线程:{}", Const.RedisLock.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        }
    }

    private void closeOrder(String lockName) {
        ShardedRedisUtil.expire(lockName, 5);
        log.info("获取{}, ThreadName:{}", lockName, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        iOrderService.closeOrder(hour);
        ShardedRedisUtil.del(lockName);
        log.info("释放{}, ThreadName:{}", lockName, Thread.currentThread().getName());
    }
}
