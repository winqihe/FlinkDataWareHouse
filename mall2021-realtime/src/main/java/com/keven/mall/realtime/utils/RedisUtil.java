package com.keven.mall.realtime.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author KevenHe
 * @create 2022/1/21 18:44
 */
public class RedisUtil {

    public static JedisPool jedisPool = null;

    public static Jedis getJedis() {

        if (jedisPool == null) {

            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(100); //������������
            jedisPoolConfig.setBlockWhenExhausted(true); //���Ӻľ��Ƿ�ȴ�
            jedisPoolConfig.setMaxWaitMillis(2000); //�ȴ�ʱ��
            jedisPoolConfig.setMaxIdle(5); //�������������
            jedisPoolConfig.setMinIdle(5); //��С����������
            jedisPoolConfig.setTestOnBorrow(true); //ȡ���ӵ�ʱ�����һ�²��� ping pong

            jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 10000);

            System.out.println("�������ӳ�");
            return jedisPool.getResource();

        } else {
//            System.out.println(" ���ӳ�:" + jedisPool.getNumActive());
            return jedisPool.getResource();
        }
    }
}
