import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'

/**
 * 测试专用配置。
 *
 * <p>刻意**不**复用 `vite.config.js`：那份配置里的 `unplugin-element-plus({ useSource: true })`
 * 会把 `element-plus` 的导入改写成源码路径（连带 `.scss`），测试环境解析不了 scss 会直接报
 * `Unknown file extension ".scss"`。 </p>
 *
 * <p>当前测试只覆盖纯逻辑（auth / request 拦截器 / 路由守卫），不挂载组件、不导入真实的
 * element-plus，因此不需要 vue / element-plus / scss 这几类插件。若将来要加组件挂载测试，
 * 需要在这里补上 `@vitejs/plugin-vue` 并处理样式。</p>
 */
export default defineConfig({
  resolve: {
    // 注意：需与 vite.config.js 中的别名保持一致
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.js'],
    css: false,
  },
})
