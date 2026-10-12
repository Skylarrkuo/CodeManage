import { ElMessage } from 'element-plus'
import router from '../router'
import axios from "axios";
import { clearAuth, getToken } from '@/utils/auth'

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间设置
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    // 统一注入登录令牌
    const token = getToken();
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config
}, error => {
    return Promise.reject(error)
});

// 登录态失效的统一处理：清空本地登录态并回到登录页
const handleUnauthorized = (msg) => {
    ElMessage.error(msg || '登录已失效，请重新登录');
    clearAuth();
    router.push('/login');
};

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 兜底：HTTP 200 但业务码为 401
        if (res.code === '401') {
            handleUnauthorized(res.msg);
        }
        return res;
    },
    error => {
        const status = error.response?.status;
        const msg = error.response?.data?.msg;
        if (status === 401) {
            // 认证拦截器返回的 401（令牌缺失、非法或已过期）
            handleUnauthorized(msg);
        } else if (status === 403) {
            ElMessage.error(msg || '无权限执行该操作');
        } else {
            console.error('请求异常：', error);
            if (msg) {
                ElMessage.error(msg);
            }
        }
        return Promise.reject(error)
    }
)


export default request
