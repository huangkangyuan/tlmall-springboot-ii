package fun.sherman.tlmall.common;

import com.google.common.collect.Lists;
import fun.sherman.tlmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Hashing;
import redis.clients.jedis.util.Sharded;

import java.util.List;

/**
 * 从RedisPool迁移到ShardedRedisPool
 * 单个redis节点 -> 分布式redis架构
 *
 * @author sherman
 */
public class ShardedRedisPool {
    private static ShardedJedisPool shardedJedisPool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMaxTotal", "10"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMaxIdle", "5"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMinIdle", "2"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.testOnBorrow", "true"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.testOnReturn", "true"));
    private static Integer maxWait = Integer.parseInt(PropertiesUtil.getProperty("redis.maxWait", "3000"));
    private static String nodeHost1 = PropertiesUtil.getProperty("redis.node1.host", "123.56.239.187");
    private static Integer nodePort1 = Integer.parseInt(PropertiesUtil.getProperty("redis.node1.port", "6379"));
    private static String nodeHost2 = PropertiesUtil.getProperty("redis.node2.host", "123.56.239.187");
    private static Integer nodePort2 = Integer.parseInt(PropertiesUtil.getProperty("redis.node2.port", "6380"));
    private static String password = PropertiesUtil.getProperty("redis.password");
    private static Integer timeout = Integer.parseInt(PropertiesUtil.getProperty("redis.timeout", "1000"));

    private static void initJedisPool() {
        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxTotal(maxTotal);
        jpc.setMaxIdle(maxIdle);
        jpc.setMinIdle(minIdle);
        jpc.setTestOnBorrow(testOnBorrow);
        jpc.setTestOnReturn(testOnReturn);
        jpc.setTestOnCreate(false);
        jpc.setMaxWaitMillis(maxWait);
        //连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时，默认为true
        jpc.setBlockWhenExhausted(true);

        JedisShardInfo nodeInfo1 = new JedisShardInfo(nodeHost1, nodePort1, timeout);
        nodeInfo1.setPassword(password);
        JedisShardInfo nodeInfo2 = new JedisShardInfo(nodeHost2, nodePort2, timeout);
        nodeInfo2.setPassword(password);
        List<JedisShardInfo> infoLists = Lists.newArrayList(nodeInfo1, nodeInfo2);
        shardedJedisPool = new ShardedJedisPool(jpc, infoLists, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initJedisPool();
    }

    public static ShardedJedis getJedis() {
        return shardedJedisPool.getResource();
    }

//    /**
//     * 验证redis一致性hash算法key的分布，最终两个redis节点上key的分布：
//     * redis-6379：key4、key9、key7、key6
//     * redis-6380：key1、key5、key8、key0、key3、key2
//     */
//    public static void main(String[] args) {
//        ShardedJedis shardedJedis = shardedJedisPool.getResource();
//        for (int i = 0; i < 10; ++i) {
//            shardedJedis.set("key" + i, "value" + i);
//        }
//    }
}
