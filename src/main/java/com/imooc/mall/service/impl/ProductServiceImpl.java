package com.imooc.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductStatusEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.service.IProductService;
import com.imooc.mall.vo.ProductDetailVo;
import com.imooc.mall.vo.ProductVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private ProductMapper productMapper;
    @Override
    public ResponseVo<PageInfo> list(Integer categoryId, Integer pageNum, Integer pageSize) {
        Set<Integer> categoryIdSet=new HashSet<>();
        if(categoryId!=null){
            categoryService.findSubCategoryId(categoryId,categoryIdSet);
            categoryIdSet.add(categoryId);
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList=productMapper.selectByCategoryIdSet(categoryIdSet);
        List<ProductVo> productVos=productList.stream().map(e->{
            ProductVo productVo=new ProductVo();
            BeanUtils.copyProperties(e,productVo);
            return productVo;
        }).collect(Collectors.toList());
        PageInfo pageInfo=new PageInfo(productList);
        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<ProductDetailVo> detail(Integer productId) {
        ProductDetailVo productDetailVo=new ProductDetailVo();
        Product product=productMapper.selectByPrimaryKey(productId);
        BeanUtils.copyProperties(product,productDetailVo);
        //只对确定性的条件判断
        if(productDetailVo.getStatus().equals(ProductStatusEnum.OFF_SELL.getCode())||
                productDetailVo.getStatus().equals(ProductStatusEnum.DELETE.getCode())){
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SELL_OR_DELETE);
        }
        productDetailVo.setStock(product.getStock()>100?100:product.getStock());
        return ResponseVo.success(productDetailVo);
    }
}
