package fun.sherman.tlmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import fun.sherman.tlmall.common.ServerResponse;
import fun.sherman.tlmall.dao.ShippingDao;
import fun.sherman.tlmall.domain.Shipping;
import fun.sherman.tlmall.service.IShippingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author sherman
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingDao shippingDao;

    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        if (StringUtils.isBlank(shipping.getReceiverName()) || StringUtils.isBlank(shipping.getReceiverPhone())) {
            return ServerResponse.buildErrorByMsg("新建地址失败");
        }
        shipping.setUserId(userId);
        int rowCount = shippingDao.insert(shipping);
        if (rowCount > 0) {
            Map<String, Integer> map = Maps.newHashMap();
            map.put("shippingId", rowCount);
            return ServerResponse.buildSuccess("新建地址成功", map);
        }
        return ServerResponse.buildErrorByMsg("新建地址失败");
    }

    @Override
    public ServerResponse deleteByUserIdAndShippingId(Integer userId, Integer shippingId) {
        int rowCount = shippingDao.deleteByUserIdAndShippingId(userId, shippingId);
        if (rowCount > 0) {
            return ServerResponse.buildSuccessByMsg("删除地址成功");
        }
        return ServerResponse.buildErrorByMsg("删除地址失败");
    }

    @Override
    public ServerResponse updateShippingSelective(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingDao.updateShippingSelective(shipping);
        if (rowCount > 0) {
            return ServerResponse.buildSuccessByMsg("更新地址成功");
        }
        return ServerResponse.buildErrorByMsg("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> selectByShippingIdAndUserId(Integer userId, Integer shippingId) {
        Shipping result = shippingDao.selectByShippingIdAndUserId(userId, shippingId);
        if (result != null) {
            return ServerResponse.buildSuccess("查询地址成功", result);
        }
        return ServerResponse.buildErrorByMsg("查询地址失败");
    }

    @Override
    public ServerResponse listAllShippingInfo(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingDao.listAllShippingInfoByUserId(userId);
        PageInfo<Shipping> pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.buildSuccessByData(pageInfo);
    }
}
