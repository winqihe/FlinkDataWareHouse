package com.keven.mall.realtime.utils;

import com.alibaba.fastjson.JSONObject;
import com.keven.mall.realtime.common.GmallConfig;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author KevenHe
 * @create 2022/1/21 18:35
 */
public class DimUtil {

    public static JSONObject getDimInfo(Connection connection, String tableName, String id) throws Exception {

        //��ѯPhoenix֮ǰ�Ȳ�ѯRedis
        Jedis jedis = RedisUtil.getJedis();
        //DIM:DIM_USER_INFO:143
        String redisKey = "DIM:" + tableName + ":" + id;
        String dimInfoJsonStr = jedis.get(redisKey);
        if (dimInfoJsonStr != null) {
            //���ù���ʱ��
            jedis.expire(redisKey, 24 * 60 * 60);
            //�黹����
            jedis.close();
            //���ؽ��
            return JSONObject.parseObject(dimInfoJsonStr);
        }

        //ƴ�Ӳ�ѯ���
        //select * from db.tn where id='18';
        String querySql = "select * from " + GmallConfig.HBASE_SCHEMA + "." + tableName +
                " where id='" + id + "'";

        //��ѯPhoenix
        List<JSONObject> queryList = JdbcUtil.queryList(connection, querySql, JSONObject.class, false);
        JSONObject dimInfoJson = queryList.get(0);

        //�ڷ��ؽ��֮ǰ,������д��Redis
        jedis.set(redisKey, dimInfoJson.toJSONString());
        jedis.expire(redisKey, 24 * 60 * 60);
        jedis.close();

        //���ؽ��
        return dimInfoJson;
    }

    public static void delRedisDimInfo(String tableName, String id) {
        Jedis jedis = RedisUtil.getJedis();
        String redisKey = "DIM:" + tableName + ":" + id;
        jedis.del(redisKey);
        jedis.close();
    }

    public static void main(String[] args) throws Exception {

        Class.forName(GmallConfig.PHOENIX_DRIVER);
        Connection connection = DriverManager.getConnection(GmallConfig.PHOENIX_SERVER);

//        long start = System.currentTimeMillis();
        System.out.println(getDimInfo(connection, "DIM_BASE_TRADEMARK", "15"));
//        long end = System.currentTimeMillis();
//        System.out.println(getDimInfo(connection, "DIM_USER_INFO", "143"));
//        long end2 = System.currentTimeMillis();
//        System.out.println(getDimInfo(connection, "DIM_USER_INFO", "143"));
//        long end3 = System.currentTimeMillis();

//        System.out.println(end - start);
//        System.out.println(end2 - end);
//        System.out.println(end3 - end2);

        connection.close();

    }

}

