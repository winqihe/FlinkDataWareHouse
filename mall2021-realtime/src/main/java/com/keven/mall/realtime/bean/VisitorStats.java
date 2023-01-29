package com.keven.mall.realtime.bean;

/**
 * @author KevenHe
 * @create 2022/1/22 15:43
 */

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Desc: �ÿ�ͳ��ʵ���� ��������ά�ȺͶ���
 */
@Data
@AllArgsConstructor
public class VisitorStats {
    //ͳ�ƿ�ʼʱ��
    private String stt;
    //ͳ�ƽ���ʱ��
    private String edt;
    //ά�ȣ��汾
    private String vc;
    //ά�ȣ�����
    private String ch;
    //ά�ȣ�����
    private String ar;
    //ά�ȣ������û���ʶ
    private String is_new;
    //�����������ÿ���
    private Long uv_ct=0L;
    //������ҳ�������
    private Long pv_ct=0L;
    //������ �������
    private Long sv_ct=0L;
    //������ ��������
    private Long uj_ct=0L;
    //������ ��������ʱ��
    private Long dur_sum=0L;
    //ͳ��ʱ��
    private Long ts;
}
