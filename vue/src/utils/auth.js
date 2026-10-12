/**
 * 登录态统一存取。
 *
 * 全项目只通过本模块读写 token 与用户信息，避免 key 散落在各页面里。
 */
const TOKEN_KEY = 'system-token'
const USER_KEY = 'system-user'

/** 获取登录令牌 */
export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

/** 保存登录令牌 */
export const setToken = (token) => {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

/** 获取当前登录用户信息 */
export const getUser = () => {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || '{}')
  } catch (e) {
    return {}
  }
}

/** 保存当前登录用户信息 */
export const setUser = (user) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
}

/** 清除全部登录态（token 与用户信息） */
export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/**
 * 供 el-upload 等不走 axios 拦截器的组件使用的鉴权头。
 */
export const authHeaders = () => {
  const token = getToken()
  return token ? { Authorization: 'Bearer ' + token } : {}
}
