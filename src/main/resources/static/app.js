// 共用小工具：呼叫 API、填下拉選單、格式化
async function api(path) {
  const res = await fetch(path);
  if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
  return res.json();
}

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
