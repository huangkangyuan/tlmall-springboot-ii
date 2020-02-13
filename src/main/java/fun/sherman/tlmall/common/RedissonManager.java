package fun.sherman.tlmall.common;

import fun.sherman.tlmall.util.PropertiesUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author sherman
 */
@Component
@Slf4j
public class RedissonManager {
    private Config config = new Config();
    private Redisson redisson;

    public Redisson getRedisson() {
        return redisson;
    }

    public void setRedisson(Redisson redisson) {
        this.redisson = redisson;
    }

    private static String nodeHost1 = PropertiesUtil.getProperty("redis.node1.host", "123.56.239.187");
    private static Integer nodePort1 = Integer.parseInt(PropertiesUtil.getProperty("redis.node1.port", "6379"));
//     // 这里使用的是useSingleServer()
//    private static String nodeHost2 = PropertiesUtil.getProperty("redis.node2.host", "123.56.239.187");
//    private static Integer nodePort2 = Integer.parseInt(PropertiesUtil.getProperty("redis.node2.port", "6380"));

    @PostConstruct
    public void init() {
        try {
            config.useSingleServer()
                    .setAddress("redis://" + nodeHost1 + ":" + nodePort1)
                    // 如果redis设置密码的话
                    .setPassword(PropertiesUtil.getProperty("redis.password"));
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化Redisson成功");
        } catch (Exception e) {
            log.info("初始化Redisson成功", e);
        }
    }
}
