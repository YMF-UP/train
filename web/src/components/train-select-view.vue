<template>
  <a-select v-model:value="trainCode"
            show-search
            allowClear
            :filter-option="filterTrainCodeOption"
            @change="onChange"
            placeholder="请选择车次"
            :style="'width: ' + localWidth">
    <a-select-option v-for="item in trains" :key="item.code" :value="item.code" :label="item.code + item.start + item.end">
      {{item.code}} {{item.start}} ~ {{item.end}}
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

    // 监听父组件传入的 modelValue 变化（编辑回显）
    watch(() => props.modelValue, (newVal) => {
      trainCode.value = newVal;
    }, {immediate: true});

    // 查所有的车次列表
    const queryAllTrain = () => {
      axios.get("/business/train/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          trains.value = data.content;
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

    // 支持按车次编号、出发站、到达站进行防抖模糊搜索
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
