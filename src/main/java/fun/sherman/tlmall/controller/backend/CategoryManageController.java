package fun.sherman.tlmall.controller.backend;

import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.service.ICategoryService;
import fun.sherman.tlmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author sherman
 */
@Controller
@RequestMapping("/manage/category")
@ResponseBody
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分类
     *
     * @param categoryName 新分类名称
     * @param parentId     parent id
     * @return 添加是否成功信息
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.GET)
    public ServerResponse addCategory(@RequestParam("category_name") String categoryName,
                                      @RequestParam(value = "parent_id", defaultValue = "0") Integer parentId) {
        return iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 修改分类名称
     *
     * @param categoryId   category id
     * @param categoryName 修改分类后的名称
     * @return 修改分类是否成功信息
     */
    @RequestMapping(value = "set_category_name.do", method = RequestMethod.GET)
    public ServerResponse setCategoryName(@RequestParam("category_id") Integer categoryId,
                                          @RequestParam("category_name") String categoryName) {
        return iCategoryService.setCategoryName(categoryId, categoryName);
    }

    /**
     * 查询子节点的category信息，不递归，保持平级
     *
     * @param categoryId 待查询子节点的category id
     * @return 查询结果信息
     */
    @RequestMapping(value = "get_child_category.do", method = RequestMethod.GET)
    ServerResponse getChildParallelCategory(@RequestParam(value = "category_id", defaultValue = "0") Integer categoryId) {
        return iCategoryService.getChildParallelCategory(categoryId);
    }

    /**
     * 递归查询子节点的category信息
     *
     * @param categoryId 待查询节点的id
     * @return 所有子节点的id信息
     */
    @RequestMapping(value = "get_all_category.do", method = RequestMethod.GET)
    ServerResponse getAllChildCategory(@RequestParam(value = "category_id", defaultValue = "0") Integer categoryId) {
        // 递归查询所有子分类
        return iCategoryService.getAllChildCategory(categoryId);
    }
}