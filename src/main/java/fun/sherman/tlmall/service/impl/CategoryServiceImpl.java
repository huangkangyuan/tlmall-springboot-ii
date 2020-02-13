package fun.sherman.tlmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.CategoryDao;
import fun.sherman.tlmall.domain.Category;
import fun.sherman.tlmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author sherman
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryDao categoryDao;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.buildErrorByMsg("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        // 该分类可用
        category.setStatus(1);
        int resultCount = categoryDao.insert(category);
        if (resultCount > 0) {
            return ServerResponse.buildSuccessByMsg("添加分类成功");
        }
        return ServerResponse.buildErrorByMsg("添加分类失败");
    }

    @Override
    public ServerResponse setCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.buildErrorByMsg("更新分类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int resultCount = categoryDao.updateSelective(category);
        if (resultCount > 0) {
            return ServerResponse.buildSuccessByMsg("更新分类成功");
        }
        return ServerResponse.buildErrorByMsg("更新分类失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId) {
        List<Category> categories = categoryDao.selectCategoryChildByParentId(categoryId);
        if (CollectionUtils.isEmpty(categories)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.buildSuccessByData(categories);
    }

    @Override
    public ServerResponse<List<Integer>> getAllChildCategory(Integer categoryId) {
        Set<Category> categories = Sets.newHashSet();
        getAllChildCategoryInternal(categories, categoryId);
        if (CollectionUtils.isEmpty(categories)) {
            logger.info("未找到当前分类的子分类");
        }
        List<Integer> categoriesId = Lists.newArrayList();
        for (Category category : categories) {
            categoriesId.add(category.getId());
        }
        return ServerResponse.buildSuccessByData(categoriesId);
    }

    private void getAllChildCategoryInternal(Set<Category> categories, Integer categoryId) {
        Category category = categoryDao.selectCategoryByPrimaryKey(categoryId);
        // 如果category是root节点，即categoryId==0，selectCategoryByPrimaryKey(0)为null，需要判断
        if (category != null) {
            categories.add(category);
        }
        List<Category> categoryList = categoryDao.selectCategoryChildByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            getAllChildCategoryInternal(categories, categoryItem.getId());
        }
    }
}
