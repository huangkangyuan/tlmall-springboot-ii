package fun.sherman.tlmall.controller.portal;

import fun.sherman.tlmall.common.Const;
import fun.sherman.tlmall.common.ResponseCode;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.domain.User;
import fun.sherman.tlmall.service.ICartService;
import fun.sherman.tlmall.util.CookieUtil;
import fun.sherman.tlmall.util.JacksonUtil;
import fun.sherman.tlmall.util.ShardedRedisUtil;
import fun.sherman.tlmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author sherman
 */
@Controller
@RequestMapping("/cart")
@ResponseBody
public class CartController {
    @Autowired
    private ICartService iCartService;

    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> add(HttpServletRequest request,
                                      @RequestParam("product_id") Integer productId,
                                      @RequestParam("count") Integer count) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(), productId, count);
    }

    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> update(HttpServletRequest request,
                                         @RequestParam("product_id") Integer productId,
                                         @RequestParam("count") Integer count) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(), productId, count);
    }

    /**
     * @param productIds ","分割的待删除产品id
     */
    @RequestMapping(value = "delete.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> delete(HttpServletRequest request, @RequestParam("product_ids") String productIds) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.delete(user.getId(), productIds);
    }

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> list(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    @RequestMapping(value = "select_all.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> selectAll(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.CHECKED);
    }

    @RequestMapping(value = "select.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> select(HttpServletRequest request, @RequestParam("product_id") Integer productId) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.CHECKED);
    }

    @RequestMapping(value = "un_select_all.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> unSelectAll(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), null, Const.Cart.UNCHECKED);
    }

    @RequestMapping(value = "un_select.do", method = RequestMethod.POST)
    public ServerResponse<CartVo> selectAll(HttpServletRequest request, @RequestParam("product_id") Integer productId) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(), productId, Const.Cart.UNCHECKED);
    }

    @RequestMapping(value = "get_cart_product_count.do", method = RequestMethod.POST)
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest request) {
        String userLoginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(userLoginToken)) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String userJsonStr = ShardedRedisUtil.get(userLoginToken);
        User user = JacksonUtil.stringToBean(userJsonStr, User.class);
        if (user == null) {
            return ServerResponse.buildErrorByCode(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
