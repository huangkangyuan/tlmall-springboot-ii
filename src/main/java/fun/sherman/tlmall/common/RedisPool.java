package fun.sherman.tlmall.common;

import fun.sherman.tlmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 单个redis节点，该类随着架构演进被废除
 *
 * @author sherman
 * @See ShardedRedisPool
 */
@Deprecated
public class RedisPool {
    private static JedisPool jedisPool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMaxTotal", "10"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMaxIdle", "5"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.poolMinIdle", "2"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.testOnBorrow", "true"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.testOnReturn", "true"));
    private static Integer maxWait = Integer.parseInt(PropertiesUtil.getProperty("redis.maxWait", "3000"));
    private static String host = PropertiesUtil.getProperty("redis.node1.host", "123.56.239.187");
    private static Integer port = Integer.parseInt(PropertiesUtil.getProperty("redis.port", "6379"));
    private static String password = PropertiesUtil.getProperty("redis.password");
    private static Integer timeout = Integer.parseInt(PropertiesUtil.getProperty("redis.timeout", "1000"));

    private static void initJedisPool() {
        JedisPoolConfig jpc = new JedisPoolConfig();
        jpc.setMaxTotal(maxTotal);
        jpc.setMaxIdle(maxIdle);
        jpc.setMinIdle(minIdle);
        jpc.setTestOnBorrow(testOnBorrow);
        jpc.setTestOnReturn(testOnReturn);
        jpc.setMaxWaitMillis(maxWait);
        jedisPool = new JedisPool(jpc, host, port, timeout, password);
    }

    static {
        initJedisPool();
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
