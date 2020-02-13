package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * 用户模块动态sql
 *
 * @author sherman
 */
public class UserProvider extends SQL {
    private static final String TABLE_NAME = "tlmall_user";

    public String updateUserProvider(User user) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (!StringUtils.isEmpty(user.getUsername())) {
                    SET("username = #{username, javaType=String, jdbcType=VARCHAR}");
                }
                if (!StringUtils.isEmpty(user.getPassword())) {
                    SET("password = #{password}");
                }
                if (!StringUtils.isEmpty(user.getEmail())) {
                    SET("email = #{email}");
                }
                if (!StringUtils.isEmpty(user.getPhone())) {
                    SET("phone = #{phone}");
                }
                if (!StringUtils.isEmpty(user.getAnswer())) {
                    SET("answer = #{answer}");
                }
                if (!StringUtils.isEmpty(user.getQuestion())) {
                    SET("question = #{question}");
                }
                if (user.getRole() != null) {
                    SET("role = #{role}");
                }
                if (user.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time = now()");
                WHERE("id = #{id}");
            }
        }.toString();
    }
}
