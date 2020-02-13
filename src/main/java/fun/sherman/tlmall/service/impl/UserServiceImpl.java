package fun.sherman.tlmall.service.impl;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.UserDao;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.IUserService;
import fun.sherman.tlmall.util.MD5Util;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户模块Service实现
 *
 * @author sherman
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserDao userDao;

    @Override
    public ServerResponse<User> login(String username, String password) {
        // 检查登录用户名是否存在
        int resultCount = userDao.checkUsername(username);
        if (0 == resultCount) {
            return ServerResponse.buildErrorByCode(1, "用户名不存在");
        }
        // 对password进行一次MD5加密
        String md5Pass = MD5Util.encodeWithUtf8(password);
        User user = userDao.selectLogin(username, md5Pass);
        if (user == null) {
            return ServerResponse.buildErrorByCode(1, "密码错误");
        }
        // 将密码置空后返回，仅仅包含username字段，其它字段都为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.buildSuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> response = checkValid(user.getUsername(), Const.USERNAME);
        if (!response.isSuccess()) {
            return response;
        }
        response = checkValid(user.getPhone(), Const.PHONE);
        if (!response.isSuccess()) {
            return response;
        }
        response = checkValid(user.getEmail(), Const.EMAIL);
        if (!response.isSuccess()) {
            return response;
        }
        // 校验成功，允许注册用户
        // MD5加密密码
        user.setPassword(MD5Util.encodeWithUtf8(user.getPassword()));
        // 设置用户权限
        user.setRole(Const.Role.ROLE_CUSTOMER);
        int resultCount = userDao.insert(user);
        if (resultCount == 0) {
            return ServerResponse.buildErrorByMsg("注册失败");
        }
        return ServerResponse.buildSuccessByMsg("注册成功");
    }

    /**
     * 校验户名、密码、email是否已经存在
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(str) && StringUtils.isNotBlank(type)) {
            int resultCount;
            switch (type) {
                case Const.USERNAME:
                    resultCount = userDao.checkUsername(str);
                    if (0 < resultCount) {
                        return ServerResponse.buildErrorByMsg("用户名已经存在");
                    }
                    break;
                case Const.PHONE:
                    resultCount = userDao.checkPhone(str);
                    if (0 < resultCount) {
                        return ServerResponse.buildErrorByMsg("手机号码已经存在");
                    }
                    break;
                case Const.EMAIL:
                    resultCount = userDao.checkEmail(str);
                    if (0 < resultCount) {
                        return ServerResponse.buildErrorByMsg("邮箱已经存在");
                    }
                    break;
            }
            return ServerResponse.buildSuccessByMsg("校验成功");
        } else {
            return ServerResponse.buildErrorByMsg("参数错误");
        }
    }

    @Override
    public ServerResponse<String> selectQuestionByUsername(String username) {
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.buildErrorByMsg("用户名不存在");
        }
        String question = userDao.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.buildSuccessByData(question);
        }
        return ServerResponse.buildSuccessByData("问题为空");
    }

    /**
     * 校验答案是否正确，如果答案正确，会构建一个名称为token_{username}，value为{forgetToken}的key，
     * 放入到本地缓存中，过期时间为12h
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userDao.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            ShardedRedisUtil.setEx(Const.TOKEN_PREFIX + username, forgetToken, Const.RedisKeyExpires.USER_RESET_PASSWORD_TOKEN);
            return ServerResponse.buildSuccessByMsg(forgetToken);
        }
        return ServerResponse.buildErrorByMsg("答案不正确");
    }

    /**
     * 忘记密码-重置密码
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.buildErrorByMsg("参数错误，token参数不存在");
        }
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.buildErrorByMsg("用户名不存在");
        }
        String token = ShardedRedisUtil.get(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.buildErrorByMsg("token无效或者过期");
        }
        if (StringUtils.equals(forgetToken, token)) {
            String newPasswordMd5 = MD5Util.encodeWithUtf8(newPassword);
            int resultCount = userDao.updatePasswordByUsername(username, newPasswordMd5);
            if (resultCount > 0) {
                return ServerResponse.buildSuccessByMsg("修改密码成功");
            }
        } else {
            return ServerResponse.buildErrorByMsg("token错误，请重新获取token");
        }
        return ServerResponse.buildErrorByMsg("修改密码失败");
    }

    /**
     * 登录状态修改密码
     * 注意防止横向越权，要校验一下这个用户的旧密码，一定要指定是这个用户
     */
    @Override
    public ServerResponse<String> resetPasswordWhenLogin(String oldPassword, String newPassword, User user) {
        int resultCount = userDao.checkPassword(MD5Util.encodeWithUtf8(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServerResponse.buildErrorByMsg("旧密码错误");
        }
        user.setPassword(MD5Util.encodeWithUtf8(newPassword));
        int updateCount = userDao.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.buildSuccessByMsg("更新密码成功");
        }
        return ServerResponse.buildErrorByMsg("更新密码失败");
    }

    /**
     * 登录修改用户密码
     */
    @Override
    public ServerResponse<User> updateInformation(User user) {
        // username不能被更新, email和phone需要校验，新的email、phone不能已经在系统中存在（注意不是属于当前用户）
        int resultCount = userDao.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.buildErrorByMsg("当前email已经存在");
        }
        resultCount = userDao.checkPhoneByUserId(user.getPhone(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.buildErrorByMsg("当前phone已经存在");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        // password暂时不考虑
        updateUser.setPassword(user.getPassword());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        resultCount = userDao.updateByPrimaryKeySelective(updateUser);
        if (resultCount > 0) {
            return ServerResponse.buildSuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.buildErrorByMsg("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer id) {
        User user = userDao.selectByPrimaryKey(id);
        if (user == null) {
            return ServerResponse.buildSuccessByMsg("找不到当前用户");
        }
        // 将密码置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.buildSuccessByData(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.buildSuccess();
        }
        return ServerResponse.buildError();
    }
}