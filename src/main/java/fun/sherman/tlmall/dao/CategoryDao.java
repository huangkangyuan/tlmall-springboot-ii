package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.Category;
import fun.sherman.tlmall.provider.CategoryProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sherman
 */
@Mapper
@Repository
public interface CategoryDao {

    @Insert("insert into tlmall_category (parent_id, name, status, sort_order, create_time, update_time) " +
            "values(#{parentId}, #{name}, #{status}, #{sortOrder}, now(), now())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(Category category);

    @UpdateProvider(type = CategoryProvider.class, method = "updateCategoryProvider")
    int updateSelective(Category category);

    @Select("select id, parent_id, name, status, sort_order, create_time, update_time from tlmall_category where parent_id = #{parentId}")
    List<Category> selectCategoryChildByParentId(Integer parentId);

    @Select("select id, parent_id, name, status, sort_order, create_time, update_time from tlmall_category where id = #{categoryId}")
    Category selectCategoryByPrimaryKey(Integer categoryId);
}
