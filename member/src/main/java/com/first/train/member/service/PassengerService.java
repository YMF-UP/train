package com.first.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.first.train.common.context.LoginMemberContext;
import com.first.train.common.resp.PageResp;
import com.first.train.common.util.SnowUtil;
import com.first.train.member.domain.Passenger;
import com.first.train.member.domain.PassengerExample;
import com.first.train.member.mapper.PassengerMapper;
import com.first.train.member.req.PassengerQueryReq;
import com.first.train.member.req.PassengerSaveReq;
import com.first.train.member.resp.PassengerQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    private static final Logger LOG = LoggerFactory.getLogger(PassengerService.class);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PassengerService.class);

    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq req)
    {
        DateTime now=DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        if(ObjectUtil.isNull(passenger.getId())){
            passenger.setMemberId(LoginMemberContext.getId());
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        }else{
            passenger.setUpdateTime(now);
            passengerMapper.updateByPrimaryKey(passenger);
        }

    }
    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req)
    {
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.setOrderByClause("id desc");
        PassengerExample.Criteria criteria=passengerExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())) {
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());

        PageHelper.startPage(req.getPage(),req.getSize());
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);

        PageInfo<Passenger> pageInfo=new PageInfo<>(passengerList);
       log.info("总行数：{}", pageInfo.getTotal());
       log.info("总页数：{}", pageInfo.getPages());

        List<PassengerQueryResp> list = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);

        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    public void delete(Long id) {
      passengerMapper.deleteByPrimaryKey(id);
    }
}
