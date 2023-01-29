package com.keven.mall.realtime.common;

/**
 * @author KevenHe
 * @create 2022/1/22 19:10
 * ����ҵ����
 */
public class GmallConstant {
    //10 ����״̬
    public static final String ORDER_STATUS_UNPAID = "1001";  //δ֧��
    public static final String ORDER_STATUS_PAID = "1002"; //��֧��
    public static final String ORDER_STATUS_CANCEL = "1003";//��ȡ��
    public static final String ORDER_STATUS_FINISH = "1004";//�����
    public static final String ORDER_STATUS_REFUND = "1005";//�˿���
    public static final String ORDER_STATUS_REFUND_DONE = "1006";//�˿����

    //11 ֧��״̬
    public static final String PAYMENT_TYPE_ALIPAY = "1101";//֧����
    public static final String PAYMENT_TYPE_WECHAT = "1102";//΢��
    public static final String PAYMENT_TYPE_UNION = "1103";//����

    //12 ����
    public static final String APPRAISE_GOOD = "1201";// ����
    public static final String APPRAISE_SOSO = "1202";// ����
    public static final String APPRAISE_BAD = "1203";//  ����
    public static final String APPRAISE_AUTO = "1204";// �Զ�

    //13 �˻�ԭ��
    public static final String REFUND_REASON_BAD_GOODS = "1301";// ��������
    public static final String REFUND_REASON_WRONG_DESC = "1302";// ��Ʒ������ʵ��������һ��
    public static final String REFUND_REASON_SALE_OUT = "1303";//   ȱ��
    public static final String REFUND_REASON_SIZE_ISSUE = "1304";//  ���벻����
    public static final String REFUND_REASON_MISTAKE = "1305";//  �Ĵ�
    public static final String REFUND_REASON_NO_REASON = "1306";//  ��������
    public static final String REFUND_REASON_OTHER = "1307";//    ����

    //14 ����ȯ״̬
    public static final String COUPON_STATUS_UNUSED = "1401";//   δʹ��
    public static final String COUPON_STATUS_USING = "1402";//     ʹ����
    public static final String COUPON_STATUS_USED = "1403";//      ��ʹ��

    //15�˿�����
    public static final String REFUND_TYPE_ONLY_MONEY = "1501";//   ���˿�
    public static final String REFUND_TYPE_WITH_GOODS = "1502";//   �˻��˿�

    //24��Դ����
    public static final String SOURCE_TYPE_QUREY = "2401";//   �û���ѯ
    public static final String SOURCE_TYPE_PROMOTION = "2402";//   ��Ʒ�ƹ�
    public static final String SOURCE_TYPE_AUTO_RECOMMEND = "2403";//   �����Ƽ�
    public static final String SOURCE_TYPE_ACTIVITY = "2404";//   �����

    //����ȯ��Χ
    public static final String COUPON_RANGE_TYPE_CATEGORY3 = "3301";//
    public static final String COUPON_RANGE_TYPE_TRADEMARK = "3302";//
    public static final String COUPON_RANGE_TYPE_SPU = "3303";//

    //����ȯ����
    public static final String COUPON_TYPE_MJ = "3201";//����
    public static final String COUPON_TYPE_DZ = "3202";//��������
    public static final String COUPON_TYPE_DJ = "3203";//����ȯ

    public static final String ACTIVITY_RULE_TYPE_MJ = "3101";
    public static final String ACTIVITY_RULE_TYPE_DZ = "3102";
    public static final String ACTIVITY_RULE_TYPE_ZK = "3103";

    public static final String KEYWORD_SEARCH = "SEARCH";
    public static final String KEYWORD_CLICK = "CLICK";
    public static final String KEYWORD_CART = "CART";
    public static final String KEYWORD_ORDER = "ORDER";
}