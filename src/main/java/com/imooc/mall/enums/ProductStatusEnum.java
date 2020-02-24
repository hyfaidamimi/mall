package com.imooc.mall.enums;

import lombok.Getter;

@Getter
public enum  ProductStatusEnum {
    ON_SELL(1),
    OFF_SELL(2),
    DELETE(3),
    ;
    private Integer code;

    ProductStatusEnum(Integer code) {
        this.code = code;
    }
}
