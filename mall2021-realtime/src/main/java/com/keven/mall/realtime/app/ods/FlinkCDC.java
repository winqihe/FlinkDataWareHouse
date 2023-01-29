package com.keven.mall.realtime.app.ods;

import com.alibaba.ververica.cdc.connectors.mysql.MySQLSource;
import com.alibaba.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.alibaba.ververica.cdc.debezium.DebeziumSourceFunction;
import com.keven.mall.realtime.app.function.CustomerDeserialization;
import com.keven.mall.realtime.utils.MyKafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author KevenHe
 * @create 2022/1/17 10:35
 */

//ods ���ֱ�ӳ�ȡ����������
public class FlinkCDC{
    public static void main(String[] args) throws Exception{
        //1.��ȡִ�л���
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //2.ͨ��FlinkCDC����SourceFunction����ȡ����
        DebeziumSourceFunction<String> sourceFunction = MySQLSource.<String>builder()
                .hostname("hadoop102")
                .port(3306)
                .username("root")
                .password("000000")
                .databaseList("gmall2021")
                .deserializer(new CustomerDeserialization())
                .startupOptions(StartupOptions.latest())
//                .startupOptions(StartupOptions.initial())
                .build();
        DataStreamSource<String> streamSource = env.addSource(sourceFunction);

        //3.��ӡ���ݲ�������д��Kafka
        streamSource.print();
        String sinkTopic = "ods_base_db";
        streamSource.addSink(MyKafkaUtil.getKafkaProducer(sinkTopic));

        //4.��������
        env.execute("FlinkCDC");
    }
}

