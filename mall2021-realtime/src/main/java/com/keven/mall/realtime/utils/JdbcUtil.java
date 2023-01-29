package com.keven.mall.realtime.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.keven.mall.realtime.common.GmallConfig;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KevenHe
 * @create 2022/1/21 18:36
 */
public class JdbcUtil {

    /**
     * select * from t1;
     * xx,xx,xx
     * xx,xx,xx
     */

    public static <T> List<T> queryList(Connection connection, String querySql, Class<T> clz, boolean underScoreToCamel) throws Exception {

        //�����������ڴ�Ų�ѯ�������
        ArrayList<T> resultList = new ArrayList<>();

        //Ԥ����SQL
        PreparedStatement preparedStatement = connection.prepareStatement(querySql);

        //ִ�в�ѯ
        ResultSet resultSet = preparedStatement.executeQuery();

        //����resultSet
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {

            //�������Ͷ�
            T t = clz.newInstance();

            //�����Ͷ���ֵ
            for (int i = 1; i < columnCount + 1; i++) {

                //��ȡ����
                String columnName = metaData.getColumnName(i);

                //�ж��Ƿ���Ҫת��Ϊ�շ�����
                if (underScoreToCamel) {
                    columnName = CaseFormat.LOWER_UNDERSCORE
                            .to(CaseFormat.LOWER_CAMEL, columnName.toLowerCase());
                }

                //��ȡ��ֵ
                Object value = resultSet.getObject(i);

                //�����Ͷ���ֵ
                //BeanUtils.copyProperty(t, columnName, value); JSONObject => {}

                BeanUtils.setProperty(t, columnName, value);

            }

            //���ö������������
            resultList.add(t);
        }

        preparedStatement.close();
        resultSet.close();

        //���ؽ������
        return resultList;
    }

    public static void main(String[] args) throws Exception {

//        System.out.println(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, "aa_bb"));

        Class.forName(GmallConfig.PHOENIX_DRIVER);
        Connection connection = DriverManager.getConnection(GmallConfig.PHOENIX_SERVER);

        List<JSONObject> queryList = queryList(connection,
                "select * from GMALL2021_REALTIME.DIM_USER_INFO",
                JSONObject.class,
                true);

        for (JSONObject jsonObject : queryList) {
            System.out.println(jsonObject);
        }

        connection.close();

    }

}
