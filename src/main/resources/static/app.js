const state = {
  projects: [],
  tasks: [],
  activeProjectId: null,
  status: 'all',
  keyword: '',
  openComments: new Set(),
  comments: new Map(),
  editingTask: null,
  editingComment: null,
  editingProject: null
};

const elements = {
  projects: document.querySelector('#project-list'),
  projectForm: document.querySelector('#project-form'),
  taskForm: document.querySelector('#task-form'),
  taskProject: document.querySelector('#task-project'),
  taskDescription: document.querySelector('#task-description'),
  taskList: document.querySelector('#task-list'),
  taskSearch: document.querySelector('#task-search'),
  connection: document.querySelector('#connection-state'),
  toastRegion: document.querySelector('#toast-region'),
  taskDialog: document.querySelector('#edit-task-dialog'),
  taskEditForm: document.querySelector('#edit-task-form'),
  commentDialog: document.querySelector('#edit-comment-dialog'),
  commentEditForm: document.querySelector('#edit-comment-form'),
  projectDialog: document.querySelector('#edit-project-dialog'),
  projectEditForm: document.querySelector('#edit-project-form')
};

async function api(path, options = {}) {
  const response = await fetch(path, {
    method: options.method || 'GET',
    headers: options.body ? { 'Content-Type': 'application/json' } : {},
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  let envelope = null;
  try {
    envelope = await response.json();
  } catch {
    // An empty or non-JSON response is handled by the common error below.
  }
  if (!response.ok || envelope?.success === false) {
    throw new Error(envelope?.error?.message || `请求没有完成（${response.status}）`);
  }
  return envelope?.data;
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
}

function showToast(message, isError = false) {
  const toast = document.createElement('div');
  toast.className = `toast${isError ? ' is-error' : ''}`;
  toast.textContent = message;
  elements.toastRegion.append(toast);
  window.setTimeout(() => toast.remove(), 3200);
}

function setConnection(isOnline) {
  elements.connection.classList.toggle('is-online', isOnline);
  elements.connection.classList.toggle('is-offline', !isOnline);
  elements.connection.lastElementChild.textContent = isOnline ? '数据已连接' : '暂时无法连接';
}

async function loadProjects() {
  const result = await api('/api/v1/projects?page=1&size=100');
  state.projects = result.records;
  renderProjects();
}

function taskQuery() {
  const query = new URLSearchParams({ page: '1', size: '100' });
  if (state.activeProjectId) query.set('projectId', state.activeProjectId);
  if (state.status === 'open') query.set('completed', 'false');
  if (state.status === 'done') query.set('completed', 'true');
  if (state.keyword.trim()) query.set('keyword', state.keyword.trim());
  return query;
}

async function loadTasks() {
  elements.taskList.setAttribute('aria-busy', 'true');
  elements.taskList.innerHTML = '<div class="empty-state loading-state"><div class="empty-illustration">◌</div><h3>正在整理清单</h3><p>马上就好…</p></div>';
  try {
    const result = await api(`/api/v1/tasks?${taskQuery()}`);
    state.tasks = result.records;
    renderTasks(result.total);
    setConnection(true);
  } catch (error) {
    elements.taskList.innerHTML = `<div class="empty-state"><div class="empty-illustration">!</div><h3>暂时没有连接上</h3><p>${escapeHtml(error.message)}</p></div>`;
    elements.taskList.setAttribute('aria-busy', 'false');
    setConnection(false);
  }
}

function renderProjects() {
  elements.projects.innerHTML = state.projects.length
    ? state.projects.map(project => `
      <div class="project-row${String(project.id) === state.activeProjectId ? ' is-active' : ''}" data-project-row="${project.id}">
        <button class="project-select" type="button" data-action="select-project" data-project-id="${project.id}" title="${escapeHtml(project.name)}">
          <span class="project-dot" aria-hidden="true"></span><span class="project-name">${escapeHtml(project.name)}</span>
        </button>
        <button class="project-action project-edit-action" type="button" data-action="edit-project" data-project-id="${project.id}" aria-label="编辑项目 ${escapeHtml(project.name)}">⋯</button>
        <button class="project-action" type="button" data-action="delete-project" data-project-id="${project.id}" aria-label="删除项目 ${escapeHtml(project.name)}">×</button>
      </div>`).join('')
    : '<p class="project-empty">还没有项目，点击上方 ＋ 创建一个。</p>';

  elements.taskProject.innerHTML = '<option value="">独立任务</option>' + state.projects
    .map(project => `<option value="${project.id}">${escapeHtml(project.name)}</option>`).join('');
  elements.taskProject.value = state.activeProjectId || '';
  document.querySelector('#all-tasks-button').classList.toggle('is-active', state.activeProjectId === null);
}

function renderTasks(total = state.tasks.length) {
  const projectNames = new Map(state.projects.map(project => [String(project.id), project.name]));
  const completed = state.tasks.filter(task => task.completed).length;
  document.querySelector('#total-count').textContent = total;
  document.querySelector('#pending-count').textContent = Math.max(total - completed, 0);
  document.querySelector('#done-count').textContent = completed;
  document.querySelector('#board-count').textContent = `${total} 件任务`;
  document.querySelector('#view-title').textContent = state.activeProjectId
    ? (projectNames.get(state.activeProjectId) || '项目任务')
    : state.status === 'done' ? '已经完成' : state.status === 'open' ? '待完成任务' : '所有任务';
  document.querySelector('#task-board-title').textContent = state.activeProjectId
    ? '项目任务' : '任务清单';
  document.querySelectorAll('.filter-button').forEach(button => {
    button.classList.toggle('is-active', button.dataset.status === state.status);
  });

  if (!state.tasks.length) {
    const title = state.keyword ? '没有找到匹配的任务' : state.status === 'done' ? '还没有完成的任务' : '清单还是空的';
    const description = state.keyword ? '试试换个关键词。' : '记下一件想做的事，今天就有了一个开始。';
    elements.taskList.innerHTML = `<div class="empty-state"><div class="empty-illustration">✳</div><h3>${title}</h3><p>${description}</p></div>`;
    elements.taskList.setAttribute('aria-busy', 'false');
    return;
  }

  elements.taskList.innerHTML = state.tasks.map(task => {
    const taskId = String(task.id);
    const commentsOpen = state.openComments.has(taskId);
    const commentsLoaded = state.comments.has(taskId);
    const commentsMarkup = !commentsOpen ? '' : commentsLoaded
      ? renderComments(taskId, state.comments.get(taskId))
      : '<div class="comment-empty">正在加载评论…</div>';
    const projectName = task.projectId ? projectNames.get(String(task.projectId)) : null;
    return `
      <article class="task-card${task.completed ? ' is-complete' : ''}" data-task-id="${task.id}">
        <div class="task-main">
          <button class="task-toggle${task.completed ? ' is-complete' : ''}" type="button" data-action="toggle-status" data-task-id="${task.id}" aria-label="${task.completed ? '标记为待完成' : '标记为已完成'}">✓</button>
          <div class="task-copy">
            <div class="task-title">${escapeHtml(task.description)}</div>
            <div class="task-meta">${projectName ? `<span class="project-badge">${escapeHtml(projectName)}</span>` : '<span>独立任务</span>'}<span>·</span><span>${task.completed ? '已完成' : '待完成'}</span></div>
          </div>
          <div class="task-actions">
            <button class="text-button comment-toggle" type="button" data-action="toggle-comments" data-task-id="${task.id}">${commentsOpen ? '收起评论' : '评论'}</button>
            <button class="text-button" type="button" data-action="edit-task" data-task-id="${task.id}">编辑</button>
            <button class="text-button danger" type="button" data-action="delete-task" data-task-id="${task.id}">删除</button>
          </div>
        </div>
        <section class="comments-area"${commentsOpen ? '' : ' hidden'} aria-label="任务评论">
          ${commentsMarkup}
          <form class="comment-form" data-task-id="${task.id}">
            <label class="sr-only" for="comment-${task.id}">添加一条评论</label>
            <input id="comment-${task.id}" name="content" maxlength="1000" placeholder="写下进度或备注…" required>
            <button class="button button-quiet" type="submit">发送</button>
          </form>
        </section>
      </article>`;
  }).join('');
  elements.taskList.setAttribute('aria-busy', 'false');
}

function renderComments(taskId, comments) {
  if (!comments.length) return '<div class="comment-empty">还没有评论，留一句进度记录吧。</div>';
  return `<div class="comments-list">${comments.map(comment => `
    <div class="comment-row" data-comment-id="${comment.id}">
      <span class="comment-bullet" aria-hidden="true"></span>
      <div class="comment-text">${escapeHtml(comment.content)}<span class="comment-time">${formatDate(comment.createdAt)}</span></div>
      <button class="text-button" type="button" data-action="edit-comment" data-comment-id="${comment.id}" data-task-id="${taskId}">编辑</button>
      <button class="text-button danger" type="button" data-action="delete-comment" data-comment-id="${comment.id}" data-task-id="${taskId}">删除</button>
    </div>`).join('')}</div>`;
}

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(date);
}

async function refresh() {
  try {
    await loadProjects();
    await loadTasks();
  } catch (error) {
    setConnection(false);
    showToast(error.message, true);
  }
}

document.querySelector('#date-value').textContent = new Intl.DateTimeFormat('zh-CN', {
  month: 'long', day: 'numeric', weekday: 'long'
}).format(new Date());
document.querySelector('#today-label').textContent = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'long'
}).format(new Date());

document.querySelector('#all-tasks-button').addEventListener('click', () => {
  state.activeProjectId = null;
  renderProjects();
  loadTasks();
});

document.querySelector('#show-project-form').addEventListener('click', () => {
  elements.projectForm.hidden = !elements.projectForm.hidden;
  if (!elements.projectForm.hidden) elements.projectForm.querySelector('input').focus();
});

elements.projectForm.addEventListener('submit', async event => {
  event.preventDefault();
  const form = new FormData(elements.projectForm);
  try {
    const project = await api('/api/v1/projects', {
      method: 'POST', body: { name: form.get('name'), description: form.get('description') || null }
    });
    elements.projectForm.reset();
    elements.projectForm.hidden = true;
    state.activeProjectId = String(project.id);
    await loadProjects();
    await loadTasks();
    showToast('项目已创建');
  } catch (error) { showToast(error.message, true); }
});

elements.taskForm.addEventListener('submit', async event => {
  event.preventDefault();
  const description = elements.taskDescription.value.trim();
  if (!description) return;
  const projectId = elements.taskProject.value;
  try {
    await api('/api/v1/tasks', {
      method: 'POST', body: { description, projectId: projectId ? Number(projectId) : null }
    });
    elements.taskForm.reset();
    elements.taskProject.value = state.activeProjectId || '';
    await loadTasks();
    elements.taskDescription.focus();
    showToast('任务已添加');
  } catch (error) { showToast(error.message, true); }
});

let searchTimer;
elements.taskSearch.addEventListener('input', () => {
  window.clearTimeout(searchTimer);
  searchTimer = window.setTimeout(() => {
    state.keyword = elements.taskSearch.value;
    loadTasks();
  }, 220);
});
document.querySelector('#search-form').addEventListener('submit', event => event.preventDefault());

document.querySelectorAll('.filter-button').forEach(button => button.addEventListener('click', () => {
  state.status = button.dataset.status;
  loadTasks();
}));

elements.projects.addEventListener('click', async event => {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const projectId = button.dataset.projectId;
  const project = state.projects.find(item => String(item.id) === projectId);
  if (button.dataset.action === 'select-project') {
    state.activeProjectId = projectId;
    renderProjects();
    loadTasks();
    return;
  }
  if (!project) return;
  if (button.dataset.action === 'edit-project') {
    state.editingProject = project;
    document.querySelector('#edit-project-name').value = project.name;
    document.querySelector('#edit-project-description').value = project.description || '';
    elements.projectDialog.showModal();
  }
  if (button.dataset.action === 'delete-project') {
    if (!window.confirm(`删除“${project.name}”及其任务和评论？`)) return;
    try {
      await api(`/api/v1/projects/${projectId}`, { method: 'DELETE' });
      if (state.activeProjectId === projectId) state.activeProjectId = null;
      state.comments.clear();
      await loadProjects();
      await loadTasks();
      showToast('项目已删除');
    } catch (error) { showToast(error.message, true); }
  }
});

elements.taskList.addEventListener('click', async event => {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const action = button.dataset.action;
  const taskId = button.dataset.taskId;
  const task = state.tasks.find(item => String(item.id) === taskId);
  if (action === 'toggle-comments') {
    if (state.openComments.has(taskId)) {
      state.openComments.delete(taskId);
    } else {
      state.openComments.add(taskId);
      state.comments.delete(taskId);
    }
    renderTasks();
    if (state.openComments.has(taskId)) {
      try {
        const result = await api(`/api/v1/tasks/${taskId}/comments?page=1&size=100`);
        state.comments.set(taskId, result.records);
        renderTasks();
      } catch (error) { showToast(error.message, true); }
    }
    return;
  }
  if (!task) return;
  try {
    if (action === 'toggle-status') {
      await api(`/api/v1/tasks/${taskId}/status`, {
        method: 'PATCH', body: { completed: !task.completed }
      });
      await loadTasks();
    } else if (action === 'edit-task') {
      state.editingTask = task;
      document.querySelector('#edit-task-description').value = task.description;
      document.querySelector('#edit-task-completed').checked = task.completed;
      elements.taskDialog.showModal();
    } else if (action === 'delete-task') {
      if (!window.confirm('删除这项任务及其评论？')) return;
      await api(`/api/v1/tasks/${taskId}`, { method: 'DELETE' });
      state.comments.delete(taskId);
      state.openComments.delete(taskId);
      await loadTasks();
      showToast('任务已删除');
    }
  } catch (error) { showToast(error.message, true); }
});

elements.taskList.addEventListener('submit', async event => {
  const form = event.target.closest('.comment-form');
  if (!form) return;
  event.preventDefault();
  const taskId = form.dataset.taskId;
  const content = new FormData(form).get('content').trim();
  if (!content) return;
  try {
    await api(`/api/v1/tasks/${taskId}/comments`, { method: 'POST', body: { content } });
    const result = await api(`/api/v1/tasks/${taskId}/comments?page=1&size=100`);
    state.comments.set(taskId, result.records);
    renderTasks();
    document.querySelector(`#comment-${taskId}`)?.focus();
    showToast('评论已添加');
  } catch (error) { showToast(error.message, true); }
});

elements.taskList.addEventListener('click', async event => {
  const button = event.target.closest('[data-action="edit-comment"], [data-action="delete-comment"]');
  if (!button) return;
  const { commentId, taskId, action } = button.dataset;
  const comments = state.comments.get(taskId) || [];
  const comment = comments.find(item => String(item.id) === commentId);
  if (action === 'edit-comment' && comment) {
    state.editingComment = comment;
    document.querySelector('#edit-comment-content').value = comment.content;
    elements.commentDialog.showModal();
  } else if (action === 'delete-comment') {
    if (!window.confirm('删除这条评论？')) return;
    try {
      await api(`/api/v1/comments/${commentId}`, { method: 'DELETE' });
      const result = await api(`/api/v1/tasks/${taskId}/comments?page=1&size=100`);
      state.comments.set(taskId, result.records);
      renderTasks();
      showToast('评论已删除');
    } catch (error) { showToast(error.message, true); }
  }
});

elements.taskEditForm.addEventListener('submit', async event => {
  event.preventDefault();
  if (!state.editingTask) return;
  try {
    await api(`/api/v1/tasks/${state.editingTask.id}`, {
      method: 'PUT', body: {
        description: document.querySelector('#edit-task-description').value.trim(),
        completed: document.querySelector('#edit-task-completed').checked
      }
    });
    elements.taskDialog.close();
    state.editingTask = null;
    await loadTasks();
    showToast('任务已更新');
  } catch (error) { showToast(error.message, true); }
});

elements.commentEditForm.addEventListener('submit', async event => {
  event.preventDefault();
  if (!state.editingComment) return;
  const content = document.querySelector('#edit-comment-content').value.trim();
  try {
    const updated = await api(`/api/v1/comments/${state.editingComment.id}`, {
      method: 'PUT', body: { content }
    });
    const taskId = String(updated.taskId);
    const comments = state.comments.get(taskId) || [];
    state.comments.set(taskId, comments.map(comment => comment.id === updated.id ? updated : comment));
    elements.commentDialog.close();
    state.editingComment = null;
    renderTasks();
    showToast('评论已更新');
  } catch (error) { showToast(error.message, true); }
});

elements.projectEditForm.addEventListener('submit', async event => {
  event.preventDefault();
  if (!state.editingProject) return;
  const form = new FormData(elements.projectEditForm);
  try {
    await api(`/api/v1/projects/${state.editingProject.id}`, {
      method: 'PUT', body: { name: form.get('name').trim(), description: form.get('description') || null }
    });
    elements.projectDialog.close();
    state.editingProject = null;
    await loadProjects();
    await loadTasks();
    showToast('项目已更新');
  } catch (error) { showToast(error.message, true); }
});

document.querySelectorAll('[data-close-dialog]').forEach(button => button.addEventListener('click', () => {
  button.closest('dialog').close();
}));

refresh();
