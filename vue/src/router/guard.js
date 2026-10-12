import { getToken, getUser } from '@/utils/auth'

/** 各角色登录后的首页 */
export const roleHome = {
  ADMIN: '/admin/home',
  TEACHER: '/teacher/home',
  STUDENT: '/student/home',
}

/**
 * 全局路由守卫。
 *
 * <p>返回 `true` 表示放行；返回路径字符串则由 vue-router 重定向到该路径。</p>
 *
 * <p>规则：公开页面（登录、注册）直接放行；未登录一律跳登录页；
 * 已登录但访问了不属于自己角色的页面时，送回该角色自己的首页（不清除登录态）。</p>
 *
 * <p>单独放在这里而不是内联在 `index.js` 里，是为了能脱离 router 实例独立测试。</p>
 */
export function authGuard(to) {
  if (to.meta?.public) {
    return true
  }
  if (!getToken()) {
    return '/login'
  }
  const role = getUser().role
  const needRole = to.matched?.find((r) => r.meta?.role)?.meta?.role
  if (needRole && role !== needRole) {
    return roleHome[role] || '/login'
  }
  return true
}
