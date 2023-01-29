package com.keven.mall.realtime.utils;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KevenHe
 * @create 2022/1/22 19:23
 */
public class KeywordUtil {

    public static List<String> splitKeyWord(String keyWord) throws IOException {

        //�����������ڴ�Ž������
        ArrayList<String> resultList = new ArrayList<>();

        StringReader reader = new StringReader(keyWord);

        IKSegmenter ikSegmenter = new IKSegmenter(reader, false);

        while (true) {
            Lexeme next = ikSegmenter.next();

            if (next != null) {
                String word = next.getLexemeText();
                resultList.add(word);
            } else {
                break;
            }
        }

        //���ؽ������
        return resultList;
    }

    public static void main(String[] args) throws IOException {

        System.out.println(splitKeyWord("�й�ȴ�������Ŀ֮ʵʱ����"));

    }
}

