package flinkstu.source;

import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author KevenHe
 * @create 2022/2/13 22:15
 */
public class Env {
    //����ִ�л���
    StreamExecutionEnvironment env =
            StreamExecutionEnvironment.getExecutionEnvironment();
    //���ر���ִ�л���
    LocalStreamEnvironment localEnv =
            StreamExecutionEnvironment.createLocalEnvironment(1);
    //Զ��ִ�л���
    StreamExecutionEnvironment sEnv =
            StreamExecutionEnvironment.createRemoteEnvironment(
                    "jobmanage-hostname", 6123,"YOURPATH//WordCount.jar"
                    );


}
