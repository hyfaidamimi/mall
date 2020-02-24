package com.imooc.mall.service.impl;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.dao.CategoryMapper;
import com.imooc.mall.pojo.Category;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public ResponseVo<List<CategoryVo>> listAll() {
        List<Category> categories=categoryMapper.selectAll();
        List<CategoryVo>  categoryVoList=categories.stream().
                filter(e->e.getParentId().equals(MallConst.ROOT_PARENT_ID))
                .map(this::category2CategoryVo).sorted(Comparator.comparing(CategoryVo::getSortOrder)).collect(Collectors.toList());
        findSubCategory(categoryVoList,categories);
        return ResponseVo.success(categoryVoList);


    }



    private void findSubCategory(List<CategoryVo> categoryVoList,List<Category> categories){
        for(CategoryVo categoryVo:categoryVoList){
            List<CategoryVo> subCategoryVoList=new ArrayList<>();
            for(Category category:categories){
                if(category.getId().equals(categoryVo.getParentId())){
                    CategoryVo subCategoryVo=category2CategoryVo(category);
                    subCategoryVoList.add(subCategoryVo);
                }
            }
            subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
            categoryVo.setSubCategories(subCategoryVoList);
            findSubCategory(subCategoryVoList,categories);
        }
    }

    private CategoryVo category2CategoryVo(Category category){
        CategoryVo categoryVo=new CategoryVo();
        BeanUtils.copyProperties(category,categoryVo);
        return categoryVo;
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories=categoryMapper.selectAll();
        findSubCategoryId(id,resultSet,categories);
    }

    public void findSubCategoryId(Integer id, Set<Integer> resultSet,List<Category> categories){
        for(Category category:categories){
            if(category.getParentId().equals(id)){
                resultSet.add(category.getId());
                findSubCategoryId(category.getId(),resultSet,categories);
            }
        }
    }
}
