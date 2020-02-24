package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
@Slf4j
public class CartServiceImplTest extends MallApplicationTests {
    @Autowired
    private ICartService cartService;

    private Gson gson=new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void add() {
        CartAddForm form=new CartAddForm();
        form.setSelected(true);
        form.setProductId(27);
        cartService.add(form,1);
    }

    @Test
    public void  test(){
        ResponseVo<CartVo> list=cartService.list(1);
        log.info("list={}",gson.toJson(list));
    }

    @Test
    public void update(){
        CartUpdateForm cartUpdateForm = new CartUpdateForm();
        cartUpdateForm.setQuantity(2);
        cartUpdateForm.setSelected(true);
        log.info("list={}",gson.toJson(cartService.update(1,26,cartUpdateForm)));


    }

    @Test
    public void delete(){

        log.info("list={}",gson.toJson(cartService.delete(1,26)));


    }
}