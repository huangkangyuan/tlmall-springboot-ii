package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.provider.UserProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author sherman
 */

@Mapper
@Repository
public interface UserDao {

    @Delete("delete from tlmall_user where id = #{id}")
    int deleteByPrimaryKey(Integer id);

    //单个参数还加了@Param注解时，使用#{}取值时，需要使用#{user.xxx}，keyProperty也需要使用user.xxx
    @Insert("insert into tlmall_user(username, password, email, phone, question, answer, role, create_time, update_time)" +
            " values(#{user.username}, #{user.password}, #{user.email}, #{user.phone}, #{user.question}, #{user.answer}," +
            "#{user.role}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "user.id", keyColumn = "id")
    int insert(@Param("user") User user);

    /**
     * 一般不查询出password字段，或者查询出来再将其置为null
     */
    @Select("select id, username, email, password, phone, question, answer, role, create_time, update_time from tlmall_user where id = #{id}")
//    @Results({
//            @Result(id = true, column = "id", property = "id"),
//            @Result(column = "username", property = "username"),
//            @Result(column = "email", property = "email"),
//            @Result(column = "phone", property = "phone"),
//            @Result(column = "question", property = "question"),
//            @Result(column = "answer", property = "answer"),
//            @Result(column = "role", property = "role"),
//            @Result(column = "create_time", property = "createTime"),
//            @Result(column = "update_time", property = "updateTime"),
//    })
    User selectByPrimaryKey(Integer id);

    @Select("select count(1) from tlmall_user where username = #{username}")
    int checkUsername(String username);

    @Select("select count(1) from tlmall_user where email = #{email}")
    int checkEmail(String email);

    @Select("select count(1) from tlmall_user where phone = #{phone}")
    int checkPhone(String phone);

    @Select("select count(1) from tlmall_user where password = #{password} and id = #{userId} ")
    int checkPassword(@Param(value = "password") String password, @Param("userId") Integer userId);

    /**
     * 注意这里使用了select *
     * 因为之后的get_user_info.do接口获取当前登陆用户信息，就是该selectLogin()查询到的字段
     */
    @Select("select id, username, email, password, phone, question, answer, role, create_time, update_time from tlmall_user where username=#{username} and password=#{password}")
    @ResultType(User.class)
    User selectLogin(@Param("username") String username, @Param("password") String password);

    @Select("select question from tlmall_user where username=#{username}")
    String selectQuestionByUsername(String username);

    @Select("select count(1) from tlmall_user where username = #{username} and question = #{question} and answer = #{answer}")
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    @Update("update tlmall_user set password = #{passwordNew}, update_time = now() where username = #{username}")
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    @UpdateProvider(type = UserProvider.class, method = "updateUserProvider")
    int updateByPrimaryKeySelective(User record);

    @Select("select count(1) from tlmall_user where email = #{email} and id != #{userId}")
    int checkEmailByUserId(@Param(value = "email") String email, @Param(value = "userId") Integer userId);

    @Select("select count(1) from tlmall_user where phone = #{phone} and id != #{userId}")
    int checkPhoneByUserId(@Param("phone") String phone, @Param("userId") Integer id);

}
