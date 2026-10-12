import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(),
  getUser: vi.fn(),
}))

import { getToken, getUser } from '@/utils/auth'
import { authGuard } from '@/router/guard'

/** 构造一个假的 route 对象 */
const to = ({ meta = {}, matched = [] } = {}) => ({ meta, matched })

/** 构造一个「需要指定角色」的路由（角色写在父路由的 meta 上） */
const roleRoute = (role) => to({ meta: {}, matched: [{ meta: { role } }] })

/** 设定当前登录态 */
const loggedInAs = (role, token = 'valid.token') => {
  getToken.mockReturnValue(token)
  getUser.mockReturnValue({ id: 1, username: 'u', name: 'n', role })
}

describe('router/guard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('公开页面（登录 / 注册）', () => {
    it('未登录也放行', () => {
      getToken.mockReturnValue('')
      expect(authGuard(to({ meta: { public: true } }))).toBe(true)
    })

    it('即使角色不符也放行', () => {
      loggedInAs('STUDENT')
      const route = to({ meta: { public: true }, matched: [{ meta: { role: 'ADMIN' } }] })
      expect(authGuard(route)).toBe(true)
    })
  })

  describe('未登录', () => {
    it('访问角色受保护的页面时跳登录页', () => {
      getToken.mockReturnValue('')
      expect(authGuard(roleRoute('ADMIN'))).toBe('/login')
    })

    it('访问不要求角色的页面时也跳登录页', () => {
      getToken.mockReturnValue('')
      expect(authGuard(to())).toBe('/login')
    })
  })

  describe('已登录', () => {
    it('角色与目标页一致时放行', () => {
      loggedInAs('ADMIN')
      expect(authGuard(roleRoute('ADMIN'))).toBe(true)
    })

    it('访问不要求角色的页面时放行', () => {
      loggedInAs('STUDENT')
      expect(authGuard(to())).toBe(true)
    })

    it.each([
      ['ADMIN', 'STUDENT', '/admin/home'],
      ['TEACHER', 'ADMIN', '/teacher/home'],
      ['STUDENT', 'ADMIN', '/student/home'],
    ])('%s 访问 %s 端时被送回自己的首页', (role, otherRole, home) => {
      loggedInAs(role)
      expect(authGuard(roleRoute(otherRole))).toBe(home)
    })

    it('角色未知时跳登录页', () => {
      loggedInAs('HACKER')
      expect(authGuard(roleRoute('ADMIN'))).toBe('/login')
    })

    it('用户信息缺失时跳登录页', () => {
      getToken.mockReturnValue('valid.token')
      getUser.mockReturnValue({})
      expect(authGuard(roleRoute('ADMIN'))).toBe('/login')
    })
  })
})
