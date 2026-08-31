<template>
  <p>
    <a-space>
      <a-button type="primary" @click="handleQuery()">刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="confirmOrders"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'operation'">
        <a-space>
          <a-popconfirm
              title="删除后不可恢复，确认删除?"
              @confirm="onDelete(record)"
              ok-text="确认" cancel-text="取消">
            <a style="color: red">删除</a>
          </a-popconfirm>
        </a-space>
      </template>
      <template v-else-if="column.dataIndex === 'status'">
        <span v-for="item in CONFIRM_ORDER_STATUS_ARRAY" :key="item.code">
          <span v-if="item.code === record.status">
            {{item.desc}}
          </span>
        </span>
      </template>
    </template>
  </a-table>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import {notification} from "ant-design-vue";
import axios from "axios";

export default defineComponent({
  name: "confirm-order-view",
  setup() {
    const CONFIRM_ORDER_STATUS_ARRAY = window.CONFIRM_ORDER_STATUS_ARRAY || [
      {code: "I", desc: "初始"},
      {code: "P", desc: "处理中"},
      {code: "S", desc: "成功"},
      {code: "F", desc: "失败"},
      {code: "C", desc: "取消"},
      {code: "E", desc: "无票"}
    ];
    const confirmOrders = ref([]);
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    });
    let loading = ref(false);
    const columns = [
      {
        title: '会员id',
        dataIndex: 'memberId',
        key: 'memberId',
      },
      {
        title: '日期',
        dataIndex: 'date',
        key: 'date',
      },
      {
        title: '车次编号',
        dataIndex: 'trainCode',
        key: 'trainCode',
      },
      {
        title: '出发站',
        dataIndex: 'start',
        key: 'start',
      },
      {
        title: '到达站',
        dataIndex: 'end',
        key: 'end',
      },
      {
        title: '余票ID',
        dataIndex: 'dailyTrainTicketId',
        key: 'dailyTrainTicketId',
      },
      {
        title: '车票',
        dataIndex: 'tickets',
        key: 'tickets',
      },
      {
        title: '订单状态',
        dataIndex: 'status',
        key: 'status',
      },
      {
        title: '操作',
        dataIndex: 'operation'
      }
    ];

    const onDelete = (record) => {
      axios.delete("/business/admin/confirm-order/delete/" + record.id).then((response) => {
        const data = response.data;
        if (data.success) {
          notification.success({description: "删除成功！"});
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize,
          });
        } else {
          notification.error({description: data.message});
        }
      });
    };

    const handleQuery = (param) => {
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        };
      }
      loading.value = true;
      axios.get("/business/admin/confirm-order/query-list", {
        params: {
          page: param.page,
          size: param.size
        }
      }).then((response) => {
        loading.value = false;
        let data = response.data;
        if (data.success) {
          confirmOrders.value = data.content.list;
          pagination.value.current = param.page;
          pagination.value.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    const handleTableChange = (page) => {
      pagination.value.pageSize = page.pageSize;
      handleQuery({
        page: page.current,
        size: page.pageSize
      });
    };

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });

    return {
      CONFIRM_ORDER_STATUS_ARRAY,
      confirmOrders,
      pagination,
      columns,
      handleTableChange,
      handleQuery,
      loading,
      onDelete
    };
  },
});
</script>

<style scoped>
</style>
