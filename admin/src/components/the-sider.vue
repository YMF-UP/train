<template>
<a-layout-sider width="200" style="background: #fff">
        <a-menu
            v-model:selectedKeys="selectedKeys2"
            v-model:openKeys="openKeys"
            mode="inline"
            :style="{ height: '100%', borderRight: 0 }"
        >


            <a-menu-item key='/welcome'>
            <router-link to="/welcome">
              <coffee-outlined /> &nbsp;欢迎
            </router-link>
            </a-menu-item>
           <a-menu-item key='/about'>
              <router-link to="/about">
                <coffee-outlined /> &nbsp;关于
              </router-link>
            </a-menu-item>
          <a-menu-item key='/station'>
            <router-link to="/station">
              <coffee-outlined /> &nbsp;车站管理
            </router-link>
          </a-menu-item>
          <a-menu-item key='/train'>
            <router-link to="/train">
              <coffee-outlined /> &nbsp;火车管理
            </router-link>
          </a-menu-item>
          <a-menu-item key='/train-station'>
            <router-link to="/train-station">
              <coffee-outlined /> &nbsp;火车车站
            </router-link>
          </a-menu-item>
          <a-menu-item key='/train-carriage'>
            <router-link to="/train-carriage">
              <coffee-outlined /> &nbsp;火车车厢
            </router-link>
          </a-menu-item>
          <a-menu-item key='/train-seat'>
            <router-link to="/train-seat">
              <coffee-outlined /> &nbsp;火车座位
            </router-link>
          </a-menu-item>
          <a-menu-item key='/batch-job'>
            <router-link to="/batch-job">
              <coffee-outlined /> &nbsp;定时任务
            </router-link>
          </a-menu-item>
          <a-menu-item key='/daily-train'>
            <router-link to="/daily-train">
              <coffee-outlined /> &nbsp;每日车次
            </router-link>
          </a-menu-item>
          <a-menu-item key='/daily-train-station'>
            <router-link to="/daily-train-station">
              <coffee-outlined /> &nbsp;每日车站
            </router-link>
          </a-menu-item>
          <a-menu-item key='/daily-train-carriage'>
            <router-link to="/daily-train-carriage">
              <coffee-outlined /> &nbsp;每日车厢
            </router-link>
          </a-menu-item>
          <a-menu-item key='/daily-train-seat'>
            <router-link to="/daily-train-seat">
              <coffee-outlined /> &nbsp;每日座位
            </router-link>
          </a-menu-item>
          <a-menu-item key='/daily-train-ticket'>
            <router-link to="/daily-train-ticket">
              <coffee-outlined /> &nbsp;余票信息
            </router-link>
          </a-menu-item>
          <a-menu-item key='/confirm-order'>
            <router-link to="/confirm-order">
              <coffee-outlined /> &nbsp;确认订单
            </router-link>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>
</template>
<script>
import {defineComponent, ref, watch} from 'vue';
import router from "@/router";


export default defineComponent({
  name: "the-sider-view",
    setup() {

     const selectedKeys=ref([]);
      watch(() => router.currentRoute.value.path, (newValue) => {
        console.log('watch', newValue);
        selectedKeys.value = [];
        selectedKeys.value.push(newValue);
      }, {immediate: true});

    return {
      selectedKeys2: ref(['1']),
      openKeys: ref(['sub1'])
    };
  },
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>

