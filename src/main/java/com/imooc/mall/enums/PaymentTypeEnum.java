package com.imooc.mall.enums;

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {
    PAY_ONLINE(1,"在线支付"),;
    private Integer code;
    private String desc;

    PaymentTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
