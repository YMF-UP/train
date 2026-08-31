<template>
  <a-select v-model:value="name"
            show-search
            allowClear
            :filter-option="filterNameOption"
            @change="onChange"
            placeholder="请选择车站"
            :style="'width: ' + localWidth">
    <a-select-option v-for="item in stations" :key="item.name" :value="item.name" :label="item.name + item.namePinyin + item.namePy">
      {{item.name}} {{item.namePinyin}}
    </a-select-option>
  </a-select>
</template>

<script>
import {defineComponent, ref, onMounted, watch} from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  name: "station-select-view",
  props: ["modelValue", "width"],
  emits: ['update:modelValue', 'change'],
  setup(props, {emit}) {
    const name = ref();
    const stations = ref([]);
    const localWidth = ref(props.width || "100%");

    // 监听父组件传入的 modelValue 变化（编辑回显）
    watch(() => props.modelValue, (newVal) => {
      name.value = newVal;
    }, {immediate: true});

    // 查所有的车站列表
    const queryAllStation = () => {
      axios.get("/business/admin/station/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          stations.value = data.content;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    // 下拉选中变化时通知父组件
    const onChange = (value) => {
      emit('update:modelValue', value);
      emit('change', value);
    };

    // 支持按中文站名、全拼（beijing）或简拼（bj）进行防抖模糊搜索
    const filterNameOption = (input, option) => {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    };

    onMounted(() => {
      queryAllStation();
    });

    return {
      name,
      stations,
      localWidth,
      onChange,
      filterNameOption,
    };
  },
});
</script>
