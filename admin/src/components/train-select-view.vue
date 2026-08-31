<template>
  <a-select v-model:value="trainCode"
            show-search
            allowClear
            :filter-option="filterTrainCodeOption"
            @change="onChange"
            placeholder="请选择车次"
            :style="'width: ' + localWidth">
    <a-select-option v-for="item in trains" :key="item.code" :value="item.code" :label="item.code + item.name">
      {{item.code}} {{item.name}}
    </a-select-option>
  </a-select>
</template>

<script>
import {defineComponent, ref, onMounted, watch} from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  name: "train-select-view",
  props: ["modelValue", "width"],
  emits: ['update:modelValue', 'change'],
  setup(props, {emit}) {
    const trainCode = ref();
    const trains = ref([]);
    const localWidth = ref(props.width || "100%");

    // 监听父组件传入的 modelValue 的变化 (如父组件编辑数据时回显)
    watch(() => props.modelValue, (newVal) => {
      trainCode.value = newVal;
    }, {immediate: true});

    // 查所有的车次列表
    const queryAllTrain = () => {
      axios.get("/business/admin/train/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          trains.value = data.content;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    // 当下拉框选中值变化时，通知父组件更新 v-model
    const onChange = (value) => {
      emit('update:modelValue', value);
      emit('change', value);
    };

    // 支持在下拉框中按车次 code 或 name 模糊搜索
    const filterTrainCodeOption = (input, option) => {
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    };

    onMounted(() => {
      queryAllTrain();
    });

    return {
      trainCode,
      trains,
      localWidth,
      onChange,
      filterTrainCodeOption,
    };
  },
});
</script>
