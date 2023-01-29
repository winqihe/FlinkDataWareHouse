package com.keven.mall.realtime.bean;

import lombok.Data;

/**
 * @author KevenHe
 * @create 2022/1/18 16:37
 */
@Data
public class TableProcess {
    public static final String SINK_TYPE_HBASE ="hbase";
    public static final String SINK_TYPE_KAFKA ="kafka";
    public static final String SINK_TYPE_CK = "clickhouse";
    //��Դ��
    String sourceTable;
    //�������� insert,update,delete
    String operateType;
    //������� hbase kafka
    String sinkType;
    //�����(����)
    String sinkTable;
    //����ֶ�
    String sinkColumns;
    //�����ֶ�
    String sinkPk;
    //������չ
    String sinkExtend;
}
