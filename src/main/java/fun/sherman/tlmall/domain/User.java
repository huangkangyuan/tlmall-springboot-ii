package fun.sherman.tlmall.domain;


import lombok.*;

/**
 * 用户
 */
@Data
public class User {
    private Integer id;
    /**
     * username有建立唯一索引
     */
    private String username;
    /**
     * 用户密码，MD5加密
     */
    private String password;
    private String email;
    private String phone;
    private String question;
    private String answer;
    /**
     * 角色：0-管理员，1-普通用户
     */
    private Integer role;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
