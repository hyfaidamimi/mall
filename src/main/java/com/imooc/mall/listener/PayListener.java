package com.imooc.mall.listener;

import com.google.gson.Gson;
import com.imooc.mall.pojo.PayInfo;
import com.imooc.mall.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RabbitListener(queues = "payNotify")
public class PayListener {
    @Autowired
    private IOrderService orderService;

    @RabbitHandler
    public void process(String msg){
        log.info("msg={}",msg);

        PayInfo payInfo=new Gson().fromJson(msg, PayInfo.class);
        if(payInfo.getPlatformStatus().equals("SUCCESS")){
            orderService.paid(payInfo.getOrderNo());
        }
    }
}
