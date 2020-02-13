package fun.sherman.tlmall.controller.backend;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.IUserService;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author sherman
 */
@Controller
@RequestMapping("/manage/user")
@ResponseBody
public class UserManageController {
    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.GET)
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse response) {
        ServerResponse<User> result = iUserService.login(username, password);
        if (result.isSuccess()) {
            User user = result.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                CookieUtil.writeLoginToken(response, session.getId());
                ShardedRedisUtil.setEx(session.getId(), JacksonUtil.beanToString(user), Const.RedisKeyExpires.USER_LOGIN_TOKEN);
            } else {
                return ServerResponse.buildErrorByMsg("非管理员，无法登陆");
            }
        }
        return result;
    }
}
