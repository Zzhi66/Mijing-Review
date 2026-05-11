/**
 * 觅境点评 —— HTTP 请求封装
 * api.js
 */

import { BASE_URL, TokenManager, navigate } from './utils.js';

/**
 * 通用请求函数
 * @param {string} path - 接口路径（不含 BASE_URL）
 * @param {RequestInit} options - fetch 配置项
 * @returns {Promise<{success, data, errorMsg, total}>}
 */
async function request(path, options = {}) {
  const token = TokenManager.get();

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'authorization': token } : {}),
    ...(options.headers || {}),
  };

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  // 401 未授权 → 清除 token 跳转登录
  if (response.status === 401) {
    TokenManager.remove();
    navigate('/frontend/login.html');
    return null;
  }

  const result = await response.json();
  return result;
}

/* =====================
   用户相关 API
   ===================== */
export const UserAPI = {
  /**
   * 发送验证码
   * @param {string} phone
   */
  sendCode(phone) {
    return request(`/user/code?phone=${encodeURIComponent(phone)}`, { method: 'POST' });
  },

  /**
   * 登录
   * @param {{ phone: string, code: string }} form
   */
  login(form) {
    return request('/user/login', {
      method: 'POST',
      body: JSON.stringify(form),
    });
  },

  /**
   * 退出登录
   */
  logout() {
    return request('/user/logout', { method: 'POST' });
  },

  /**
   * 获取当前登录用户信息
   */
  me() {
    return request('/user/me');
  },

  /**
   * 根据 ID 查询用户
   * @param {number} id
   */
  getUserById(id) {
    return request(`/user/${id}`);
  },
};

/* =====================
   商铺相关 API
   ===================== */
export const ShopAPI = {
  getById(id) {
    return request(`/shop/${id}`);
  },

  getByType(typeId, current = 1, x, y) {
    const params = new URLSearchParams({ typeId, current });
    if (x !== undefined) params.set('x', x);
    if (y !== undefined) params.set('y', y);
    return request(`/shop/geo/list?${params}`);
  },

  update(shop) {
    return request('/shop', { method: 'PUT', body: JSON.stringify(shop) });
  },
};

/* =====================
   商铺分类 API
   ===================== */
export const ShopTypeAPI = {
  list() {
    return request('/shop-type/list');
  },
};

/* =====================
   博客笔记 API
   ===================== */
export const BlogAPI = {
  getHot(current = 1) {
    return request(`/blog/hot?current=${current}`);
  },

  getById(id) {
    return request(`/blog/${id}`);
  },

  save(blog) {
    return request('/blog', { method: 'POST', body: JSON.stringify(blog) });
  },

  like(id) {
    return request(`/blog/like/${id}`, { method: 'PUT' });
  },

  getLikes(id) {
    return request(`/blog/likes/${id}`);
  },

  getFollowFeed(lastId, offset = 0) {
    return request(`/blog/of/follow?lastId=${lastId}&offset=${offset}`);
  },
};

/* =====================
   优惠券 API
   ===================== */
export const VoucherAPI = {
  listByShop(shopId) {
    return request(`/voucher/list/${shopId}`);
  },

  add(voucher) {
    return request('/voucher', { method: 'POST', body: JSON.stringify(voucher) });
  },

  addSeckill(voucher) {
    return request('/voucher/seckill', { method: 'POST', body: JSON.stringify(voucher) });
  },
};

/* =====================
   秒杀订单 API
   ===================== */
export const VoucherOrderAPI = {
  seckill(voucherId) {
    return request(`/voucher-order/seckill/${voucherId}`, { method: 'POST' });
  },
};

/* =====================
   AI 客服 API
   ===================== */
export const AiAPI = {
  chat(message) {
    return request('/ai/chat', {
      method: 'POST',
      body: JSON.stringify({ message }),
    });
  },
};

/* =====================
   关注 API
   ===================== */
export const FollowAPI = {
  follow(id, isFollow) {
    return request(`/follow/${id}/${isFollow}`, { method: 'PUT' });
  },

  isFollowed(id) {
    return request(`/follow/or/not/${id}`);
  },
};
