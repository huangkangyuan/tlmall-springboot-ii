package fun.sherman.tlmall.util;

import fun.sherman.tlmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * 单个redis节点的工具类，该类随着架构演进被废除
 *
 * @author sherman
 * @See SharedRedisUtil
 */
@Slf4j
@Deprecated
public class RedisUtil {

    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key:{} value:{}", key, value, e);
        } finally {
            release(jedis);
        }
        return result;
    }

    /**
     * 设置key的过期时间，单位秒
     */
    public static Long expire(String key, int expires) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, expires);
        } catch (Exception e) {
            log.error("setExpire key:{} expires:{}", key, expires, e);
        } finally {
            release(jedis);
        }
        return result;
    }

    //exTime的单位是秒
    public static String setEx(String key, String value, int exTime) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key, exTime, value);
        } catch (Exception e) {
            log.error("setex key:{} value:{} error", key, value, e);
        } finally {
            release(jedis);
        }
        return result;
    }

    public static String get(String key) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key:{}", key, e);
        } finally {
            release(jedis);
        }
        return result;
    }

    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{}", key, e);
        } finally {
            release(jedis);
        }
        return result;
    }

    private static void release(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
