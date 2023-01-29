package com.keven.mall.realtime.bean;

/**
 * @author KevenHe
 * @create 2022/1/22 19:05
 * Desc: ��Ʒͳ��ʵ����
 *
 * @Builderע�� ����ʹ�ù����߷�ʽ�������󣬸����Ը�ֵ
 * @Builder.Default ��ʹ�ù����߷�ʽ�����Ը�ֵ��ʱ�����Եĳ�ʼֵ�ᶪʧ
 * ��ע������þ����޸��������
 * ���磺�����������ϸ�ֵ�˳�ʼֵΪ0L������������ע�⣬ͨ�������ߴ����Ķ�������ֵ���Ϊnull
 */
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class ProductStats {

    String stt;//������ʼʱ��
    String edt;  //���ڽ���ʱ��
    Long sku_id; //sku���
    String sku_name;//sku����
    BigDecimal sku_price; //sku����
    Long spu_id; //spu���
    String spu_name;//spu����
    Long tm_id; //Ʒ�Ʊ��
    String tm_name;//Ʒ������
    Long category3_id;//Ʒ����
    String category3_name;//Ʒ������

    @Builder.Default
    Long display_ct = 0L; //�ع���

    @Builder.Default
    Long click_ct = 0L;  //�����

    @Builder.Default
    Long favor_ct = 0L; //�ղ���

    @Builder.Default
    Long cart_ct = 0L;  //��ӹ��ﳵ��

    @Builder.Default
    Long order_sku_num = 0L; //�µ���Ʒ����

    @Builder.Default   //�µ���Ʒ���
            BigDecimal order_amount = BigDecimal.ZERO;

    @Builder.Default
    Long order_ct = 0L; //������

    @Builder.Default   //֧�����
            BigDecimal payment_amount = BigDecimal.ZERO;

    @Builder.Default
    Long paid_order_ct = 0L;  //֧��������

    @Builder.Default
    Long refund_order_ct = 0L; //�˿����

    @Builder.Default
    BigDecimal refund_amount = BigDecimal.ZERO;

    @Builder.Default
    Long comment_ct = 0L;//���۶�����

    @Builder.Default
    Long good_comment_ct = 0L; //����������

    @Builder.Default
    @TransientSink
    Set orderIdSet = new HashSet();  //����ͳ�ƶ�����

    @Builder.Default
    @TransientSink
    Set paidOrderIdSet = new HashSet(); //����ͳ��֧��������

    @Builder.Default
    @TransientSink
    Set refundOrderIdSet = new HashSet();//�����˿�֧��������

    Long ts; //ͳ��ʱ���
}