const $ = (selector) => document.querySelector(selector);
const money = (value) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(value || 0));
const today = new Date();
const dateString = (date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
let categories = [];
let currentPage = 1;
let totalEntries = 0;
let editingId = null;
let toastTimer;

$('#summary-from').value = dateString(monthStart);
$('#summary-to').value = dateString(today);
$('#filter-from').value = dateString(monthStart);
$('#filter-to').value = dateString(today);
$('#entry-date').value = dateString(today);

async function api(path, options = {}) {
  const response = await fetch(path, { ...options, headers: { 'Content-Type': 'application/json', ...(options.headers || {}) } });
  let payload;
  try { payload = await response.json(); } catch { throw new Error('服务器返回了无法读取的内容'); }
  if (!response.ok || !payload.success) throw new Error(payload.error?.message || `请求失败（${response.status}）`);
  return payload.data;
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 2600);
}

function setMessage(message, success = false) {
  const node = $('#form-message');
  node.textContent = message;
  node.classList.toggle('success', success);
}

function fillCategoryOptions() {
  const type = $('#entry-type').value;
  const entrySelect = $('#entry-category');
  const previous = entrySelect.value;
  entrySelect.replaceChildren();
  categories.filter((category) => category.type === type).forEach((category) => {
    const option = document.createElement('option');
    option.value = category.categoryId;
    option.textContent = category.categoryName;
    entrySelect.append(option);
  });
  if ([...entrySelect.options].some((option) => option.value === previous)) entrySelect.value = previous;

  const filter = $('#filter-category');
  const filterPrevious = filter.value;
  filter.replaceChildren(new Option('全部分类', ''));
  categories.filter((category) => !$('#filter-type').value || category.type === $('#filter-type').value).forEach((category) => {
    filter.add(new Option(`${category.categoryName} · ${category.type === 'INCOME' ? '收入' : '支出'}`, category.categoryId));
  });
  if ([...filter.options].some((option) => option.value === filterPrevious)) filter.value = filterPrevious;
}

async function loadSummary() {
  const params = new URLSearchParams();
  if ($('#summary-from').value) params.set('from', $('#summary-from').value);
  if ($('#summary-to').value) params.set('to', $('#summary-to').value);
  const summary = await api(`/api/v1/ledger/summary?${params}`);
  categories = summary.categories;
  $('#income-total').textContent = money(summary.incomeTotal);
  $('#expense-total').textContent = money(summary.expenseTotal);
  $('#balance-total').textContent = money(summary.balance);
  const hasAmounts = summary.categories.some((category) => Number(category.total) > 0 && category.count > 0);
  $('#summary-grid').hidden = !hasAmounts;
  renderCategories(summary.categories);
  fillCategoryOptions();
}

function renderCategories(items) {
  const list = $('#category-list');
  list.replaceChildren();
  const activeCategories = items.filter((category) => Number(category.total) > 0 && category.count > 0);
  const panel = $('.category-panel');
  panel.hidden = activeCategories.length === 0;
  activeCategories.forEach((category) => {
    const item = document.createElement('div');
    item.className = 'category-item';
    const name = document.createElement('span');
    name.className = 'category-name';
    const dot = document.createElement('i');
    dot.className = `category-dot ${category.type === 'EXPENSE' ? 'expense' : ''}`;
    const label = document.createElement('span');
    label.textContent = `${category.categoryName} · ${category.type === 'INCOME' ? '收入' : '支出'}`;
    name.append(dot, label);
    const amount = document.createElement('strong');
    amount.className = 'category-value';
    amount.textContent = money(category.total);
    item.append(name, amount);
    list.append(item);
  });
}

function renderEntries(entries) {
  const list = $('#ledger-list');
  list.replaceChildren();
  if (!entries.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.textContent = '这段时间还没有账目，先记下第一笔吧。';
    list.append(empty);
    return;
  }
  entries.forEach((entry) => {
    const row = document.createElement('article');
    row.className = 'entry-row';
    const details = document.createElement('div');
    details.className = 'entry-category';
    const icon = document.createElement('span');
    icon.className = `entry-icon ${entry.type === 'EXPENSE' ? 'expense' : ''}`;
    icon.textContent = entry.type === 'INCOME' ? '↗' : '↙';
    const text = document.createElement('div');
    const category = document.createElement('div');
    category.className = 'entry-name';
    category.textContent = entry.categoryName;
    text.append(category);
    if (entry.note) {
      const note = document.createElement('div');
      note.className = 'entry-note';
      note.textContent = entry.note;
      text.append(note);
    }
    details.append(icon, text);
    const type = document.createElement('span');
    type.className = 'entry-type-label';
    type.textContent = entry.type === 'INCOME' ? '收入' : '支出';
    const date = document.createElement('time');
    date.className = 'entry-date';
    date.dateTime = entry.occurredOn;
    date.textContent = entry.occurredOn;
    const amount = document.createElement('strong');
    amount.className = `entry-amount ${entry.type.toLowerCase()}`;
    amount.textContent = `${entry.type === 'INCOME' ? '+' : '−'}${money(entry.amount)}`;
    const actions = document.createElement('div');
    actions.className = 'entry-actions';
    const edit = document.createElement('button');
    edit.className = 'action-button'; edit.type = 'button'; edit.textContent = '编辑';
    edit.addEventListener('click', () => beginEdit(entry));
    const remove = document.createElement('button');
    remove.className = 'action-button delete'; remove.type = 'button'; remove.textContent = '删除';
    remove.addEventListener('click', () => removeEntry(entry));
    actions.append(edit, remove);
    row.append(details, type, date, amount, actions);
    list.append(row);
  });
}

async function loadEntries() {
  const list = $('#ledger-list');
  list.setAttribute('aria-busy', 'true');
  const params = new URLSearchParams({ page: currentPage, size: 10 });
  if ($('#filter-from').value) params.set('from', $('#filter-from').value);
  if ($('#filter-to').value) params.set('to', $('#filter-to').value);
  if ($('#filter-type').value) params.set('type', $('#filter-type').value);
  if ($('#filter-category').value) params.set('categoryId', $('#filter-category').value);
  try {
    const result = await api(`/api/v1/ledger/entries?${params}`);
    totalEntries = result.total;
    renderEntries(result.records);
    $('#page-label').textContent = `第 ${result.current} 页 · 共 ${result.total} 笔`;
    $('#page-prev').disabled = result.current <= 1;
    $('#page-next').disabled = result.current * result.size >= result.total;
  } catch (error) {
    list.replaceChildren();
    const message = document.createElement('div'); message.className = 'empty-state'; message.textContent = error.message; list.append(message);
  } finally { list.setAttribute('aria-busy', 'false'); }
}

async function refreshAll() {
  try { await loadSummary(); await loadEntries(); }
  catch (error) { showToast(error.message); }
}

function beginEdit(entry) {
  editingId = entry.id;
  $('#form-title').textContent = '修改账目';
  $('#submit-entry').textContent = '保存修改';
  $('#cancel-edit').hidden = false;
  $('#entry-type').value = entry.type;
  fillCategoryOptions();
  $('#entry-category').value = entry.categoryId;
  $('#entry-amount').value = entry.amount;
  $('#entry-date').value = entry.occurredOn;
  $('#entry-note').value = entry.note || '';
  setMessage('正在修改这笔账目');
  window.scrollTo({ top: $('#entry-form').getBoundingClientRect().top + window.scrollY - 20, behavior: 'smooth' });
}

function resetForm() {
  editingId = null;
  $('#entry-form').reset();
  $('#entry-type').value = 'EXPENSE';
  $('#entry-date').value = dateString(today);
  $('#form-title').textContent = '记一笔';
  $('#submit-entry').textContent = '保存账目';
  $('#cancel-edit').hidden = true;
  fillCategoryOptions();
  setMessage('');
}

async function removeEntry(entry) {
  if (!window.confirm(`确定删除「${entry.categoryName}」的这笔账目吗？`)) return;
  try {
    await api(`/api/v1/ledger/entries/${entry.id}`, { method: 'DELETE' });
    if (currentPage > 1 && totalEntries % 10 === 1) currentPage -= 1;
    await refreshAll(); showToast('账目已删除');
  } catch (error) { showToast(error.message); }
}

$('#entry-type').addEventListener('change', fillCategoryOptions);
$('#filter-type').addEventListener('change', fillCategoryOptions);
$('#summary-refresh').addEventListener('click', refreshAll);
$('#filter-apply').addEventListener('click', () => { currentPage = 1; loadEntries(); });
$('#page-prev').addEventListener('click', () => { if (currentPage > 1) { currentPage -= 1; loadEntries(); } });
$('#page-next').addEventListener('click', () => { currentPage += 1; loadEntries(); });
$('#cancel-edit').addEventListener('click', resetForm);
$('#entry-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  setMessage('');
  const payload = { categoryId: Number($('#entry-category').value), amount: Number($('#entry-amount').value), occurredOn: $('#entry-date').value, note: $('#entry-note').value.trim() || null };
  try {
    await api(editingId ? `/api/v1/ledger/entries/${editingId}` : '/api/v1/ledger/entries', {
      method: editingId ? 'PUT' : 'POST', body: JSON.stringify(payload)
    });
    const wasEditing = editingId !== null;
    resetForm(); currentPage = 1; await refreshAll(); showToast(wasEditing ? '账目已更新' : '账目已保存');
  } catch (error) { setMessage(error.message); }
});

refreshAll();
