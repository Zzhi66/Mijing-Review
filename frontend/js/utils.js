/**
 * 觅境点评 —— 公共工具库
 * utils.js
 */

/* =====================
   常量配置
   ===================== */
export const BASE_URL = 'http://localhost:8081';
export const TOKEN_KEY = 'mj_token';
export const USER_KEY  = 'mj_user';

/* =====================
   Token 管理
   ===================== */
export const TokenManager = {
  get()         { return localStorage.getItem(TOKEN_KEY); },
  set(token)    { localStorage.setItem(TOKEN_KEY, token); },
  remove()      { localStorage.removeItem(TOKEN_KEY); },
  exists()      { return !!localStorage.getItem(TOKEN_KEY); },
};

/* =====================
   用户信息缓存
   ===================== */
export const UserCache = {
  get()         { const s = localStorage.getItem(USER_KEY); return s ? JSON.parse(s) : null; },
  set(user)     { localStorage.setItem(USER_KEY, JSON.stringify(user)); },
  remove()      { localStorage.removeItem(USER_KEY); },
  clear()       { TokenManager.remove(); UserCache.remove(); },
};

/* =====================
   表单校验
   ===================== */
/**
 * 校验手机号（中国大陆）
 * @param {string} phone
 * @returns {boolean}
 */
export function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone.trim());
}

/**
 * 校验验证码（6位数字）
 * @param {string} code
 * @returns {boolean}
 */
export function isValidCode(code) {
  return /^\d{6}$/.test(code.trim());
}

/* =====================
   Toast 提示
   ===================== */
let toastContainer = null;

function getToastContainer() {
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);
  }
  return toastContainer;
}

/**
 * 显示 Toast
 * @param {string} message - 提示文字
 * @param {'success'|'error'|'info'|'warning'} type - 类型
 * @param {number} duration - 持续时间（ms）
 */
export function showToast(message, type = 'info', duration = 2500) {
  const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
  const container = getToastContainer();

  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `<span>${icons[type] || 'ℹ'}</span><span>${message}</span>`;
  container.appendChild(el);

  setTimeout(() => {
    el.classList.add('hide');
    el.addEventListener('animationend', () => el.remove(), { once: true });
  }, duration);
}

export const toast = {
  success: (msg, d) => showToast(msg, 'success', d),
  error:   (msg, d) => showToast(msg, 'error', d),
  info:    (msg, d) => showToast(msg, 'info', d),
  warning: (msg, d) => showToast(msg, 'warning', d),
};

/* =====================
   倒计时工具
   ===================== */
/**
 * 启动按钮倒计时
 * @param {HTMLButtonElement} btn - 按钮元素
 * @param {number} seconds - 倒计时秒数
 * @param {string} originalText - 按钮原文字
 */
export function startCountdown(btn, seconds = 60, originalText = '发送验证码') {
  let remaining = seconds;
  btn.disabled = true;
  btn.textContent = `${remaining}s 后重发`;

  const timer = setInterval(() => {
    remaining -= 1;
    btn.textContent = `${remaining}s 后重发`;
    if (remaining <= 0) {
      clearInterval(timer);
      btn.disabled = false;
      btn.textContent = originalText;
    }
  }, 1000);

  return timer;
}

/* =====================
   日期格式化
   ===================== */
/**
 * 格式化时间为"几分钟前/几小时前"等相对时间
 * @param {string|Date} date
 * @returns {string}
 */
export function timeAgo(date) {
  const d = date instanceof Date ? date : new Date(date);
  const now = new Date();
  const diff = Math.floor((now - d) / 1000);

  if (diff < 60)     return '刚刚';
  if (diff < 3600)   return `${Math.floor(diff / 60)} 分钟前`;
  if (diff < 86400)  return `${Math.floor(diff / 3600)} 小时前`;
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`;
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
}

/**
 * 格式化日期为 YYYY-MM-DD HH:mm
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDate(date) {
  const d = date instanceof Date ? date : new Date(date);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/* =====================
   数字格式化
   ===================== */
/**
 * 格式化数字（大于1000显示 1.2k）
 * @param {number} num
 * @returns {string}
 */
export function formatCount(num) {
  if (!num) return '0';
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`;
  if (num >= 1000)  return `${(num / 1000).toFixed(1)}k`;
  return String(num);
}

/* =====================
   URL 工具
   ===================== */
/**
 * 获取 URL 查询参数
 * @param {string} key
 * @returns {string|null}
 */
export function getQuery(key) {
  return new URLSearchParams(location.search).get(key);
}

/**
 * 跳转页面
 * @param {string} path - 相对路径
 */
export function navigate(path) {
  location.href = path;
}

/* =====================
   登录守卫
   ===================== */
/**
 * 检查登录状态，未登录则跳转到登录页
 */
export function requireLogin() {
  if (!TokenManager.exists()) {
    navigate('/frontend/login.html');
    return false;
  }
  return true;
}
