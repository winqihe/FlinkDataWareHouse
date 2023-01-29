package flinkstu.transform;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author KevenHe
 * @create 2022/2/14 11:04
 */
public class Transform {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<String> dataStream =
                env.readTextFile("C:\\Users\\winqihe\\Desktop\\DataWareHouseFlink\\flinkdemo\\src\\main\\java\\flinkstu\\api\\sensor.txt");

        // map. StringתΪ�������
        DataStream<Integer> mapStream = dataStream.map(new MapFunction<String, Integer>() {
            public Integer map(String value) throws Exception {
                return value.length();
            }
        });

        // flatmap. �����з��ֶ�
        DataStream<String> flatMapStream = dataStream.flatMap(new FlatMapFunction<String,
                String>() {
            public void flatMap(String value, Collector<String> out) throws Exception {
                String[] fields = value.split(",");
                for( String field: fields )
                    out.collect(field);
                }
        });

        //filter. ɸѡsensor_1��ͷ��id��Ӧ������
        DataStream<String> filterStream = dataStream.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                return s.startsWith("sensor_1");
            }
        });

        //��ӡ���
        mapStream.print("map");
        flatMapStream.print("flatMap");
        filterStream.print("filter");

        env.execute();
    }
}
