<template>
  <div class="order-train">
    <span class="order-train-main">{{dailyTrainTicket.date}}</span>&nbsp;
    <span class="order-train-main">{{dailyTrainTicket.trainCode}}</span>次&nbsp;
    <span class="order-train-main">{{dailyTrainTicket.start}}</span>站
    <span class="order-train-main">({{dailyTrainTicket.startTime}})</span>
    <span class="order-train-main">——</span>
    <span class="order-train-main">{{dailyTrainTicket.end}}</span>站
    <span class="order-train-main">({{dailyTrainTicket.endTime}})</span>&nbsp;

    <div class="order-train-ticket">
      <span v-if="dailyTrainTicket.ydz >= 0">
        一等座[￥{{dailyTrainTicket.ydzPrice}}]余票: {{dailyTrainTicket.ydz}}&nbsp;&nbsp;
      </span>
      <span v-if="dailyTrainTicket.edz >= 0">
        二等座[￥{{dailyTrainTicket.edzPrice}}]余票: {{dailyTrainTicket.edz}}&nbsp;&nbsp;
      </span>
      <span v-if="dailyTrainTicket.rw >= 0">
        软卧[￥{{dailyTrainTicket.rwPrice}}]余票: {{dailyTrainTicket.rw}}&nbsp;&nbsp;
      </span>
      <span v-if="dailyTrainTicket.yw >= 0">
        硬卧[￥{{dailyTrainTicket.ywPrice}}]余票: {{dailyTrainTicket.yw}}&nbsp;&nbsp;
      </span>
    </div>
  </div>

  <a-divider></a-divider>
  <b>勾选乘车人：</b>
  <a-checkbox-group v-model:value="passengerChecks" :options="passengerOptions" />

  <div class="order-tickets">
    <a-table :dataSource="tickets" :columns="columns" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'passengerType'">
          <span v-for="item in PASSENGER_TYPE_ARRAY" :key="item.code">
            <span v-if="item.code === record.passengerType">
              {{item.desc}}
            </span>
          </span>
        </template>
        <template v-else-if="column.dataIndex === 'seatTypeCode'">
          <a-select v-model:value="record.seatTypeCode" style="width: 120px">
            <a-select-option v-for="item in SEAT_TYPE_ARRAY" :key="item.code" :value="item.code">
              {{item.desc}}
            </a-select-option>
          </a-select>
        </template>
      </template>
    </a-table>
  </div>

  <div v-if="tickets.length > 0" class="order-seat">
    <div v-if="chooseSeatType === '0'">
      <a-alert message="选座提示" description="请选择同一种席别进行选座；若不选座，系统将为您随机分配座位。" type="info" show-icon />
    </div>
    <div v-else>
      <b>在线选座：</b>
      <div class="order-seat-row">
        <span style="font-weight: bold">窗</span>&nbsp;
        <span v-for="item in SEAT_COL_ARRAY" :key="item.code">
          <a-button :type="chooseSeatObj[item.code + '1'] ? 'primary' : 'default'"
                    @click="clickSeat(item.code + '1')">
            {{item.code}}1
          </a-button>&nbsp;
        </span>
        <span style="font-weight: bold">&nbsp;过道&nbsp;</span>
      </div>
      <div class="order-seat-row">
        <span style="font-weight: bold">窗</span>&nbsp;
        <span v-for="item in SEAT_COL_ARRAY" :key="item.code">
          <a-button :type="chooseSeatObj[item.code + '2'] ? 'primary' : 'default'"
                    @click="clickSeat(item.code + '2')">
            {{item.code}}2
          </a-button>&nbsp;
        </span>
        <span style="font-weight: bold">&nbsp;过道&nbsp;</span>
      </div>
      <br/>
      <div v-if="chooseSeatCount > 0">
        已选择座位：{{chooseSeatNames}}
      </div>
    </div>
  </div>

  <div style="margin-top: 20px; text-align: center;">
    <a-button type="primary" size="large" @click="handleOk" :loading="submitLoading">
      提交订单
    </a-button>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, watch, computed } from 'vue';
import {notification} from "ant-design-vue";
import axios from "axios";
import router from "@/router";

export default defineComponent({
  name: "order-view",
  setup() {
    const dailyTrainTicket = SessionStorage.get(SESSION_ORDER) || {};
    const passengers = ref([]);
    const passengerOptions = ref([]);
    const passengerChecks = ref([]);
    const tickets = ref([]);
    const submitLoading = ref(false);

    const PASSENGER_TYPE_ARRAY = window.PASSENGER_TYPE_ARRAY || [
      {code: "1", desc: "成人"},
      {code: "2", desc: "儿童"},
      {code: "3", desc: "学生"}
    ];

    const SEAT_TYPE_ARRAY = window.SEAT_TYPE_ARRAY || [
      {code: "1", desc: "一等座", price: dailyTrainTicket.ydzPrice},
      {code: "2", desc: "二等座", price: dailyTrainTicket.edzPrice},
      {code: "3", desc: "软卧", price: dailyTrainTicket.rwPrice},
      {code: "4", desc: "硬卧", price: dailyTrainTicket.ywPrice}
    ];

    const columns = [
      {
        title: '乘客姓名',
        dataIndex: 'passengerName',
        key: 'passengerName',
      },
      {
        title: '身份证',
        dataIndex: 'passengerIdCard',
        key: 'passengerIdCard',
      },
      {
        title: '票种',
        dataIndex: 'passengerType',
        key: 'passengerType',
      },
      {
        title: '席别',
        dataIndex: 'seatTypeCode',
        key: 'seatTypeCode',
      }
    ];

    // 查询当前会员的所有乘车人
    const queryPassenger = () => {
      axios.get("/member/passenger/query-mine").then((response) => {
        let data = response.data;
        if (data.success) {
          passengers.value = data.content;
          passengerOptions.value = [];
          passengers.value.forEach((item) => {
            passengerOptions.value.push({
              label: item.name,
              value: item.name
            });
          });
        } else {
          notification.error({description: data.message});
        }
      });
    };

    // 勾选乘车人变化时更新 tickets 列表
    watch(() => passengerChecks.value, (newVal) => {
      tickets.value = [];
      newVal.forEach((item) => {
        let p = passengers.value.find((passenger) => passenger.name === item);
        if (p) {
          // 智能默认席别：优先选择有一等座或二等座
          let defaultSeatType = "2";
          if (dailyTrainTicket.ydz >= 0) {
            defaultSeatType = "1";
          }
          if (dailyTrainTicket.edz >= 0) {
            defaultSeatType = "2";
          }
          tickets.value.push({
            passengerId: p.id,
            passengerType: p.type,
            passengerName: p.name,
            passengerIdCard: p.idCard,
            seatTypeCode: defaultSeatType,
            seat: ""
          });
        }
      });
    });

    // 选座逻辑 (12-10)
    const chooseSeatObj = ref({});
    const SEAT_COL_ARRAY = computed(() => {
      let type = chooseSeatType.value;
      if (type === "1") {
        return [{code: "A"}, {code: "C"}, {code: "D"}, {code: "F"}];
      } else if (type === "2") {
        return [{code: "A"}, {code: "B"}, {code: "C"}, {code: "D"}, {code: "F"}];
      } else {
        return [];
      }
    });

    // 判断当前勾选的席别是否一致
    const chooseSeatType = computed(() => {
      if (tickets.value.length === 0 || tickets.value.length > 5) {
        return "0";
      }
      let firstType = tickets.value[0].seatTypeCode;
      let allSame = tickets.value.every((item) => item.seatTypeCode === firstType);
      if (allSame && (firstType === "1" || firstType === "2")) {
        return firstType;
      }
      return "0";
    });

    // 点击选座切换
    const clickSeat = (seat) => {
      if (chooseSeatObj.value[seat]) {
        delete chooseSeatObj.value[seat];
      } else {
        if (Object.keys(chooseSeatObj.value).length >= tickets.value.length) {
          notification.warning({description: "选座数量不能超过乘车人数量！"});
          return;
        }
        chooseSeatObj.value[seat] = true;
      }
    };

    const chooseSeatCount = computed(() => {
      return Object.keys(chooseSeatObj.value).length;
    });

    const chooseSeatNames = computed(() => {
      return Object.keys(chooseSeatObj.value).join(", ");
    });

    // 提交订单 (12-12)
    const handleOk = () => {
      if (tickets.value.length === 0) {
        notification.warning({description: "请至少勾选一位乘车人！"});
        return;
      }

      let selectedSeats = Object.keys(chooseSeatObj.value);
      if (selectedSeats.length > 0 && selectedSeats.length !== tickets.value.length) {
        notification.warning({description: "请选择与乘车人数一致的座位数量，或者不选座由系统分配！"});
        return;
      }

      // 给 tickets 赋值选座结果
      for (let i = 0; i < tickets.value.length; i++) {
        if (selectedSeats.length > 0) {
          tickets.value[i].seat = selectedSeats[i];
        } else {
          tickets.value[i].seat = "";
        }
      }

      submitLoading.value = true;
      axios.post("/business/confirm-order/do", {
        dailyTrainTicketId: dailyTrainTicket.id,
        date: dailyTrainTicket.date,
        trainCode: dailyTrainTicket.trainCode,
        start: dailyTrainTicket.start,
        end: dailyTrainTicket.end,
        tickets: tickets.value
      }).then((response) => {
        submitLoading.value = false;
        let data = response.data;
        if (data.success) {
          notification.success({description: "下单成功！"});
          router.push("/ticket");
        } else {
          notification.error({description: data.message});
        }
      });
    };

    onMounted(() => {
      queryPassenger();
    });

    return {
      dailyTrainTicket,
      passengers,
      passengerOptions,
      passengerChecks,
      tickets,
      columns,
      PASSENGER_TYPE_ARRAY,
      SEAT_TYPE_ARRAY,
      chooseSeatObj,
      SEAT_COL_ARRAY,
      chooseSeatType,
      chooseSeatCount,
      chooseSeatNames,
      clickSeat,
      handleOk,
      submitLoading
    };
  },
});
</script>

<style scoped>
.order-train {
  margin-bottom: 15px;
  border-radius: 5px;
  border: 1px solid #1890ff;
  padding: 12px;
  background-color: #f0f7ff;
}
.order-train-main {
  font-size: 18px;
  font-weight: bold;
}
.order-train-ticket {
  margin-top: 10px;
}
.order-seat {
  margin-top: 20px;
  padding: 15px;
  background-color: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}
.order-seat-row {
  margin-top: 10px;
}
</style>
