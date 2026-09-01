package com.first.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.first.train.business.domain.ConfirmOrder;
import com.first.train.business.domain.ConfirmOrderExample;
import com.first.train.business.domain.DailyTrainCarriage;
import com.first.train.business.domain.DailyTrainSeat;
import com.first.train.business.enums.ConfirmOrderStatusEnum;
import com.first.train.business.enums.SeatColEnum;
import com.first.train.business.mapper.ConfirmOrderMapper;
import com.first.train.business.mapper.DailyTrainSeatMapper;
import com.first.train.business.req.ConfirmOrderDoReq;
import com.first.train.business.req.ConfirmOrderQueryReq;
import com.first.train.business.req.ConfirmOrderSaveReq;
import com.first.train.business.req.ConfirmOrderTicketReq;
import com.first.train.business.resp.ConfirmOrderQueryResp;
import com.first.train.common.resp.PageResp;
import com.first.train.common.util.SnowUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private  DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    DailyTrainTicketService dailyTrainTicketService;

    @Resource
    DailyTrainSeatService dailyTrainSeatService;

    @Resource
    DailyTrainStationService dailyTrainStationService;

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;


    public void save(ConfirmOrderSaveReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);

        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());

        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 12-12: 确认下单购票接口骨架
     */
    public void doConfirm(ConfirmOrderDoReq req) {
        Date now=DateTime.now();
        Date date = req.getDate();

        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets=req.getTickets();
        //先填入数据库
        ConfirmOrder confirmOrder=new ConfirmOrder();
        // 12-12 保存确认订单表，初始状态为 INIT
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
        confirmOrder.setCreateTime(now);
        confirmOrder.setUpdateTime(now);
        confirmOrder.setMemberId(req.getMemberId());
        confirmOrder.setDate(date);
        confirmOrder.setTrainCode(trainCode);
        confirmOrder.setStart(start);
        confirmOrder.setEnd(end);
        confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        confirmOrder.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(confirmOrder);

        //这个需要写到最前面
        List<DailyTrainSeat> seatSell=new ArrayList<>();
        //这个作为统一的map吧
        Map<String, Integer> needTypeMap = new HashMap<>();
        Map<String,Integer> dbSeatMap=new HashMap<>();
        int startIndex=dailyTrainStationService.find(req.getStart(), req.getTrainCode(), req.getDate());
        int endIndex=dailyTrainStationService.find(req.getEnd(), req.getTrainCode(), req.getDate());
        //还需要一个终点站的站序,但这里没有sell啊

		if(tickets.get(0).getSeat()!=null){
			//判断余票
            long seats = dailyTrainSeatService.count(req.getTrainCode(), tickets.get(0).getSeatTypeCode());
            if((int) seats<tickets.size()){
                throw new RuntimeException("余票不足，库存只有 " + seats + " 张");
            }

            //这个需要加一下map了,可以顶替前面的了
            needTypeMap.put(tickets.get(0).getSeatTypeCode(),tickets.size());

            //接下来就是和选座都一样了,就是去
			List<DailyTrainCarriage> dailyTrainCarriages=dailyTrainCarriageService
                .getCarraige(tickets.get(0).getSeatTypeCode(),req.getDate(),req.getTrainCode());

            List<SeatColEnum> enums=SeatColEnum.getColsByType(tickets.get(0).getSeatTypeCode());
            //感觉map也可以,用map会复杂度更高吗?就是需要一个自增的东西了,所以会不太好吗?
          /*HashMap<String,Integer> base=new HashMap<>();
                 int sum=0;*/
            List<String> base=new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                //还是不理解为什么得到的就是顺序了
                for(SeatColEnum seatColEnum:enums){
                    //怎么拼接来着--这样可以吗?
                    String s=seatColEnum.getCode()+i;
                    base.add(s);
                }
            }
            //去算偏移值了
            List<Integer> move=new ArrayList<>();
            move.add(base.indexOf(tickets.get(0).getSeat()));
            for (int i=1;i<tickets.size();i++){
                //seat字段是吧?
                move.add(base.indexOf(tickets.get(i).getSeat())-move.get(0));
            }
            for (DailyTrainCarriage carriage:dailyTrainCarriages){
                List<DailyTrainSeat> dailyTrainSeats=dailyTrainSeatService
                        .carriageSeat(carriage.getTrainCode(),carriage.getIndex(),carriage.getDate());
                int carriage_List=carriage.getColCount();
                List<DailyTrainSeat> ticketSeat=new ArrayList<>();
                for (int i = 0; i < carriage.getRowCount(); i++){
                    //这个是选座的,但是有不选座位的啊,要分开去写,无非就是少了偏移,但是有公共的部分啊--要封装吗?两个不同的方法
                    //所以偏移值什么的要写在这个判断里面啊,那看来可以封装啊,只封装偏移值的就可以了吧
                        ticketSeat.clear();
                        //所以这个要写在前面啊
                        int index=i*carriage_List+move.get(0);
                        ticketSeat.add(dailyTrainSeats.get(index));
                        for (int j = 1; j < move.size(); j++) {
                            //去循环获取座位信息啊,对吧,但是得有第一个才行,单这样回写到后面啊,似乎不太行啊,我需要拿到的是当前座位对应的二维坐标
                            //只有拿到具体的二维坐标,才可以确定具体的座位,才可以后续操作啊
                            //但是需要单独的处理第一个啊,不然一个循环里面不能统一处理啊
                            ticketSeat.add(dailyTrainSeats.get(index+move.get(j)));
                        }
                        seatSell=testSeat( startIndex, endIndex,ticketSeat);
                        //跳出循环了
                        if(seatSell!=null){
                            break;
                        }
                }
                //得到座位之后就可以去改数据库了--具体改什么呢?哪些表呢?
                //余票表;订单表;座位表--sell字段,直接返回得到修改完sell的seat了,所以直接改就行了
                //这个似乎是通用的
               /* if(seatSell!=null){

                }*/

            }
		}
        else {
            //没有选座的话,传的是什么?这个有点复杂了,因为选座的是同一个座位类型,也是用一个车厢,但是非选座的话
            //我就需要把这if提到更前面,不对,从筛车厢开始都是偏移,所以需要全部提取为一个方法吗?
            //但是有公共的啊,但是不行,公共的也有差距
            //不选座的话,需要去把所有对应的座位筛出来,太复杂了吧,数据量太大了吧
            //这个应该是只需要统计哪些座位有几张票就行了吧?我看前面的筛选也是只筛了有选座的,因为有选座的类型一样,也就说全部都要重写一遍
            //位置应该写在最前面,就这个判断
            //我需要先通过选票的座位类型,先统计不同座位类型的数量,然后再去判断余票够不够,然后再去通过座位筛选
            //这个不一样,这个需要把订单里面的所有座位类型都统计出来,然后才能去数据判断,好复杂啊.
            //也可以找数据库,然后在找数据库的过程中去增加--这样的逻辑会更好吗?似乎并不会还会复杂,因为这样没有办法只筛一次数据库了

            //那这样的话最好使用map啊
            // 现在 sumType 里存的是每种 seatTypeCode 出现的次数,ai果然干净利落
            //AI的逻辑分析确实可以啊,非常的利落干净---其实选座的也可以,反而更简单,因为类型一致
            // 1. 第一步：统计出“每种乘客类型各需要几张票”（顺便把 Key 收集了）

            for (ConfirmOrderTicketReq ticketReq : tickets) {
                // 这行代码就是你把 Key（乘客类型）收集起来，并统计数量
                needTypeMap.merge(ticketReq.getSeatTypeCode(), 1, Integer::sum);
            }



                 /*   // 假设你的 Mapper 支持传入 List 批量查询---看来不支持了
                    List<String> typeList = new ArrayList<>(needTypeMap.keySet()); // 这就是你要的 Key 集合！

                     看来mybatis没有帮助啊
                   List<Long> dbSeatList=new ArrayList<>();
                    for (int j = 0; j < typeList.size(); j++) {

                        dbSeatList.add(dailyTrainSeatService.count(req.getTrainCode(),typeList.get(j)));
                    }*/

            //这个方法确实不知道
            for (String type : needTypeMap.keySet()) {
                // 这里注意：你 new 的方法是 count(trainCode, seatType)，seatType 对应的是座位类型，不是乘客类型！
                // 假设你的 ticketReq 里存的是 seatType，直接调用
                long seats = dailyTrainSeatService.count(req.getTrainCode(), type);
                dbSeatMap.put(type, (int) seats); // count 返回 long，转成 int 存
            }

            for (Map.Entry<String, Integer> entry : needTypeMap.entrySet()) {
                String type = entry.getKey();        // 乘客类型（这就是你要的 Key）
                int needNum = entry.getValue();      // 需要几张
                int dbNum = dbSeatMap.getOrDefault(type, 0); // 数据库里有几张
                if (dbNum < needNum) {
                    throw new RuntimeException("乘客类型 [" + type + "] 余票不足，库存只有 " + dbNum + " 张");
                }
            }
            //然后重点就是去判断具体座位了,不要求车厢的话,只通过具体座位就可以了,要去筛具体座位
            //但是终点站和起始站,seat表里面没有,之前是在station里面去拿到的,而且封装方法了
            for (Map.Entry<String, Integer> entry : needTypeMap.entrySet()){
                //复用ai的吧
                String type = entry.getKey();        // 乘客类型（这就是你要的 Key）
                int needNum = entry.getValue();      // 需要几张
                //吐了,选座位那个还不能复用,上面是用的车厢,这次需要一个一个查了
                List<DailyTrainSeat> typeSeats=dailyTrainSeatService.typeSeat(req.getTrainCode(),req.getDate(),type);
                //看来逻辑有点混乱啊,这个怎么写到for循环车厢里面了,哈哈,应该要分开的;不是在循环车厢就是对应的座位类型
                List<DailyTrainSeat> isSell=testSeat2(req,typeSeats,needNum);
                if(isSell==null){
                    throw new RuntimeException("乘客类型 [" + type + "] 余票不足");
                }
                //还不行呢?这个是list的list,写一个for吗?
                seatSell.addAll(isSell);
            }

        }
        //得到座位之后就可以去改数据库了--具体改什么呢?哪些表呢?
        //余票表;订单表;座位表--sell字段,直接返回得到修改完sell的seat了,所以直接改就行了
        //这个似乎是通用的
        if(seatSell!=null){
            //余票表比较疯狂,第二个不选座的话,需要去对每一个更新,所以这个不能通用啊,还需要写一个for啊,不对,座位表里面有了
            //得到的里面有座位类型,直接去更就行了
            //可以先更新座位表,sell那个,fuck,没有对应的mabatis;不行就更新订单表先,也不行

            //这个还有必要吗?seatsell里面都是更新好了的座位了,还需要一个又一个的遍历座位吗?
            int finalIndex=seatSell.get(0).getSell().length();
            for (DailyTrainSeat dailyTrainSeat:seatSell){
                //去更新余票表,对应去减一,肯定也没有mabatis了,这个会复杂一些
            }
            //更新余票用的是写进去的map,我需要站序和终点站序啊,这个在前面啊
            for (Map.Entry<String, Integer> entry : needTypeMap.entrySet()){
                String type = entry.getKey();        // 乘客类型（这就是你要的 Key）
                int needNum = entry.getValue();      // 需要几张
                //然后去更新
                for (int i = 0; i <= startIndex; i++) {
                    for (int j = endIndex; j < finalIndex; j++) {
                        //去改余票信息,从map里面就能得到类型和数目,去数据库里面更新
                        //在ticket里面写一下方法吧

                    }

                }
            }

        }

        /*//先去判断余票够不够---起始和终止站都是一样的,就是在一个订单里面里面
        //先写这么多吧,因为我不知道对不对
        List<DailyTrainTicket> sumTicket=dailyTrainTicketService.sumSeat(req.getDate(),req.getTrainCode(),req.getStart(),req.getEnd());
        //然后就下面的获取一下吧,然后循环去判断
        int ydz=sumTicket.get(0).getYdz();
        for (ConfirmOrderTicketReq ticket:tickets) {
            //我还真不会用这个switch了呢?不对,需要先把对应的票数的类型统计出来啊,不对不对,这样太麻烦了,直接去查余票数据然后减一就行了
            String seatType = ticket.getSeatTypeCode();
            switch (seatType) {
                //seattype难道不是YDZ这种吗?为什么还要引入啊
                case YDZ :
                    if(ydz<=0){
                        //怎么抛异常来着?
                    }


            }
        }*/

        /*//筛车厢出来--要for循环啊,因为一个订单可能有多个选座信息;筛座位也要在这个里面吗?
        /// 看来之前还是理解不到位,应该是只先筛出来符合座位类型的车厢,具体的座位要在下面去筛选了
        List<DailyTrainCarriage> dailyTrainCarriages=dailyTrainCarriageService
                .getCarraige(tickets.get(0).getSeatTypeCode(),req.getDate(),req.getTrainCode());
        //怎么去数据库查来着,写过方法吗要,我没看到mapper里面有对应的方法啊,要写example吗?但是
        /// 得到对应车厢之后,就要开始去选座了吧,有的话,先算偏移值,毕竟都是在对应车厢去选择
        /// 但可能没有具体选座,只有座位类型.也是在车厢里面选择对应的座位
        /// 都还要筛座位啊,怎么具体去筛呢?--就是根据车厢的厢序

        //先开始算偏移值,有的没有选座啊,所以要单独写一个方法吗?req里面包含这个信息了吗?就是选座的信息--真没有概念啊,在ticket里面,seat信息
        //怎么算来着--先获取座位类型对应的座位排序
        List<SeatColEnum> enums=SeatColEnum.getColsByType(tickets.get(0).getSeatTypeCode());
        //感觉map也可以,用map会复杂度更高吗?就是需要一个自增的东西了,所以会不太好吗?
        *//*HashMap<String,Integer> base=new HashMap<>();
        int sum=0;*//*
        List<String> base=new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            //还是不理解为什么得到的就是顺序了
           for(SeatColEnum seatColEnum:enums){
               //怎么拼接来着--这样可以吗?
               String s=seatColEnum.getCode()+i;
               base.add(s);
           }
        }
        //去算偏移值了
        List<Integer> move=new ArrayList<>();
        move.add(base.indexOf(tickets.get(0).getSeat()));
        for (int i=1;i<tickets.size();i++){
            //seat字段是吧?
            move.add(base.indexOf(tickets.get(i).getSeat())-move.get(0));
        }
        //应该需要把上面的变成一个方法吧,不然空白的seat怎么搞,有点迷.先写座位选择吧,这个是公共的了
        //前面已经得到符合的车厢了,然后去车厢里面筛具体座位--这样数据库操作似乎太多了
//   有问题不是得数目的,应该是具体的list  long seatcount=dailyTrainSeatService.count(req.getTrainCode(),tickets.get(0).getSeatTypeCode());
        //应该算一个就行了吧,就是起始和终点,但是要循环每一个座位,带上偏移值,按照座位行来循环,数据库里面是排号
        //总感觉逻辑有点重合,不对,现在得到的车厢只是符合座位类型的,但是没有终点站这些
        //但是既然去筛座位seat了,查出来的车厢还有用吗?直接对座位,难道是因为强制在一个车厢里面?那还是要按照车厢去筛啊,在每一个车厢里面去筛啊

        //第一个循环就是去车厢,第二个循环就是就是按照行数也就是数据库里面的排数
        for (DailyTrainCarriage carriage:dailyTrainCarriages){
            //还是需要去得到具体的座位信息
            List<DailyTrainSeat> dailyTrainSeats=dailyTrainSeatService
                    .carriageSeat(carriage.getTrainCode(),carriage.getIndex(),carriage.getDate());
            int carriage_List=carriage.getColCount();
            for (int i = 0; i < carriage.getRowCount(); i++) {
                //根据偏移值去找对应的座位,怎么找呢?目前只有车厢信息啊
                //这个是为了防止订单里面只有部分座位可以购买,但总体没有能被完全购买的

                List<DailyTrainSeat> ticketSeat=new ArrayList<>();
                //这个是选座的,但是有不选座位的啊,要分开去写,无非就是少了偏移,但是有公共的部分啊--要封装吗?两个不同的方法
                //所以偏移值什么的要写在这个判断里面啊,那看来可以封装啊,只封装偏移值的就可以了吧
                if(tickets.get(0).getSeat()!=null)
                {    //所以这个要写在前面啊
                    int index=i*carriage_List+move.get(0);
                    ticketSeat.add(dailyTrainSeats.get(index));
                    for (int j = 1; j < move.size(); j++) {
                        //去循环获取座位信息啊,对吧,但是得有第一个才行,单这样回写到后面啊,似乎不太行啊,我需要拿到的是当前座位对应的二维坐标
                        //只有拿到具体的二维坐标,才可以确定具体的座位,才可以后续操作啊
                        //但是需要单独的处理第一个啊,不然一个循环里面不能统一处理啊
                        ticketSeat.add(dailyTrainSeats.get(index+move.get(j)));
                    }
                    seatSell=testSeat(req,ticketSeat);
                    //跳出循环了

                }

                else {
                    //没有选座的话,传的是什么?这个有点复杂了,因为选座的是同一个座位类型,也是用一个车厢,但是非选座的话
                    //我就需要把这if提到更前面,不对,从筛车厢开始都是偏移,所以需要全部提取为一个方法吗?
                    //但是有公共的啊,但是不行,公共的也有差距
                    //不选座的话,需要去把所有对应的座位筛出来,太复杂了吧,数据量太大了吧
                    //这个应该是只需要统计哪些座位有几张票就行了吧?我看前面的筛选也是只筛了有选座的,因为有选座的类型一样,也就说全部都要重写一遍
                    //位置应该写在最前面,就这个判断
                    //我需要先通过选票的座位类型,先统计不同座位类型的数量,然后再去判断余票够不够,然后再去通过座位筛选
                    //这个不一样,这个需要把订单里面的所有座位类型都统计出来,然后才能去数据判断,好复杂啊.
                    //也可以找数据库,然后在找数据库的过程中去增加--这样的逻辑会更好吗?似乎并不会还会复杂,因为这样没有办法只筛一次数据库了

	                //那这样的话最好使用map啊
                    // 现在 sumType 里存的是每种 seatTypeCode 出现的次数,ai果然干净利落
                    //AI的逻辑分析确实可以啊,非常的利落干净---其实选座的也可以,反而更简单,因为类型一致
                    // 1. 第一步：统计出“每种乘客类型各需要几张票”（顺便把 Key 收集了）
                    Map<String, Integer> needTypeMap = new HashMap<>();
                    for (ConfirmOrderTicketReq ticketReq : tickets) {
                        // 这行代码就是你把 Key（乘客类型）收集起来，并统计数量
                        needTypeMap.merge(ticketReq.getPassengerType(), 1, Integer::sum);
                    }



                 *//*   // 假设你的 Mapper 支持传入 List 批量查询---看来不支持了
                    List<String> typeList = new ArrayList<>(needTypeMap.keySet()); // 这就是你要的 Key 集合！

                     看来mybatis没有帮助啊
                   List<Long> dbSeatList=new ArrayList<>();
                    for (int j = 0; j < typeList.size(); j++) {

                        dbSeatList.add(dailyTrainSeatService.count(req.getTrainCode(),typeList.get(j)));
                    }*//*
                       Map<String,Integer> dbSeatMap=new HashMap<>();
                       //这个方法确实不知道
                    for (String type : needTypeMap.keySet()) {
                        // 这里注意：你 new 的方法是 count(trainCode, seatType)，seatType 对应的是座位类型，不是乘客类型！
                        // 假设你的 ticketReq 里存的是 seatType，直接调用
                        long seats = dailyTrainSeatService.count(req.getTrainCode(), type);
                        dbSeatMap.put(type, (int) seats); // count 返回 long，转成 int 存
                    }

                    for (Map.Entry<String, Integer> entry : needTypeMap.entrySet()) {
                        String type = entry.getKey();        // 乘客类型（这就是你要的 Key）
                        int needNum = entry.getValue();      // 需要几张
                        int dbNum = dbSeatMap.getOrDefault(type, 0); // 数据库里有几张
                        if (dbNum < needNum) {
                            throw new RuntimeException("乘客类型 [" + type + "] 余票不足，库存只有 " + dbNum + " 张");
                        }
                    }
                    //然后重点就是去判断具体座位了,不要求车厢的话,只通过具体座位就可以了,要去筛具体座位
                    //但是终点站和起始站,seat表里面没有,之前是在station里面去拿到的,而且封装方法了
                    for (Map.Entry<String, Integer> entry : needTypeMap.entrySet()){
                        //复用ai的吧
                        String type = entry.getKey();        // 乘客类型（这就是你要的 Key）
                        int needNum = entry.getValue();      // 需要几张
                        //吐了,选座位那个还不能复用,上面是用的车厢,这次需要一个一个查了
                        List<DailyTrainSeat> typeSeats=dailyTrainSeatService.typeSeat(req.getTrainCode(),req.getDate(),type);
                        //看来逻辑有点混乱啊,这个怎么写到for循环车厢里面了,哈哈,应该要分开的
                        List<DailyTrainSeat> isSell=testSeat2(req,typeSeats,needNum);
                        if(isSell==null){
                            throw new RuntimeException("乘客类型 [" + type + "] 余票不足");
                        }
                        //还不行呢?这个是list的list,写一个for吗?
                        seatSell.addAll(isSell);
                    }


                }
                    //这个不能写成公共的啊,不选座的需要都查一遍,然后才能确定,需要多次调testseat然后才能去判断,这个只是选座的可以这么去做,不是选座的,用不了这个
                    //需要重写一个方法去判断

                   *//*
                   if(seatSell!=null){
                    //就可以更新数据库了--这个要写成公共的吗?---应该是可以的


                }*//*

            }

        }






        *//*for (ConfirmOrderTicketReq ticket:tickets){
            //我还真不会用这个switch了呢?不对,需要先把对应的票数的类型统计出来啊,不对不对,这样太麻烦了,直接去查余票数据然后减一就行了
            String seatType=ticket.getSeatTypeCode();
            switch (seatType){


            }
            dailyTrainCarriages=dailyTrainCarriageService
                    .getCarraige(ticket.getSeatTypeCode(),req.getDate(), req.getTrainCode());
        }*//*
*/
    }


    //这个似乎可以通用啊,这个方法有问题啊,这样处理的只有对选座的才有用,草了,不选座的,需要去传数量
    public List<DailyTrainSeat> testSeat(int startIndex,int endIndex,List<DailyTrainSeat> ticketSeats){
        //写一个判断座位能不能买的方法吧,根据起始和终点站,这个在ticket里面,奇怪,订单里面居然没有,可以加入这两个字段吗?
        //怎么去找啊?req里面没有啊,怎么根据req里面的string来判断次序呢?用车站表去找吗?又嵌套一次吗?

        List<DailyTrainSeat> result = new ArrayList<>();
	    List<DailyTrainSeat> ticketSeat = ticketSeats;
//        List<String> seatSell=new ArrayList<>();
        for (DailyTrainSeat seat: ticketSeat) {

            String sell=seat.getSell();
            //Ai写的确实厉害,比我厉害多了
            String segment = sell.substring(startIndex, endIndex); // 截取该段
            // 检查该段是否全部为 '0'
            if (segment.contains("1")) {
                // 如果有任何一个座位不可售，直接返回 null（表示整体失败）
                return null;
                // 如果你想跳出循环但返回已成功的部分，可以用 break 并记录失败标志
            }

            // 可售：修改该段为 '1'
            char[] chars = sell.toCharArray();
            for (int i = startIndex; i < endIndex; i++) { // 注意 endIndex 不包含
                if (chars[i] == '0') chars[i] = '1';
            }
            String newSell = new String(chars);
            seat.setSell(newSell);
            result.add(seat);
            // 注意：这里并没有更新数据库，只是收集新字符串
        }
        return result;
            //开始截取
           /* String sellResult=sell.substring(startIndex,endIndex);
            if(Integer.getInteger(sellResult)==0){
                //可以售卖--这个要在这里库存减一了吧,不对,应该是订单全部完成后才会减去,这个就有点复杂了
                //问题就是说如果订单里面没有卖出去,就要去恢复,这个有简单的方法吗?
                //改变一下sell--这个方法是我问的AI,哈哈
                char[] chars = sell.toCharArray();
                for (int i = startIndex; i <= endIndex; i++) {
                    if (chars[i] == '0') chars[i] = '1';
                }
                sell = new String(chars);
                //但是还需要插入啊,就是更新啊,怎么更新啊,我
                seatSell.add(sell);
            }else {
                return null;
            }
        }

        return  seatSell;*/

    }

    //这个似乎可以通用啊,这个方法有问题啊,这样处理的只有对选座的才有用,草了,不选座的,需要去传数量
    public List<DailyTrainSeat>  testSeat2(ConfirmOrderDoReq req,List<DailyTrainSeat> ticketSeats,int needSum){
        //写一个判断座位能不能买的方法吧,根据起始和终点站,这个在ticket里面,奇怪,订单里面居然没有,可以加入这两个字段吗?
        //怎么去找啊?req里面没有啊,怎么根据req里面的string来判断次序呢?用车站表去找吗?又嵌套一次吗?
        int startIndex=dailyTrainStationService.find(req.getStart(), req.getTrainCode(), req.getDate());
        int endIndex=dailyTrainStationService.find(req.getEnd(), req.getTrainCode(), req.getDate());
        List<DailyTrainSeat> result = new ArrayList<>();
        List<DailyTrainSeat> ticketSeat = ticketSeats;
//        List<String> seatSell=new ArrayList<>();
        for (DailyTrainSeat seat: ticketSeat) {

            String sell=seat.getSell();
            //Ai写的确实厉害,比我厉害多了
            String segment = sell.substring(startIndex, endIndex); // 截取该段
            // 检查该段是否全部为 '0'
            //这个有个隐蔽的点,很重要,就是选座的时候已经是把可以选择的给出来了,但是非选座的是只要个数合适就行
            /*
            if (segment.contains("1")) {
                // 如果有任何一个座位不可售，直接返回 null（表示整体失败）
                return null;
                // 如果你想跳出循环但返回已成功的部分，可以用 break 并记录失败标志
            }
            */
            //这个要写成这样
            if (segment.contains("1")) {
                // 如果有任何一个座位不可售，直接返回 null（表示整体失败）
                continue;
                // 如果你想跳出循环但返回已成功的部分，可以用 break 并记录失败标志
            }

            // 可售：修改该段为 '1'
            char[] chars = sell.toCharArray();
            for (int i = startIndex; i < endIndex; i++) { // 注意 endIndex 不包含
                if (chars[i] == '0') chars[i] = '1';
            }
            String newSell = new String(chars);
            seat.setSell(newSell);
            result.add(seat);
            // 注意：这里并没有更新数据库，只是收集新字符串
            //对应的要增加这个,和continue对应
            if(result.size()==needSum){
                return result;
            }
        }
        return null;
        //开始截取
           /* String sellResult=sell.substring(startIndex,endIndex);
            if(Integer.getInteger(sellResult)==0){
                //可以售卖--这个要在这里库存减一了吧,不对,应该是订单全部完成后才会减去,这个就有点复杂了
                //问题就是说如果订单里面没有卖出去,就要去恢复,这个有简单的方法吗?
                //改变一下sell--这个方法是我问的AI,哈哈
                char[] chars = sell.toCharArray();
                for (int i = startIndex; i <= endIndex; i++) {
                    if (chars[i] == '0') chars[i] = '1';
                }
                sell = new String(chars);
                //但是还需要插入啊,就是更新啊,怎么更新啊,我
                seatSell.add(sell);
            }else {
                return null;
            }
        }

        return  seatSell;*/

    }



}
