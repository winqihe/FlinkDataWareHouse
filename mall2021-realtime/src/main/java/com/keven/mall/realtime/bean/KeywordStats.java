package com.keven.mall.realtime.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KevenHe
 * @create 2022/1/22 19:21
 * �ؼ���ͳ��ʵ����
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeywordStats {
    private String keyword;
    private Long ct;
    private String source;
    private String stt;
    private String edt;
    private Long ts;
}