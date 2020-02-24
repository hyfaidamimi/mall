package com.imooc.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVo {
    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;
}