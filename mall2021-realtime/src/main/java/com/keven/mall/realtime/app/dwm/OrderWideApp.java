package com.keven.mall.realtime.app.dwm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.keven.mall.realtime.app.function.DimAsyncFunction;
import com.keven.mall.realtime.bean.OrderDetail;
import com.keven.mall.realtime.bean.OrderInfo;
import com.keven.mall.realtime.bean.OrderWide;
import com.keven.mall.realtime.utils.MyKafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author KevenHe
 * @create 2022/1/21 17:19
 */
//��������web/app -> nginx -> SpringBoot -> Mysql -> FlinkApp -> Kafka(ods) -> FlinkApp -> Kafka/Phoenix(dwd-dim) -> FlinkApp(redis) -> Kafka(dwm)
//��  ��MockDb -> Mysql -> FlinkCDC -> Kafka(ZK) -> BaseDbApp -> Kafka/Phoenix(zk/hdfs/hbase) -> OrderWideApp(Redis) -> Kafka
public class OrderWideApp {

    public static void main(String[] args) throws Exception {

        //TODO 1.��ȡִ�л���
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //TODO 2.��ȡKafka ��������� ��ת��ΪJavaBean����&��ȡʱ�������WaterMark
        String orderInfoSourceTopic = "dwd_order_info";
        String orderDetailSourceTopic = "dwd_order_detail";
        String orderWideSinkTopic = "dwm_order_wide";
        String groupId = "order_wide_group_0325";
        //oderInfoDS
        SingleOutputStreamOperator<OrderInfo> orderInfoDS = env.addSource(MyKafkaUtil.getKafkaConsumer(orderInfoSourceTopic, groupId))
                .map(line -> {
                        OrderInfo orderInfo = JSON.parseObject(line, OrderInfo.class);

                        String create_time = orderInfo.getCreate_time();
                        String[] dateTimeArr = create_time.split(" ");
                        orderInfo.setCreate_date(dateTimeArr[0]);
                        orderInfo.setCreate_hour(dateTimeArr[1].split(":")[0]);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        orderInfo.setCreate_ts(sdf.parse(create_time).getTime());

                        return orderInfo;
                }).assignTimestampsAndWatermarks(WatermarkStrategy.<OrderInfo>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<OrderInfo>() {
                                @Override
                                public long extractTimestamp(OrderInfo element, long recordTimestamp) {
                                        return element.getCreate_ts();
                                }
                        }));
        //orderDetailDS
        SingleOutputStreamOperator<OrderDetail> orderDetailDS = env.addSource(MyKafkaUtil.getKafkaConsumer(orderDetailSourceTopic, groupId))
                .map(line -> {
                        OrderDetail orderDetail = JSON.parseObject(line, OrderDetail.class);
                        String create_time = orderDetail.getCreate_time();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        orderDetail.setCreate_ts(sdf.parse(create_time).getTime());

                        return orderDetail;
                }).assignTimestampsAndWatermarks(WatermarkStrategy.<OrderDetail>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<OrderDetail>() {
                                @Override
                                public long extractTimestamp(OrderDetail element, long recordTimestamp) {
                                        return element.getCreate_ts();
                                }
                        }));

        //TODO 3.˫��JOIN
        SingleOutputStreamOperator<OrderWide> orderWideWithNoDimDS = orderInfoDS.keyBy(OrderInfo::getId)
                .intervalJoin(orderDetailDS.keyBy(OrderDetail::getOrder_id))
                .between(Time.seconds(-5), Time.seconds(5)) //���ɻ����и���ʱ�������ӳ�ʱ��
                .process(new ProcessJoinFunction<OrderInfo, OrderDetail, OrderWide>() {
                        @Override
                        public void processElement(OrderInfo orderInfo, OrderDetail orderDetail, Context ctx, Collector<OrderWide> out) throws Exception {
                                out.collect(new OrderWide(orderInfo, orderDetail));
                        }
                });

        //��ӡ����
        orderWideWithNoDimDS.print("orderWideWithNoDimDS>>>>>>>>>");

        //TODO 4.����ά����Ϣ  HBase Phoenix
//        orderWideWithNoDimDS.map(orderWide -> {
//            //�����û�ά��
//            Long user_id = orderWide.getUser_id();
//            //����user_id��ѯPhoenix�û���Ϣ
//            //���û���Ϣ������orderWide
//            //����
//            //SKU
//            //SPU
//            //������
//            //���ؽ��
//            return orderWide;
//        });

        //4.1 �����û�ά��
        SingleOutputStreamOperator<OrderWide> orderWideWithUserDS = AsyncDataStream.unorderedWait(
                orderWideWithNoDimDS,
                new DimAsyncFunction<OrderWide>("DIM_USER_INFO") {
                        @Override
                        public String getKey(OrderWide orderWide) {
                                return orderWide.getUser_id().toString();
                        }

                        @Override
                        public void join(OrderWide orderWide, JSONObject dimInfo) throws ParseException {
                                orderWide.setUser_gender(dimInfo.getString("GENDER"));

                                String birthday = dimInfo.getString("BIRTHDAY");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                long currentTs = System.currentTimeMillis();
                                long ts = sdf.parse(birthday).getTime();

                                long age = (currentTs - ts) / (1000 * 60 * 60 * 24 * 365L);

                                orderWide.setUser_age((int) age);
                        }
                },
                60,
                TimeUnit.SECONDS);

        //��ӡ����
//        orderWideWithUserDS.print("orderWideWithUserDS");

        //4.2 ��������ά��
        SingleOutputStreamOperator<OrderWide> orderWideWithProvinceDS = AsyncDataStream.unorderedWait(orderWideWithUserDS,
                new DimAsyncFunction<OrderWide>("DIM_BASE_PROVINCE") {
                        @Override
                        public String getKey(OrderWide orderWide) {
                                return orderWide.getProvince_id().toString();
                        }

                        @Override
                        public void join(OrderWide orderWide, JSONObject dimInfo) throws ParseException {
                                orderWide.setProvince_name(dimInfo.getString("NAME"));
                                orderWide.setProvince_area_code(dimInfo.getString("AREA_CODE"));
                                orderWide.setProvince_iso_code(dimInfo.getString("ISO_CODE"));
                                orderWide.setProvince_3166_2_code(dimInfo.getString("ISO_3166_2"));
                        }
                }, 60, TimeUnit.SECONDS);

        //4.3 ����SKUά��
        SingleOutputStreamOperator<OrderWide> orderWideWithSkuDS = AsyncDataStream.unorderedWait(
                orderWideWithProvinceDS, new DimAsyncFunction<OrderWide>("DIM_SKU_INFO") {
                        @Override
                        public void join(OrderWide orderWide, JSONObject jsonObject) throws ParseException {
                                orderWide.setSku_name(jsonObject.getString("SKU_NAME"));
                                orderWide.setCategory3_id(jsonObject.getLong("CATEGORY3_ID"));
                                orderWide.setSpu_id(jsonObject.getLong("SPU_ID"));
                                orderWide.setTm_id(jsonObject.getLong("TM_ID"));
                        }

                        @Override
                        public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getSku_id());
                        }
                }, 60, TimeUnit.SECONDS);

        //4.4 ����SPUά��
        SingleOutputStreamOperator<OrderWide> orderWideWithSpuDS = AsyncDataStream.unorderedWait(
                orderWideWithSkuDS, new DimAsyncFunction<OrderWide>("DIM_SPU_INFO") {
                        @Override
                        public void join(OrderWide orderWide, JSONObject jsonObject) throws ParseException {
                                orderWide.setSpu_name(jsonObject.getString("SPU_NAME"));
                        }

                        @Override
                        public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getSpu_id());
                        }
                }, 60, TimeUnit.SECONDS);

        //4.5 ����TMά��
        SingleOutputStreamOperator<OrderWide> orderWideWithTmDS = AsyncDataStream.unorderedWait(
                orderWideWithSpuDS, new DimAsyncFunction<OrderWide>("DIM_BASE_TRADEMARK") {
                        @Override
                        public void join(OrderWide orderWide, JSONObject jsonObject) throws ParseException {
                                orderWide.setTm_name(jsonObject.getString("TM_NAME"));
                        }

                        @Override
                        public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getTm_id());
                        }
                }, 60, TimeUnit.SECONDS);

        //4.6 ����Categoryά��
        SingleOutputStreamOperator<OrderWide> orderWideWithCategory3DS = AsyncDataStream.unorderedWait(
                orderWideWithTmDS, new DimAsyncFunction<OrderWide>("DIM_BASE_CATEGORY3") {
                        @Override
                        public void join(OrderWide orderWide, JSONObject jsonObject) throws ParseException {
                                orderWide.setCategory3_name(jsonObject.getString("NAME"));
                        }

                        @Override
                        public String getKey(OrderWide orderWide) {
                                return String.valueOf(orderWide.getCategory3_id());
                        }
                }, 60, TimeUnit.SECONDS);

        orderWideWithCategory3DS.print("orderWideWithCategory3DS>>>>>>>>>>>");

        //TODO 5.������д��Kafka
        orderWideWithCategory3DS
                .map(JSONObject::toJSONString)
                .addSink(MyKafkaUtil.getKafkaProducer(orderWideSinkTopic));

        //TODO 6.��������
        env.execute("OrderWideApp");
    }
}
