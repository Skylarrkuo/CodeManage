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

/** 角色中文名，用于提示文案 */
const ROLE_LABEL = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
}

/**
 * 校验当前用户能否进入某个角色的控制台。
 *
 * <p>三个端 Layout 进入时的自查。注意这是**纵深防御**：路由守卫在进入 Layout 之前
 * 已经保证了 token 有效且角色匹配，所以正常导航下这里不会拦到人。</p>
 *
 * @param user 当前用户信息（来自 {@link getUser}）
 * @param requiredRole 该控制台要求的角色：ADMIN / TEACHER / STUDENT
 * @returns {string|null} 通过返回 null；否则返回需要提示给用户的错误文案
 */
export const consoleAccessError = (user, requiredRole) => {
  if (!user?.id) {
    return '请登录！'
  }
  if (user.role !== requiredRole) {
    return `当前账号没有${ROLE_LABEL[requiredRole] || ''}权限`
  }
  return null
}
