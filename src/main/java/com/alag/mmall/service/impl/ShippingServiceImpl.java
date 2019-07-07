package com.alag.mmall.service.impl;

import com.alag.mmall.common.ServerResponse;
import com.alag.mmall.mapper.ShippingMapper;
import com.alag.mmall.model.Shipping;
import com.alag.mmall.service.ShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements ShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount <= 0) {
            return ServerResponse.createByErrorMessage("新增地址失败");
        }
        Map result = Maps.newHashMap();
        result.put("shippingId", shipping.getId());
        return ServerResponse.createBySuccess(result);
    }

    @Override
    public ServerResponse del(Integer userId, Integer id) {
        int rowCount = shippingMapper.deleteByUserIdAndId(userId, id);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("删除收货地址成功！");
        }
        return ServerResponse.createByErrorMessage("删除收货地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByPrimaryKeySelective(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改收货地址成功！");
        }
        return ServerResponse.createByErrorMessage("修改收货地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByIdAndUserId(userId,shippingId);
        if (shipping == null) {
            return ServerResponse.createBySuccessMessage("未找到该收货地址！");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
