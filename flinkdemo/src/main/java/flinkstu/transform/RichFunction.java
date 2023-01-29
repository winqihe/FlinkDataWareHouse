package flinkstu.transform;

import flinkstu.bean.Sensor;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author KevenHe
 * @create 2022/2/14 17:48
 */
public class RichFunction {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<String> inputStream =
                env.readTextFile("C:\\Users\\winqihe\\Desktop\\DataWareHouseFlink\\flinkdemo\\src\\main\\java\\flinkstu\\api\\sensor.txt");

        DataStream<Sensor> dataStream = inputStream.map(new
            MapFunction<String, Sensor>() {
                public Sensor map(String value) throws Exception {
                    String[] filed = value.split(",");
                    return new Sensor(filed[0],new Long(filed[1]),new Double(filed[2]));
                }
            });

        DataStream<Tuple2<Integer,String>> resultStream = dataStream.map(new MyMapFunction());

        resultStream.print();
        env.execute();
    }
    public static class  MyMapper implements MapFunction<Sensor,Tuple2<String,Integer>>{

        @Override
        public Tuple2<String, Integer> map(Sensor value) throws Exception {
            return new Tuple2<>(value.getId(),value.getId().length());
        }
    }
    //ʵ���Զ��帻����
    public static class MyMapFunction extends RichMapFunction<Sensor,
            Tuple2<Integer, String>> {
        @Override
        public Tuple2<Integer, String> map(Sensor value) throws Exception {
            return new Tuple2<>(getRuntimeContext().getIndexOfThisSubtask(),
                    value.getId());
        }
        @Override
        public void open(Configuration parameters) throws Exception {
            System.out.println("my map open");
            // ���¿�����һЩ��ʼ�����������罨��һ���� HDFS ������
        }
        @Override
        public void close() throws Exception {
            System.out.println("my map close");
            // ������һЩ������������Ͽ��� HDFS ������
        }
    }
}
