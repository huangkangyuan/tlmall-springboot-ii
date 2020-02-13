package fun.sherman.tlmall.service;

import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.Category;

import java.util.List;

/**
 * @author sherman
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse setCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> getAllChildCategory(Integer categoryId);
}
