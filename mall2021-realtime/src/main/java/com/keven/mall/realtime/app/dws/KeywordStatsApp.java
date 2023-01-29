package com.keven.mall.realtime.app.dws;

import com.keven.mall.realtime.app.function.SplitFunction;
import com.keven.mall.realtime.bean.KeywordStats;
import com.keven.mall.realtime.utils.ClickHouseUtil;
import com.keven.mall.realtime.utils.MyKafkaUtil;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * @author KevenHe
 * @create 2022/1/22 19:22
 */
//��������web/app -> Nginx -> SpringBoot -> Kafka(ods) -> FlinkApp -> Kafka(dwd) -> FlinkApp -> ClickHouse
//��  ��mockLog -> Nginx -> Logger.sh  -> Kafka(ZK)  -> BaseLogApp -> kafka -> KeywordStatsApp -> ClickHouse
public class KeywordStatsApp {

    public static void main(String[] args) throws Exception {

        //TODO 1.��ȡִ�л���
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        //TODO 2.ʹ��DDL��ʽ��ȡKafka���ݴ�����
        String groupId = "keyword_stats_app";
        String pageViewSourceTopic = "dwd_page_log";
        tableEnv.executeSql("create table page_view( " +
                "    `common` Map<STRING,STRING>, " +
                "    `page` Map<STRING,STRING>, " +
                "    `ts` BIGINT, " +
                "    `rt` as TO_TIMESTAMP(FROM_UNIXTIME(ts/1000)), " +
                "    WATERMARK FOR rt AS rt - INTERVAL '1' SECOND " +
                ") with (" + MyKafkaUtil.getKafkaDDL(pageViewSourceTopic, groupId) + ")");

        //TODO 3.��������  ��һ��ҳ��Ϊ"search" and ������ is not null
        Table fullWordTable = tableEnv.sqlQuery("" +
                "select " +
                "    page['item'] full_word, " +
                "    rt " +
                "from  " +
                "    page_view " +
                "where " +
                "    page['last_page_id']='search' and page['item'] is not null");

        //TODO 4.ע��UDTF,���зִʴ���
        tableEnv.createTemporarySystemFunction("split_words", SplitFunction.class);
        Table wordTable = tableEnv.sqlQuery("" +
                "SELECT  " +
                "    word,  " +
                "    rt " +
                "FROM  " +
                "    " + fullWordTable + ", LATERAL TABLE(split_words(full_word))");

        //TODO 5.���顢�������ۺ�
        Table resultTable = tableEnv.sqlQuery("" +
                "select " +
                "    'search' source, " +
                "    DATE_FORMAT(TUMBLE_START(rt, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss') stt, " +
                "    DATE_FORMAT(TUMBLE_END(rt, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss') edt, " +
                "    word keyword, " +
                "    count(*) ct, " +
                "    UNIX_TIMESTAMP()*1000 ts " +
                "from " + wordTable + " " +
                "group by " +
                "    word, " +
                "    TUMBLE(rt, INTERVAL '10' SECOND)");

        //TODO 6.����̬��ת��Ϊ��
        DataStream<KeywordStats> keywordStatsDataStream = tableEnv.toAppendStream(resultTable, KeywordStats.class);

        //TODO 7.�����ݴ�ӡ��д��ClickHouse
        keywordStatsDataStream.print();
        keywordStatsDataStream.addSink(ClickHouseUtil.getSink("insert into keyword_stats_2021(keyword,ct,source,stt,edt,ts) values(?,?,?,?,?,?)"));

        //TODO 8.��������
        env.execute("KeywordStatsApp");
    }

}
