package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.service.IProductService;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ResponseVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductServiceImplTest extends MallApplicationTests {
    @Autowired
    private IProductService productService;
    @Test
    public void list() {
        productService.list(100002,1,1);
    }
    @Test
    public void detail(){
        ResponseVo<ProductDetailVo> productVoResponseVo=productService.detail(26);
        Assert.assertEquals(ResponseEnum.SUCCESS.getCode(),productVoResponseVo.getStatus());
    }
}