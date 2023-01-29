package com.keven.mall.realtime.app.function;

import com.keven.mall.realtime.utils.KeywordUtil;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.List;

/**
 * @author KevenHe
 * @create 2022/1/22 19:22
 */
@FunctionHint(output = @DataTypeHint("ROW<word STRING>"))
public class SplitFunction extends TableFunction<Row> {

    public void eval(String str) {

        try {
            //�ִ�
            List<String> words = KeywordUtil.splitKeyWord(str);

            //������д��
            for (String word : words) {
                collect(Row.of(word));
            }

        } catch (IOException e) {
            collect(Row.of(str));
        }
    }
}