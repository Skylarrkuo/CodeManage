import MarkdownIt from 'markdown-it'

// 全局 Markdown 渲染器
// - 禁用原始 HTML，防止 XSS
// - 启用换行 & 链接自动识别
const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
})

/**
 * 将 Markdown 文本渲染为 HTML 字符串
 * @param {string} text
 * @returns {string}
 */
export function renderMarkdown(text) {
  if (!text) return ''
  return md.render(String(text))
}
