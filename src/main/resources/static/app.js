// 共用小工具：呼叫 API、登入狀態、填下拉選單、格式化

// Spring Security 把 CSRF token 放在 XSRF-TOKEN cookie；送出 POST / PUT / DELETE 時要放進標頭
function csrfToken() {
  const m = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  return m ? decodeURIComponent(m[1]) : null;
}

// 呼叫後端 API。失敗時丟出 Error，訊息是後端回傳的 detail（例如「Email 或密碼錯誤」）
async function api(path, { method = 'GET', body } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET') {
    const token = csrfToken();
    if (token) headers['X-XSRF-TOKEN'] = token;
  }
  const res = await fetch(path, {
    method, headers, credentials: 'same-origin',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.detail || `錯誤 ${res.status}`);
    err.status = res.status;
    throw err;
  }
  return data;
}

// 目前登入者；沒登入回傳 null
async function currentUser() {
  try {
    return await api('/api/me');
  } catch (e) {
    if (e.status === 401) return null;
    throw e;
  }
}

const ROLE_LABEL = { audience: '觀眾', performer: '表演者', venue: '場地方', admin: '平台管理者' };

// 導覽列右邊：沒登入顯示「登入」，登入後顯示暱稱（連到我的帳號）
async function renderNav() {
  const nav = document.querySelector('header nav');
  const me = await currentUser().catch(() => null);
  const link = document.createElement('a');
  link.href = me ? 'account.html' : 'login.html';
  link.textContent = me ? `👤 ${me.display_name}` : '登入 / 註冊';
  if (location.pathname.endsWith('/' + link.getAttribute('href'))) link.classList.add('active');
  nav?.appendChild(link);
  return me;
}

// 每個頁面載入 app.js 後就會開始畫導覽列；頁面可以用 meReady.then(me => ...) 取得登入者
const meReady = renderNav();

async function fillSelect(select, path) {
  for (const value of await api(path)) select.add(new Option(value, value));
}

const WEEK = '日一二三四五六';
function fmtDate(iso) {
  const d = new Date(iso + 'T00:00:00');
  return `${d.getMonth() + 1}/${d.getDate()}（${WEEK[d.getDay()]}）`;
}

const money = n => Number(n).toLocaleString('zh-TW');

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

// 在表單下方顯示成功 / 錯誤訊息
function showMsg(el, text, ok = false) {
  el.textContent = text;
  el.className = ok ? 'msg ok' : 'msg error';
}
