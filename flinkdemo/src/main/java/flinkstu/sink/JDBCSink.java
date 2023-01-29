package flinkstu.sink;

import flinkstu.bean.Sensor;
import flinkstu.transform.RichFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * @author KevenHe
 * @create 2022/2/14 18:06
 */
public class JDBCSink {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStream<String> inputStream =
                env.readTextFile("C:\\Users\\winqihe\\Desktop\\DataWareHouseFlink\\flinkdemo\\src\\main\\java\\flinkstu\\transform\\sensor.txt");

        DataStream<Sensor> dataStream = inputStream.map((MapFunction<String, Sensor>) value -> {
            String[] filed = value.split(",");
            return new Sensor(filed[0], new Long(filed[1]), new Double(filed[2]));

        });

        dataStream.addSink(new MyJdbcSink());
        env.execute();
    }
}
    //�Զ���sink Functioon
    class MyJdbcSink extends RichSinkFunction<Sensor> {
        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        // open ��Ҫ�Ǵ�������
        @Override
        public void open(Configuration parameters) throws Exception {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo?useSSL=false", "root", "010613");
            // ����Ԥ����������ռλ�����ɴ������
            insertStmt = conn.prepareStatement("INSERT INTO sensor_temp (id, temp) VALUES (?, ?)");
            updateStmt = conn.prepareStatement("UPDATE sensor_temp SET temp = ? WHERE id = ?");
        }
        // �������ӣ�ִ�� sql
        @Override
        public void invoke(Sensor value, Context context) throws Exception {
            // ִ�и�����䣬ע�ⲻҪ�� super
            updateStmt.setDouble(1, value.getTemperature());
            updateStmt.setString(2, value.getId());
            updateStmt.execute();
            // ����ղ� update ���û�и��£���ô����
            if (updateStmt.getUpdateCount() == 0) {
                insertStmt.setString(1, value.getId());
                insertStmt.setDouble(2, value.getTemperature());
                insertStmt.execute();
            }
        }
        @Override
        public void close() throws Exception {
            insertStmt.close();
            updateStmt.close();
            conn.close();
        }
    }