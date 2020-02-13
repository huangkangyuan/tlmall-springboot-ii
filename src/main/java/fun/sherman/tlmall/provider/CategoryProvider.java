package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.Category;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Product分类模块动态sql
 *
 * @author sherman
 */
public class CategoryProvider extends SQL {

    private static final String TABLE_NAME = "tlmall_category";

    public String updateCategoryProvider(Category category) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (category.getParentId() != null) {
                    SET("parent_id = #{parentId}");
                }
                if (!StringUtils.isEmpty(category.getName())) {
                    SET("name = #{name}");
                }
                if (category.getStatus() != null) {
                    SET("status = #{status}");
                }
                if (category.getSortOrder() != null) {
                    SET("sort_order = #{sortOrder}");
                }
                if (category.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time = #{updateTime}");
                WHERE("id = #{id}");
            }
        }.toString();
    }
}
