import { describe, it, expect, beforeEach } from 'vitest'
import {
  getToken,
  setToken,
  getUser,
  setUser,
  clearAuth,
  authHeaders,
} from '@/utils/auth'

const TOKEN_KEY = 'system-token'
const USER_KEY = 'system-user'

describe('utils/auth', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('token 存取', () => {
    it('未设置时返回空字符串', () => {
      expect(getToken()).toBe('')
    })

    it('写入后能读回', () => {
      setToken('abc.def.ghi')
      expect(getToken()).toBe('abc.def.ghi')
      expect(localStorage.getItem(TOKEN_KEY)).toBe('abc.def.ghi')
    })

    it('传入空值等同于清除', () => {
      setToken('abc')
      setToken('')
      expect(getToken()).toBe('')
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull()

      setToken('abc')
      setToken(null)
      expect(getToken()).toBe('')
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
    })
  })

  describe('用户信息存取', () => {
    it('未设置时返回空对象', () => {
      expect(getUser()).toEqual({})
    })

    it('存了非法 JSON 时返回空对象而不是抛异常', () => {
      localStorage.setItem(USER_KEY, '{ 这不是合法 JSON')
      expect(getUser()).toEqual({})
    })

    it('setUser / getUser 往返一致', () => {
      const user = { id: 7, username: 'stu7', name: '学生七', role: 'STUDENT' }
      setUser(user)
      expect(getUser()).toEqual(user)
    })

    it('传入空值时不写入 undefined 字面量', () => {
      setUser(null)
      expect(getUser()).toEqual({})
    })
  })

  describe('clearAuth', () => {
    it('同时清除 token 与用户信息', () => {
      setToken('abc')
      setUser({ id: 1, role: 'ADMIN' })

      clearAuth()

      expect(getToken()).toBe('')
      expect(getUser()).toEqual({})
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull()
      expect(localStorage.getItem(USER_KEY)).toBeNull()
    })
  })

  describe('authHeaders（供 el-upload 等不走 axios 的地方使用）', () => {
    it('无 token 时返回空对象', () => {
      expect(authHeaders()).toEqual({})
    })

    it('有 token 时返回 Bearer 头', () => {
      setToken('abc.def')
      expect(authHeaders()).toEqual({ Authorization: 'Bearer abc.def' })
    })
  })
})
