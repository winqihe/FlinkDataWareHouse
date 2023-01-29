package com.keven.mall.realtime.app.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.google.inject.internal.cglib.proxy.$FixedValue;
import com.keven.mall.realtime.utils.MyKafkaUtil;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author KevenHe
 * @create 2022/1/17 12:39
 */

//��������web/app -> Nginx -> SpringBoot -> Kafka(ods) -> FlinkApp -> Kafka(dwd)
//��  ��mockLog -> Nginx -> Logger.sh  -> Kafka(ZK)  -> BaseLogApp -> kafka
public class BaseLogApp {

    public static void main(String[] args) throws Exception {

        //TODO 1.��ȡִ�л���
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //TODO 2.���� ods_base_log �������ݴ�����
        String sourceTopic = "ods_base_log";
        String groupId = "base_log_app_210325";
        DataStreamSource<String> kafkaDS = env.addSource(MyKafkaUtil.getKafkaConsumer(sourceTopic, groupId));

        //TODO 3.��ÿ������ת��ΪJSON����(����������)
        OutputTag<String> outputTag = new OutputTag<String>("Dirty") {};
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDS.process(new ProcessFunction<String, JSONObject>() {
            @Override
            public void processElement(String value, Context ctx, Collector<JSONObject> out) throws Exception {
                try {
                    //��valueתΪJson��ʽ
                    JSONObject jsonObject = JSON.parseObject(value);
                    out.collect(jsonObject);
                } catch (Exception e) {
                    //�����쳣,������д��������
                    ctx.output(outputTag, value);
                }
            }
        });

        //TODO 4.�����û�У��  ״̬���

        SingleOutputStreamOperator<JSONObject> jsonObjWithNewFlagDS = jsonObjDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid"))
                .map(new RichMapFunction<JSONObject, JSONObject>() {
                    private ValueState<String> valueState;
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        valueState = getRuntimeContext().getState(new ValueStateDescriptor<String>("value-state", String.class));
                    }
                    @Override
                    public JSONObject map(JSONObject value) throws Exception {
                        //��ȡ�����е�"is_new"���
                        String isNew = value.getJSONObject("common").getString("is_new");
                        //�ж�isNew����Ƿ�Ϊ"1"
                        if ("1".equals(isNew)) {
                            //��ȡ״̬����
                            String state = valueState.value();
                            if (state != null) {
                                //�޸�isNew���
                                value.getJSONObject("common").put("is_new", "0");
                            } else {
                                valueState.update("1");
                            }
                        }
                        return value;
                    }
                });

        //TODO 5.����  �������  ҳ�棺����  �������������  �ع⣺�������
        OutputTag<String> startTag = new OutputTag<String>("start") {};
        OutputTag<String> displayTag = new OutputTag<String>("display") {};
        
        SingleOutputStreamOperator<String> pageDS = jsonObjWithNewFlagDS.process(new ProcessFunction<JSONObject, String>() {
            @Override
            public void processElement(JSONObject value, Context ctx, Collector<String> out) throws Exception {

                //��ȡ������־�ֶ�
                String start = value.getString("start");
                if (start != null && start.length() > 0) {
                    //������д��������־�������
                    ctx.output(startTag, value.toJSONString());
                } else {
                    //������д��ҳ����־����
                    out.collect(value.toJSONString());
                    //ȡ�������е��ع�����
                    JSONArray displays = value.getJSONArray("displays");
                    if (displays != null && displays.size() > 0) {
                        //��ȡҳ��ID
                        String pageId = value.getJSONObject("page").getString("page_id");
                        for (int i = 0; i < displays.size(); i++) {
                            JSONObject display = displays.getJSONObject(i);
                            //���ҳ��id
                            display.put("page_id", pageId);
                            //�����д�����ع�������
                            ctx.output(displayTag, display.toJSONString());
                        }
                    }
                }
            }
        });

        //TODO 6.��ȡ�������
        //���ݿ���ͨ��filter��split��sideoutput�������Ƽ�ʹ��outputtag
        DataStream<String> startDS = pageDS.getSideOutput(startTag);
        DataStream<String> displayDS = pageDS.getSideOutput(displayTag);

        //TODO 7.�����������д�ӡ���������Ӧ��Kafka������
        startDS.print("Start>>>>>>>>>>>");
        pageDS.print("Page>>>>>>>>>>>");
        displayDS.print("Display>>>>>>>>>>>>");

        startDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_start_log"));
        pageDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_page_log"));
        displayDS.addSink(MyKafkaUtil.getKafkaProducer("dwd_display_log"));

        //TODO 8.��������
        env.execute("BaseLogApp");

    }

}