package com.keven.mall.realtime.app.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.keven.mall.realtime.bean.TableProcess;
import com.keven.mall.realtime.common.GmallConfig;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author KevenHe
 * @create 2022/1/19 21:05
 */
public class TableProcessFunction extends BroadcastProcessFunction<JSONObject, String, JSONObject> {

    private OutputTag<JSONObject> objectOutputTag;
    private MapStateDescriptor<String, TableProcess> mapStateDescriptor;
    private Connection connection;

    public TableProcessFunction(OutputTag<JSONObject> objectOutputTag, MapStateDescriptor<String, TableProcess> mapStateDescriptor) {
        this.objectOutputTag = objectOutputTag;
        this.mapStateDescriptor = mapStateDescriptor;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Class.forName(GmallConfig.PHOENIX_DRIVER);
        connection = DriverManager.getConnection(GmallConfig.PHOENIX_SERVER);
    }

    //value:{"db":"","tn":"","before":{},"after":{},"type":""}
    @Override
    public void processBroadcastElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {
        //1.��ȡ����������
        JSONObject jsonObject = JSON.parseObject(value);
        String data = jsonObject.getString("after");
        TableProcess tableProcess = JSON.parseObject(data, TableProcess.class);
        //2.����
        if (TableProcess.SINK_TYPE_HBASE.equals(tableProcess.getSinkType())) {
            checkTable(tableProcess.getSinkTable(),
                    tableProcess.getSinkColumns(),
                    tableProcess.getSinkPk(),
                    tableProcess.getSinkExtend());
        }
        //3.д��״̬,�㲥��ȥ
        BroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        String key = tableProcess.getSourceTable() + "-" + tableProcess.getOperateType();
        broadcastState.put(key, tableProcess);
    }

    //������� : create table if not exists db.tn(id varchar primary key,tm_name varchar) xxx;
    private void checkTable(String sinkTable, String sinkColumns, String sinkPk, String sinkExtend) {

        PreparedStatement preparedStatement = null;

        try {
            if (sinkPk == null) {
                sinkPk = "id";
            }
            if (sinkExtend == null) {
                sinkExtend = "";
            }

            StringBuffer createTableSQL = new StringBuffer("create table if not exists ")
                    .append(GmallConfig.HBASE_SCHEMA)
                    .append(".")
                    .append(sinkTable)
                    .append("(");

            String[] fields = sinkColumns.split(",");

            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];

                //�ж��Ƿ�Ϊ����
                if (sinkPk.equals(field)) {
                    createTableSQL.append(field).append(" varchar primary key ");
                } else {
                    createTableSQL.append(field).append(" varchar ");
                }

                //�ж��Ƿ�Ϊ���һ���ֶ�,�������,�����","
                if (i < fields.length - 1) {
                    createTableSQL.append(",");
                }
            }

            createTableSQL.append(")").append(sinkExtend);

            //��ӡ�������
            System.out.println(createTableSQL);

            //Ԥ����SQL
            preparedStatement = connection.prepareStatement(createTableSQL.toString());

            //ִ��
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Phoenix��" + sinkTable + "����ʧ�ܣ�");
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //value:{"db":"","tn":"","before":{},"after":{},"type":""}
    @Override
    public void processElement(JSONObject value, ReadOnlyContext ctx, Collector<JSONObject> out) throws Exception {

        //1.��ȡ״̬����
        ReadOnlyBroadcastState<String, TableProcess> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
        String key = value.getString("tableName") + "-" + value.getString("type");
        TableProcess tableProcess = broadcastState.get(key);

        if (tableProcess != null) {

            //2.�����ֶ�
            JSONObject data = value.getJSONObject("after");
            filterColumn(data, tableProcess.getSinkColumns());

            //3.����
            //�������/������Ϣд��Value
            value.put("sinkTable", tableProcess.getSinkTable());
            String sinkType = tableProcess.getSinkType();
            if (TableProcess.SINK_TYPE_KAFKA.equals(sinkType)) {
                //Kafka����,д������
                out.collect(value);
            } else if (TableProcess.SINK_TYPE_HBASE.equals(sinkType)) {
                //HBase����,д��������
                ctx.output(objectOutputTag, value);
            }

        } else {
            System.out.println("�����Key��" + key + "�����ڣ�");
        }
    }

    /**
     * @param data        {"id":"11","tm_name":"keven","logo_url":"aaa"}
     * @param sinkColumns id,tm_name
     *                    {"id":"11","tm_name":"keven"}
     */
    private void filterColumn(JSONObject data, String sinkColumns) {

        String[] fields = sinkColumns.split(",");
        List<String> columns = Arrays.asList(fields);
        data.entrySet().removeIf(next -> !columns.contains(next.getKey()));

    }
}