package flinkstu.processfunction;

import flinkstu.bean.Sensor;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author KevenHe
 * @create 2022/2/16 11:55
 */
public class KeyedProcessFunc {
    public static void main(String[] args) {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //����ʱ��ΪeventTime
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //socket�ı���
        DataStreamSource<String> inputStream = env.socketTextStream("localhost", 7777);

        DataStream<Sensor> dataStream = inputStream.map((MapFunction<String, Sensor>) value -> {
            String[] filed = value.split(",");
            return new Sensor(filed[0], new Long(filed[1]), new Double(filed[2]));

        });
        //����KeyedProcessFunction,�ȷ�����Զ��崦��
        dataStream.keyBy("id")
                .process(new MyProcess())
                .print();
    }

        //�Զ��崦����
    public static class MyProcess extends KeyedProcessFunction<Tuple,Sensor,Integer>{

            @Override
            public void processElement(Sensor value, Context ctx, Collector<Integer> out) throws Exception {
                out.collect(value.getId().length());
                //context
                ctx.timestamp();
                ctx.getCurrentKey();
//                ctx.output();
                ctx.timerService().currentProcessingTime();
                ctx.timerService().currentWatermark();
                ctx.timerService().registerEventTimeTimer((value.getTimestamp()+10)*1000);
                ctx.timerService().registerProcessingTimeTimer(12);
                ctx.timerService().deleteEventTimeTimer(12);

            }
        }
}
