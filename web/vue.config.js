const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,

  devServer: {
    historyApiFallback: true, // 关键配置：启用 HTML5 History API 回退
  }
})
