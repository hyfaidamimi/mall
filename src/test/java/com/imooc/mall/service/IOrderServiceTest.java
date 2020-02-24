package com.imooc.mall.service;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.vo.OrderVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
@Slf4j
public class IOrderServiceTest extends MallApplicationTests {

    @Autowired
    private IOrderService orderService;


    private Integer uid=1;

    private Integer shippingId=4;

    private Integer productId=26;

    private Gson gson=new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void create(){
        ResponseVo<OrderVo> responseVo=orderService.create(uid,shippingId);
        log.info("result={}",gson.toJson(responseVo));
    }

    @Test
    public void list(){
        ResponseVo<PageInfo> responseVo=orderService.list(uid,1,10);
        log.info("result={}",gson.toJson(responseVo));
    }

    @Test
    public void detail(){
        Long orderNo=Long.parseLong("1582109585329");
        ResponseVo<OrderVo> responseVo=orderService.detail(uid,orderNo);
        log.info("result={}",gson.toJson(responseVo));
    }

}