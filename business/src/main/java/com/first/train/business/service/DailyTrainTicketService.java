package com.first.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.first.train.business.domain.*;
import com.first.train.business.enums.SeatTypeEnum;
import com.first.train.business.enums.TrainTypeEnum;
import com.first.train.business.mapper.DailyTrainTicketMapper;
import com.first.train.business.req.DailyTrainTicketQueryReq;
import com.first.train.business.req.DailyTrainTicketSaveReq;
import com.first.train.business.resp.DailyTrainTicketQueryResp;
import com.first.train.common.resp.PageResp;
import com.first.train.common.util.SnowUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private TrainCarriageService trainCarriageService;

    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        } else {
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }
    }

    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();

        if (ObjUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }
        if (ObjUtil.isNotEmpty(req.getStart())) {
            criteria.andStartEqualTo(req.getStart());
        }
        if (ObjUtil.isNotEmpty(req.getEnd())) {
            criteria.andEndEqualTo(req.getEnd());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());

        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);

        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);

        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void genDaily(DailyTrain dailyTrain, Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的余票信息开始", DateUtil.formatDate(date), trainCode);

        // 1. 幂等防线：删除某日某车次的余票信息
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);

        // 2. 查出车站列表
        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)) {
            LOG.info("该车次没有车站基础数据，生成该车次的余票信息结束");
            return;
        }

        // 3. 计算车次类型价格倍率（利用 EnumUtil 根据 code 提取 priceRate）
        String trainType = dailyTrain.getType();
        BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, trainType);

        // 4. 在外层一次性统计各席别初始座位数（12-4）
        int ydz = countSeatByCarriage(trainCode, SeatTypeEnum.YDZ.getCode());
        int edz = countSeatByCarriage(trainCode, SeatTypeEnum.EDZ.getCode());
        int rw  = countSeatByCarriage(trainCode, SeatTypeEnum.RW.getCode());
        int yw  = countSeatByCarriage(trainCode, SeatTypeEnum.YW.getCode());

        // 5. 双重 for 循环生成 276 个乘车区间
        for (int i = 0; i < stationList.size(); i++) {
            TrainStation stationStart = stationList.get(i);
            BigDecimal sumKm = BigDecimal.ZERO;

            for (int j = i + 1; j < stationList.size(); j++) {
                TrainStation stationEnd = stationList.get(j);
                sumKm = sumKm.add(stationEnd.getKm());

                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(stationStart.getName());
                dailyTrainTicket.setStartPinyin(stationStart.getNamePinyin());
                dailyTrainTicket.setStartTime(stationStart.getOutTime());
                dailyTrainTicket.setStartIndex(stationStart.getIndex());
                dailyTrainTicket.setEnd(stationEnd.getName());
                dailyTrainTicket.setEndPinyin(stationEnd.getNamePinyin());
                dailyTrainTicket.setEndTime(stationEnd.getInTime());
                dailyTrainTicket.setEndIndex(stationEnd.getIndex());

                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setYw(yw);

                // 票价计算：里程 * 席别基础单价 * 车次类型倍率，保留 2 位小数
                dailyTrainTicket.setYdzPrice(sumKm.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setEdzPrice(sumKm.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setRwPrice(sumKm.multiply(SeatTypeEnum.RW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                dailyTrainTicket.setYwPrice(sumKm.multiply(SeatTypeEnum.YW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));

                DateTime now = DateTime.now();
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);
            }
        }
        LOG.info("生成日期【{}】车次【{}】的余票信息结束", DateUtil.formatDate(date), trainCode);
    }

    private int countSeatByCarriage(String trainCode, String seatType) {
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        int count = 0;
        for (TrainCarriage carriage : carriageList) {
            if (seatType.equals(carriage.getSeatType())) {
                count += carriage.getSeatCount();
            }
        }
        return count == 0 ? -1 : count;
    }

    public List<DailyTrainTicket> sumSeat(Date date,String trainCode, String start,String end) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andStartEqualTo(start)
                .andEndEqualTo(end);
        return dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
    }


}
