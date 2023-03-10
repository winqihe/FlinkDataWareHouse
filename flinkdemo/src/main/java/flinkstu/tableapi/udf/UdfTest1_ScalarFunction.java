package flinkstu.tableapi.udf;/**
 * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved
 * <p>
 * Project: FlinkTutorial
 * Package: com.atguigu.apitest.tableapi.udf
 * Version: 1.0
 * <p>
 * Created by wushengran on 2020/11/14 9:24
 */
import flinkstu.bean.Sensor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

/**
 * @ClassName: UdfTest1_ScalarFunction
 * @Description:
 * @Author: wushengran on 2020/11/14 9:24
 * @Version: 1.0
 */
public class UdfTest1_ScalarFunction {
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 1. 读取数据
        DataStreamSource<String> inputStream = env.readTextFile("D:\\Projects\\BigData\\FlinkTutorial\\src\\main\\resources\\sensor.txt");

        // 2. 转换成POJO
        DataStream<Sensor> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new Sensor(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        // 3. 将流转换成表
        Table sensorTable = tableEnv.fromDataStream(dataStream, "id, timestamp as ts, temperature as temp");

        // 4. 自定义标量函数，实现求id的hash值
        // 4.1 table API
        HashCode hashCode = new HashCode(23);
        // 需要在环境中注册UDF
        tableEnv.registerFunction("hashCode", hashCode);
        Table resultTable = sensorTable.select("id, ts, hashCode(id)");

        // 4.2 SQL
        tableEnv.createTemporaryView("sensor", sensorTable);
        Table resultSqlTable = tableEnv.sqlQuery("select id, ts, hashCode(id) from sensor");

        // 打印输出
        tableEnv.toAppendStream(resultTable, Row.class).print("result");
        tableEnv.toAppendStream(resultSqlTable, Row.class).print("sql");

        env.execute();
    }

    // 实现自定义的ScalarFunction
    public static class HashCode extends ScalarFunction{
        private int factor = 13;

        public HashCode(int factor) {
            this.factor = factor;
        }

        public int eval(String str){
            return str.hashCode() * factor;
        }
    }
}
