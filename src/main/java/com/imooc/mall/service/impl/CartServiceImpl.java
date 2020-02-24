package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductStatusEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartProductVo;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartServiceImpl implements ICartService {
    private final static String CART_REDIS_KEY_TEMPLATE="cat_%d";
    private Integer quantity=1;
    private Gson gson=new Gson();
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public ResponseVo<CartVo> add(CartAddForm form,Integer uid) {
        Product product=productMapper.selectByPrimaryKey(form.getProductId());
        //商品是否存在
        if(product==null){
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }
        //商品是否正常在售
        if(!product.getStatus().equals(ProductStatusEnum.ON_SELL.getCode())){
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SELL_OR_DELETE);
        }
        //产品库存是否充足
        if(product.getStock()<=0){
            return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }
        //写入到redis
        //key:cart_1
        HashOperations<String, String, String> opsForHash= redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        String value= opsForHash.get(redisKey,String.valueOf(product.getId()));
        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品，新增
             cart=new Cart(product.getId(),quantity,form.getSelected());
        }else{
            //已经有了，数量+1
            cart=gson.fromJson(value,Cart.class);
            cart.setQuantity(cart.getQuantity()+quantity);

        }
                opsForHash.put(String.format(CART_REDIS_KEY_TEMPLATE,uid),
                String.valueOf(product.getId()),
                gson.toJson(cart)
                );
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String,String,String> opsForHash=redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        Map<String,String> entries=opsForHash.entries(redisKey);
        boolean selectAll=true;
        BigDecimal totalPrice=BigDecimal.ZERO;
        Integer totalQuantity=0;
        Set<Integer> productIds=new HashSet<>();
        Map<Integer,Cart> cartMap=new HashMap<>();
        for(Map.Entry<String,String> entry:entries.entrySet()){
            Integer productId=Integer.valueOf(entry.getKey());
            productIds.add(productId);
            cartMap.put(productId,gson.fromJson(entry.getValue(),Cart.class));
            if(!gson.fromJson(entry.getValue(),Cart.class).getProductSelected()){
                selectAll=false;
            }
        }
        List<CartProductVo> cartProductVos=new ArrayList<>();
        List<Product> products=new ArrayList<>();
        if(productIds.size()!=0){
            products=productMapper.selectByPrimaryKeys(productIds);
        }
        if(products!=null){
            for(Product p:products){
              CartProductVo cartProductVo=new CartProductVo(
                      p.getId(),
                      cartMap.get(p.getId()).getQuantity(),
                      p.getName(),
                      p.getSubtitle(),
                      p.getMainImage(),
                      p.getPrice(),
                      p.getStatus(),
                      p.getPrice().multiply(BigDecimal.valueOf(cartMap.get(p.getId()).getQuantity())),
                      p.getStock(),
                      cartMap.get(p.getId()).getProductSelected());
              cartProductVos.add(cartProductVo);
              if(cartMap.get(p.getId()).getProductSelected()) {
                  totalPrice = totalPrice.add(cartProductVo.getProductTotalPrice());
              }
              totalQuantity+=cartMap.get(p.getId()).getQuantity();
            }
        }
        CartVo cartVo=new CartVo();
        cartVo.setSelectedAll(selectAll);
        cartVo.setCartTotalPrice(totalPrice);
        cartVo.setCartTotalQuantity(totalQuantity);
        cartVo.setCartProductVoList(cartProductVos);
        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm form) {
        HashOperations<String, String, String> opsForHash= redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        String value= opsForHash.get(redisKey,String.valueOf(productId));
        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品，报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }else{
            //已经有了，数量+1
            cart=gson.fromJson(value,Cart.class);
            cart.setQuantity(cart.getQuantity()+quantity);
            if(form.getQuantity()!=null&&form.getQuantity()>=0){
                cart.setQuantity(form.getQuantity());
            }if(form.getSelected()!=null){
                cart.setProductSelected(form.getSelected());
            }
            opsForHash.put(redisKey,String.valueOf(productId),gson.toJson(cart));
            return list(uid);
        }

    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        HashOperations<String, String, String> opsForHash= redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        String value= opsForHash.get(redisKey,String.valueOf(productId));
        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品，报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }else{
            opsForHash.delete(redisKey,String.valueOf(productId));
            return list(uid);
        }

    }

    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String,String,String> opsForHash=redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        for (Cart cart:listForCart(uid)){
            cart.setProductSelected(true);
            opsForHash.put(redisKey,String.valueOf(cart.getProductId()),
                    gson.toJson(cart)
                    );
        }
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String,String,String> opsForHash=redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        for (Cart cart:listForCart(uid)){
            cart.setProductSelected(false);
            opsForHash.put(redisKey,String.valueOf(cart.getProductId()),
                    gson.toJson(cart)
            );
        }
        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        Integer sum=listForCart(uid).stream().map(Cart::getQuantity).reduce(0,Integer::sum);
        return ResponseVo.success(sum);
    }


    public List<Cart> listForCart(Integer uid){
        HashOperations<String,String,String> opsForHash=redisTemplate.opsForHash();
        String redisKey=String.format(CART_REDIS_KEY_TEMPLATE,uid);
        Map<String,String> entries=opsForHash.entries(redisKey);
        List<Cart> cartList=new ArrayList<>();
        for(Map.Entry<String,String> entry:entries.entrySet()){
            Cart cart;
            cart=gson.fromJson(entry.getValue(),Cart.class);
            cartList.add(cart);
        }
        return cartList;
    }

}
