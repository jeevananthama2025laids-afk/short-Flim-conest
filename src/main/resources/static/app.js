const state = {
  entries: [],
  judges: [],
  leaderboard: [],
  scores: new Map(),
  activeView: 'overview'
};

const viewCopy = {
  overview: ['Overview', 'CONTEST CONTROL ROOM', 'A clear view of the films, jury, and scores.'],
  entries: ['Film entries', 'SUBMISSIONS', 'Review submissions and add films to the contest.'],
  jury: ['Jury desk', 'JURY & SCORING', 'Manage judges, assignments, and scorecards.'],
  leaderboard: ['Leaderboard', 'CONTEST STANDINGS', 'Ranked by the average of submitted judge scorecards.']
};

const byId = (id) => document.getElementById(id);
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (character) => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[character]);
const averageText = (score) => score == null ? '<span class="score-dash">No score</span>' : Number(score).toFixed(1);

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers }
  });
  if (response.status === 204) return null;
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.message || `Request failed (${response.status})`);
  return body;
}

function showNotice(message, isError = false) {
  const notice = byId('notice');
  notice.textContent = message;
  notice.classList.toggle('error', isError);
  notice.hidden = false;
  window.clearTimeout(showNotice.timeout);
  showNotice.timeout = window.setTimeout(() => { notice.hidden = true; }, 5200);
}

function setConnection(isOnline) {
  byId('connectionDot').classList.toggle('online', isOnline);
  byId('connectionDot').classList.toggle('offline', !isOnline);
  byId('connectionLabel').textContent = isOnline ? 'API connected' : 'API unavailable';
}

function setView(view) {
  if (!viewCopy[view]) return;
  state.activeView = view;
  document.querySelectorAll('.view-panel').forEach((panel) => {
    panel.hidden = panel.id !== `view-${view}`;
  });
  document.querySelectorAll('.nav-link').forEach((button) => {
    const active = button.dataset.view === view;
    button.classList.toggle('active', active);
    button.setAttribute('aria-current', active ? 'page' : 'false');
  });
  const [title, eyebrow, subtitle] = viewCopy[view];
  byId('pageTitle').textContent = title;
  byId('pageEyebrow').textContent = eyebrow;
  byId('pageSubtitle').textContent = subtitle;
  byId('currentSection').textContent = title.toUpperCase();
  document.title = `FilmJury | ${title}`;
}

function renderLeaderboard(rows, targetId, emptyId, limit = rows.length) {
  const target = byId(targetId);
  const visibleRows = rows.slice(0, limit);
  target.innerHTML = visibleRows.map((row) => `
    <tr>
      <td class="rank-cell"><span class="${row.rank === 1 ? 'rank-leading' : ''}">${row.rank}</span></td>
      <td class="film-title-cell">${escapeHtml(row.title)}</td>
      <td>${escapeHtml(row.genre)}</td>
      <td class="score-cell">${averageText(row.averageScore)}</td>
    </tr>`).join('');
  byId(emptyId).hidden = rows.length > 0;
}

function renderEntries() {
  const search = byId('entrySearch').value.trim().toLowerCase();
  const filtered = state.entries.filter((entry) =>
    `${entry.title} ${entry.genre}`.toLowerCase().includes(search));

  byId('entriesTable').innerHTML = filtered.map((entry) => `
    <tr>
      <td class="film-title-cell">${escapeHtml(entry.title)}</td>
      <td>${escapeHtml(entry.genre)}</td>
      <td class="score-cell">${averageText(state.scores.get(entry.id))}</td>
      <td>
        <button class="table-action" type="button" data-score-entry="${entry.id}" aria-label="Score ${escapeHtml(entry.title)}">Score</button>
        <button class="table-action delete-action" type="button" data-delete-entry="${entry.id}" aria-label="Delete ${escapeHtml(entry.title)}">Delete</button>
      </td>
    </tr>`).join('');
  byId('entriesEmpty').hidden = filtered.length > 0;

  byId('recentEntries').innerHTML = [...state.entries].slice(-4).reverse().map((entry) => `
    <div class="recent-item">
      <span class="poster-mark" aria-hidden="true">${escapeHtml(entry.title.trim().charAt(0).toUpperCase() || 'F')}</span>
      <div class="recent-copy"><strong>${escapeHtml(entry.title)}</strong><span>${escapeHtml(entry.genre)}</span></div>
      <span class="recent-score">${averageText(state.scores.get(entry.id))}</span>
    </div>`).join('');
  byId('recentEntriesEmpty').hidden = state.entries.length > 0;
}

function setSelectOptions(selectId, rows, label) {
  const select = byId(selectId);
  const selected = select.value;
  select.innerHTML = `<option value="">Choose ${label}</option>` + rows.map((row) =>
    `<option value="${row.id}">${escapeHtml(row.title ?? row.name)}</option>`).join('');
  if (rows.some((row) => String(row.id) === selected)) select.value = selected;
}

function renderJudges() {
  byId('judgesList').innerHTML = state.judges.map((judge) => {
    const initials = judge.name.trim().split(/\s+/).slice(0, 2).map((part) => part.charAt(0)).join('').toUpperCase();
    return `<div class="judge-row">
      <span class="judge-avatar" aria-hidden="true">${escapeHtml(initials)}</span>
      <div class="judge-details"><strong>${escapeHtml(judge.name)}</strong><span>${escapeHtml(judge.email)}</span></div>
    </div>`;
  }).join('');
  byId('judgesEmpty').hidden = state.judges.length > 0;
  byId('juryCountLabel').textContent = `${state.judges.length} ${state.judges.length === 1 ? 'judge' : 'judges'}`;
}

async function loadDashboard() {
  try {
    const [entries, judges, leaderboard] = await Promise.all([
      api('/api/entries'), api('/api/judges'), api('/api/leaderboard')
    ]);
    state.entries = entries;
    state.judges = judges;
    state.leaderboard = leaderboard;
    state.scores = new Map(leaderboard.map((row) => [row.entryId, row.averageScore]));

    setConnection(true);
    byId('entryCount').textContent = entries.length;
    byId('judgeCount').textContent = judges.length;
    byId('scoredCount').textContent = leaderboard.length;
    byId('topScore').textContent = leaderboard.length ? Number(leaderboard[0].averageScore).toFixed(1) : '--';
    byId('topFilm').textContent = leaderboard.length ? leaderboard[0].title : 'No scores yet';
    byId('leaderboardCount').textContent = `${leaderboard.length} scored ${leaderboard.length === 1 ? 'film' : 'films'}`;

    renderLeaderboard(leaderboard, 'overviewLeaderboard', 'overviewLeaderboardEmpty', 5);
    renderLeaderboard(leaderboard, 'leaderboardTable', 'leaderboardEmpty');
    renderEntries();
    renderJudges();
    setSelectOptions('scoreEntry', entries, 'a film');
    setSelectOptions('assignEntry', entries, 'a film');
    setSelectOptions('scoreJudge', judges, 'a judge');
    setSelectOptions('assignJudge', judges, 'a judge');
  } catch (error) {
    setConnection(false);
    showNotice(`${error.message}. Start Spring Boot and confirm the MySQL password and database.`, true);
  }
}

document.querySelectorAll('[data-view]').forEach((button) => {
  button.addEventListener('click', () => setView(button.dataset.view));
});

document.querySelectorAll('[data-go]').forEach((button) => {
  button.addEventListener('click', () => setView(button.dataset.go));
});

byId('refreshButton').addEventListener('click', loadDashboard);
byId('entrySearch').addEventListener('input', renderEntries);

byId('entryForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = Object.fromEntries(new FormData(form));
  try {
    await api('/api/entries', { method: 'POST', body: JSON.stringify(values) });
    form.reset();
    await loadDashboard();
    showNotice('Film entry added.');
  } catch (error) {
    showNotice(error.message, true);
  }
});

byId('judgeForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = Object.fromEntries(new FormData(form));
  try {
    await api('/api/judges', { method: 'POST', body: JSON.stringify(values) });
    form.reset();
    await loadDashboard();
    showNotice('Judge added to the panel.');
  } catch (error) {
    showNotice(error.message, true);
  }
});

byId('assignmentForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.currentTarget));
  try {
    const result = await api(`/api/entries/${values.entryId}/judges/${values.judgeId}`, { method: 'POST' });
    showNotice(result.message || 'Judge assigned to entry.');
  } catch (error) {
    showNotice(error.message, true);
  }
});

byId('scoreForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.currentTarget));
  const scores = Object.fromEntries(['Story', 'Direction', 'Acting', 'Cinematography']
    .map((criterion) => [criterion, Number(values[criterion])]));
  try {
    const result = await api(`/api/entries/${values.entryId}/judges/${values.judgeId}/score`, {
      method: 'POST', body: JSON.stringify({ scores })
    });
    showNotice(`Scorecard submitted. Current film average: ${Number(result.averageScore).toFixed(1)}.`);
    await loadDashboard();
  } catch (error) {
    showNotice(error.message, true);
  }
});

byId('entriesTable').addEventListener('click', async (event) => {
  const scoreButton = event.target.closest('[data-score-entry]');
  if (scoreButton) {
    setView('jury');
    byId('scoreEntry').value = scoreButton.dataset.scoreEntry;
    byId('scoreEntry').focus();
    return;
  }

  const deleteButton = event.target.closest('[data-delete-entry]');
  if (!deleteButton) return;
  const entry = state.entries.find((row) => String(row.id) === deleteButton.dataset.deleteEntry);
  if (!entry || !window.confirm(`Delete "${entry.title}" and its scorecards?`)) return;
  try {
    await api(`/api/entries/${entry.id}`, { method: 'DELETE' });
    await loadDashboard();
    showNotice('Film entry deleted.');
  } catch (error) {
    showNotice(error.message, true);
  }
});

setView('overview');
loadDashboard();