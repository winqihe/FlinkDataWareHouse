package com.keven.mall.realtime.utils;

/**
 * @author KevenHe
 * @create 2022/1/22 18:54
 */

import com.keven.mall.realtime.common.GmallConfig;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.keven.mall.realtime.bean.TransientSink;

/**
 * Desc: ���� ClickHouse �Ĺ�����
 */
//obj.getField()   =>  field.get(obj)
//obj.method(args) =>  method.invoke(obj,args)
public class ClickHouseUtil {

    public static <T> SinkFunction<T> getSink(String sql) {

        return JdbcSink.<T>sink(sql,
                new JdbcStatementBuilder<T>() {
                    @Override
                    public void accept(PreparedStatement preparedStatement, T t) throws SQLException {
                        try {
                            //��ȡ���е�������Ϣ
                            Field[] fields = t.getClass().getDeclaredFields();

                            //�����ֶ�
                            int offset = 0;
                            for (int i = 0; i < fields.length; i++) {

                                //��ȡ�ֶ�
                                Field field = fields[i];

                                //����˽�����Կɷ���
                                field.setAccessible(true);

                                //��ȡ�ֶ���ע��
                                TransientSink annotation = field.getAnnotation(TransientSink.class);
                                if (annotation != null) {
                                    //���ڸ�ע��
                                    offset++;
                                    continue;
                                }

                                //��ȡֵ
                                Object value = field.get(t);

                                //��Ԥ����SQL����ֵ
                                preparedStatement.setObject(i + 1 - offset, value);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new JdbcExecutionOptions.Builder()
                        .withBatchSize(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withDriverName(GmallConfig.CLICKHOUSE_DRIVER)
                        .withUrl(GmallConfig.CLICKHOUSE_URL)
                        .build());

    }

}
