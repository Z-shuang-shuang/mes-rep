// utils/request.ts
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import type { AxiosRequestConfig } from 'axios'

// 定义响应数据结构
export interface ResponseData<T = any> {
  code: number
  msg: string
  data: T
}

interface RequestOptions {
  showLoading?: boolean
  showError?: boolean
  errorMessage?: string
  retry?: number
  retryDelay?: number
}

// 默认配置
const defaultOptions: RequestOptions = {
  showLoading: false,
  showError: true,
  retry: 0,
  retryDelay: 1000
}

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://47.100.25.50:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从 pinia store 获取 token（更现代的状态管理）
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳防止缓存（可选）
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    return config
  },
  (error) => {
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { data, config } = response
    const { code, msg } = data
    
    // 获取请求的自定义配置
    const requestOptions = (config as any).requestOptions || defaultOptions
    
    // 业务成功
    if (code === 200 || code === 0) {
      return data
    }
    
    // token 过期或未登录
    if (code === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      })
      return Promise.reject(new Error('登录已过期'))
    }
    
    // 权限不足
    if (code === 403) {
      ElMessage.error('权限不足，无法访问')
      return Promise.reject(new Error('权限不足'))
    }
    
    // 其他业务错误
    if (requestOptions.showError) {
      ElMessage.error(requestOptions.errorMessage || msg || '请求失败')
    }
    
    return Promise.reject(new Error(msg || '请求失败'))
  },
  async (error) => {
    const { config, response, message } = error
    
    // 网络错误处理
    if (!response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }
    
    const { status, data } = response
    const requestOptions = config?.requestOptions || defaultOptions
    
    // HTTP 状态码错误处理
    switch (status) {
      case 400:
        ElMessage.error(data?.msg || '请求参数错误')
        break
      case 401:
        // 已在上面处理，避免重复
        break
      case 403:
        ElMessage.error('权限不足')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        if (requestOptions.showError) {
          ElMessage.error(data?.msg || message || '请求失败')
        }
    }
    
    // 重试机制（用于关键接口）
    if (requestOptions.retry && config && config.__retryCount === undefined) {
      config.__retryCount = 0
    }
    
    if (config && requestOptions.retry && config.__retryCount < requestOptions.retry) {
      config.__retryCount += 1
      await new Promise(resolve => setTimeout(resolve, requestOptions.retryDelay))
      return service(config)
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法（支持泛型）
const request = <T = any>(
  url: string,
  method: string,
  data?: any,
  options?: RequestOptions
): Promise<ResponseData<T>> => {
  const config: AxiosRequestConfig = {
    url,
    method,
    requestOptions: { ...defaultOptions, ...options }
  }
  
  if (method.toLowerCase() === 'get') {
    config.params = data
  } else {
    config.data = data
  }
  
  return service(config)
}

// 导出常用方法
export const http = {
  get: <T = any>(url: string, params?: any, options?: RequestOptions) =>
    request<T>(url, 'get', params, options),
  
  post: <T = any>(url: string, data?: any, options?: RequestOptions) =>
    request<T>(url, 'post', data, options),
  
  put: <T = any>(url: string, data?: any, options?: RequestOptions) =>
    request<T>(url, 'put', data, options),
  
  delete: <T = any>(url: string, params?: any, options?: RequestOptions) =>
    request<T>(url, 'delete', params, options),
  
  patch: <T = any>(url: string, data?: any, options?: RequestOptions) =>
    request<T>(url, 'patch', data, options),
  
  upload: <T = any>(url: string, file: File, fieldName = 'file', options?: RequestOptions) => {
    const formData = new FormData()
    formData.append(fieldName, file)
    return request<T>(url, 'post', formData, {
      ...options,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export default service
