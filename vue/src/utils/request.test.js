import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn(), success: vi.fn() },
}))
vi.mock('@/router', () => ({
  default: { push: vi.fn() },
}))
vi.mock('@/utils/auth', () => ({
  getToken: vi.fn(),
  clearAuth: vi.fn(),
}))

import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, clearAuth } from '@/utils/auth'
import {
  onRequestFulfilled,
  onRequestRejected,
  onResponseFulfilled,
  onResponseRejected,
} from '@/utils/request'

describe('utils/request 拦截器', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 「其它状态」分支会 console.error，测试里静音以免污染输出
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  describe('请求拦截器', () => {
    it('总是设置 Content-Type', () => {
      getToken.mockReturnValue('')
      const config = onRequestFulfilled({ headers: {} })
      expect(config.headers['Content-Type']).toBe('application/json;charset=utf-8')
    })

    it('有 token 时注入 Bearer 头', () => {
      getToken.mockReturnValue('abc.def')
      const config = onRequestFulfilled({ headers: {} })
      expect(config.headers['Authorization']).toBe('Bearer abc.def')
    })

    it('无 token 时完全不写 Authorization 头', () => {
      getToken.mockReturnValue('')
      const config = onRequestFulfilled({ headers: {} })
      expect(config.headers).not.toHaveProperty('Authorization')
    })

    it('拒绝回调原样抛出错误', async () => {
      const err = new Error('boom')
      await expect(onRequestRejected(err)).rejects.toBe(err)
    })
  })

  describe('响应拦截器（业务成功）', () => {
    it('blob 响应原样透传，不做解包', () => {
      const blob = { fake: 'blob' }
      const out = onResponseFulfilled({ data: blob, config: { responseType: 'blob' } })
      expect(out).toBe(blob)
    })

    it('字符串响应体被解析成对象', () => {
      const out = onResponseFulfilled({
        data: JSON.stringify({ code: '200', msg: 'ok' }),
        config: {},
      })
      expect(out).toEqual({ code: '200', msg: 'ok' })
    })

    it('业务码 401（HTTP 200）时清登录态并跳登录页', () => {
      const out = onResponseFulfilled({
        data: { code: '401', msg: '登录已失效' },
        config: {},
      })
      expect(out.code).toBe('401')
      expect(ElMessage.error).toHaveBeenCalledWith('登录已失效')
      expect(clearAuth).toHaveBeenCalled()
      expect(router.push).toHaveBeenCalledWith('/login')
    })

    it('正常响应不触发任何副作用', () => {
      onResponseFulfilled({ data: { code: '200' }, config: {} })
      expect(clearAuth).not.toHaveBeenCalled()
      expect(router.push).not.toHaveBeenCalled()
    })
  })

  describe('响应拦截器（HTTP 错误）', () => {
    it('HTTP 401 清登录态并跳登录页', async () => {
      const err = { response: { status: 401, data: { msg: '令牌失效' } } }
      await expect(onResponseRejected(err)).rejects.toBe(err)
      expect(ElMessage.error).toHaveBeenCalledWith('令牌失效')
      expect(clearAuth).toHaveBeenCalled()
      expect(router.push).toHaveBeenCalledWith('/login')
    })

    it('HTTP 403 只提示，不清登录态、不跳转', async () => {
      const err = { response: { status: 403, data: { msg: '无权限执行该操作' } } }
      await expect(onResponseRejected(err)).rejects.toBe(err)
      expect(ElMessage.error).toHaveBeenCalledWith('无权限执行该操作')
      expect(clearAuth).not.toHaveBeenCalled()
      expect(router.push).not.toHaveBeenCalled()
    })

    it('HTTP 403 且后端未给 msg 时使用默认文案', async () => {
      const err = { response: { status: 403, data: {} } }
      await expect(onResponseRejected(err)).rejects.toBe(err)
      expect(ElMessage.error).toHaveBeenCalledWith('无权限执行该操作')
    })

    it('其它状态码原样抛出，不清登录态', async () => {
      const err = { response: { status: 500, data: { msg: '服务器错误' } } }
      await expect(onResponseRejected(err)).rejects.toBe(err)
      expect(clearAuth).not.toHaveBeenCalled()
      expect(router.push).not.toHaveBeenCalled()
    })
  })
})
