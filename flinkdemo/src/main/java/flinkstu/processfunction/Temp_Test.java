package flinkstu.processfunction;

import flinkstu.bean.Sensor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author KevenHe
 * @create 2022/2/16 12:08
 */
public class Temp_Test {
        public static void main(String[] args) throws Exception{
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);

            // socket�ı���
            DataStream<String> inputStream = env.socketTextStream("localhost", 7777);

            // ת����SensorReading����
            DataStream<Sensor> dataStream = inputStream.map(line -> {
                String[] fields = line.split(",");
                return new Sensor(fields[0], new Long(fields[1]), new Double(fields[2]));
            });

            // ����KeyedProcessFunction���ȷ���Ȼ���Զ��崦��
            dataStream.keyBy("id")
                    .process( new TempConsIncreWarning(10) )
                    .print();

            env.execute();
        }

        // ʵ���Զ��崦���������һ��ʱ���ڵ��¶������������������
        public static class TempConsIncreWarning extends KeyedProcessFunction<Tuple, Sensor, String>{
            // ����˽�����ԣ���ǰͳ�Ƶ�ʱ����
            private Integer interval;

            public TempConsIncreWarning(Integer interval) {
                this.interval = interval;
            }

            // ����״̬��������һ�ε��¶�ֵ����ʱ��ʱ���
            private ValueState<Double> lastTempState;
            private ValueState<Long> timerTsState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastTempState = getRuntimeContext().getState(new ValueStateDescriptor<Double>("last-temp", Double.class, Double.MIN_VALUE));
                timerTsState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("timer-ts", Long.class));
            }

            @Override
            public void processElement(Sensor value, Context ctx, Collector<String> out) throws Exception {
                // ȡ��״̬
                Double lastTemp = lastTempState.value();
                Long timerTs = timerTsState.value();

                // ����¶���������û�ж�ʱ����ע��10���Ķ�ʱ������ʼ�ȴ�
                if( value.getTemperature() > lastTemp && timerTs == null ){
                    // �������ʱ��ʱ���
                    Long ts = ctx.timerService().currentProcessingTime() + interval * 1000L;
                    ctx.timerService().registerProcessingTimeTimer(ts);
                    timerTsState.update(ts);
                }
                // ����¶��½�����ôɾ����ʱ��
                else if( value.getTemperature() < lastTemp && timerTs != null ){
                    ctx.timerService().deleteProcessingTimeTimer(timerTs);
                    timerTsState.clear();
                }

                // �����¶�״̬
                lastTempState.update(value.getTemperature());
            }

            @Override
            public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                // ��ʱ�����������������Ϣ
                out.collect("������" + ctx.getCurrentKey().getField(0) + "�¶�ֵ����" + interval + "s����");
                timerTsState.clear();
            }

            @Override
            public void close() throws Exception {
                lastTempState.clear();
            }
        }
}
