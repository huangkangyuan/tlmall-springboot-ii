package fun.sherman.tlmall.controller.portal;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.IUserService;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 门户User Controller层
 *
 * @author sherman
 */
@Controller
@RequestMapping(value = "/user")
@ResponseBody
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 登录功能
     */
    @RequestMapping(value = "login.do", method = RequestMethod.GET)
    public ServerResponse<User> login(String username, String password, HttpServletResponse response, HttpSession session) {
        ServerResponse<User> result = iUserService.login(username, password);
        if (result.isSuccess()) {
            CookieUtil.writeLoginToken(response, session.getId());
            ShardedRedisUtil.setEx(session.getId(), JacksonUtil.beanToString(result.getData()), Const.RedisKeyExpires.USER_LOGIN_TOKEN);
        }
        return result;
    }

    /**
     * 用户登出
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    public ServerResponse<User> logout(HttpServletRequest request, HttpServletResponse response) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        CookieUtil.delLoginToken(response, request);
        ShardedRedisUtil.del(userLoginToken);
        return ServerResponse.buildSuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    public ServerResponse<String> register(@RequestBody User user) {
        return iUserService.register(user);
    }

    /**
     * 根据type进行校验str，用于输入框边输入边校验
     *
     * @param str  待校验字符
     * @param type 校验类型：username、email、phone
     * @return 返回是否校验成功
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取用户信息
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    public ServerResponse<User> getUserInfo(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user != null) {
            return ServerResponse.buildSuccessByData(user);
        }
        return ServerResponse.buildErrorByMsg("用户登录token已过期，请重新登录");
    }

    /**
     * 忘记密码-通过用户名寻找问题
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetGetQuestionByUsername(@RequestParam("username") String username) {
        return iUserService.selectQuestionByUsername(username);
    }

    /**
     * 忘记密码-校验问题答案是否正确
     */
    @RequestMapping(value = "forget_check_question.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 忘记密码-重置密码
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        return iUserService.forgetResetPassword(username, newPassword, forgetToken);
    }

    /**
     * 登录状态-修改密码
     */
    @RequestMapping(value = "reset_password_when_login.do", method = RequestMethod.POST)
    public ServerResponse<String> resetPasswordWhenLogin(HttpServletRequest request, String oldPassword, String newPassword) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        return iUserService.resetPasswordWhenLogin(oldPassword, newPassword, user);
    }

    /**
     * 登录状态修改用户信息
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    public ServerResponse<User> updateInformation(HttpServletRequest request, @RequestBody User user) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User currentUser = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (currentUser == null) {
            return ServerResponse.buildErrorByMsg("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            ShardedRedisUtil.setEx(userLoginToken, JacksonUtil.beanToString(response.getData()), Const.RedisKeyExpires.USER_LOGIN_TOKEN);
        }
        return response;
    }

    /**
     * 获取用户的所有信息
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    public ServerResponse<User> getInformation(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByMsg("用户未登录");
        }
        return iUserService.getInformation(user.getId());
    }
}
