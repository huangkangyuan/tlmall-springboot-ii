package fun.sherman.tlmall.controller.backend;

import com.github.pagehelper.PageInfo;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.service.IOrderService;
import fun.sherman.tlmall.service.IUserService;
import fun.sherman.tlmall.vo.OrderVo;
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
@RequestMapping("/manage/order/")
@ResponseBody
public class OrderManageController {
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    public ServerResponse<PageInfo> manageOrderList(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return iOrderService.manageOrderList(pageNum, pageSize);
    }

    @RequestMapping(value = "details.do", method = RequestMethod.POST)
    public ServerResponse<OrderVo> manageOrderDetails(@RequestParam("order_no") Long orderNo) {
        return iOrderService.manageOrderDetails(orderNo);
    }

    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    public ServerResponse<PageInfo> manageOrderSearch(@RequestParam("order_no") Long orderNo,
                                                      @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return iOrderService.mangeOrderSearch(orderNo, pageNum, pageSize);
    }

    @RequestMapping(value = "send_goods.do", method = RequestMethod.POST)
    public ServerResponse manageSendGoods(@RequestParam("order_no") Long orderNo) {
        return iOrderService.manageSendGoods(orderNo);
    }
}
