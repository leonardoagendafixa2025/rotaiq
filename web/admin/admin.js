/**
 * ROTA IQ ADMIN — CLIENTE WEB ADMINISTRATIVO & BACKOFFICE REAL
 * Comunicação direta com a API FastAPI + Banco de Dados PostgreSQL (Supabase).
 * REGRA ABSOLUTA: ZERO MOCKS — Persistência real em todas as mutações.
 */

const API_BASE_URL = 'http://localhost:8000/api/v1';
const HEALTH_URL = 'http://localhost:8000/health';

// Estado global da sessão administrativa
let currentSession = {
    token: null,
    email: null,
    role: 'ADMIN'
};

// Estado da paginação de motoristas
let driverPagination = {
    currentPage: 1,
    totalPages: 1,
    searchDebounceTimer: null
};

// ==========================================================================
// 1. INICIALIZAÇÃO & VERIFICAÇÃO DE SAÚDE DO POSTGRESQL
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    checkDatabaseHealth();
    // Poll de saúde do banco a cada 15 segundos
    setInterval(checkDatabaseHealth, 15000);

    const savedSession = sessionStorage.getItem('rota_iq_admin_session');
    if (savedSession) {
        try {
            currentSession = JSON.parse(savedSession);
            showAdminDashboard();
        } catch (e) {
            console.warn('Sessão administrativa expirada ou corrompida:', e);
            sessionStorage.removeItem('rota_iq_admin_session');
        }
    }
});

async function checkDatabaseHealth() {
    const badge = document.getElementById('conn-status-badge');
    const dot = document.getElementById('conn-status-dot');
    const text = document.getElementById('conn-status-text');

    try {
        const response = await fetch(HEALTH_URL);
        if (response.ok) {
            const data = await response.json();
            if (data.database === 'connected') {
                if (dot) dot.style.background = '#00E676';
                if (text) text.innerText = `POSTGRESQL CONECTADO (${data.latency_ms || 0}ms)`;
                
                const infraLatency = document.getElementById('infra-latency');
                if (infraLatency) infraLatency.innerText = `${data.latency_ms || 0} ms`;
            } else {
                if (dot) dot.style.background = '#FF334B';
                if (text) text.innerText = 'POSTGRESQL OFFLINE / INDISPONÍVEL';
            }
        } else {
            if (dot) dot.style.background = '#FF334B';
            if (text) text.innerText = 'BANCO INDISPONÍVEL (HTTP ' + response.status + ')';
        }
    } catch (err) {
        if (dot) dot.style.background = '#FF334B';
        if (text) text.innerText = 'BACKEND OFFLINE / REINICIALIZANDO';
    }
}

function getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (currentSession.token) {
        headers['Authorization'] = `Bearer ${currentSession.token}`;
    }
    return headers;
}

// ==========================================================================
// 2. AUTENTICAÇÃO REAL (PBKDF2 + JWT) — ZERO MOCKS
// ==========================================================================

async function handleAdminLogin(event) {
    event.preventDefault();
    const email = document.getElementById('admin-email').value.trim();
    const password = document.getElementById('admin-password').value;
    const role = document.getElementById('admin-role').value;
    const errorMsg = document.getElementById('login-error-msg');
    const submitBtn = document.getElementById('btn-login-submit');

    errorMsg.style.display = 'none';
    submitBtn.innerText = 'AUTENTICANDO NO SERVIDOR...';
    submitBtn.disabled = true;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, role })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Credenciais de administrador incorretas ou acesso não autorizado.');
        }

        const data = await response.json();
        currentSession = {
            token: data.access_token,
            email: data.admin.email,
            role: data.admin.role
        };

        sessionStorage.setItem('rota_iq_admin_session', JSON.stringify(currentSession));
        showToast('Login efetuado com sucesso! Bem-vindo ao Backoffice.', 'success');
        showAdminDashboard();
    } catch (err) {
        errorMsg.innerText = err.message || 'Falha ao autenticar. Verifique sua senha e conexão.';
        errorMsg.style.display = 'block';
    } finally {
        submitBtn.innerText = 'ENTRAR NO PAINEL';
        submitBtn.disabled = false;
    }
}

function showAdminDashboard() {
    document.getElementById('admin-login-modal').style.display = 'none';
    document.getElementById('admin-app').style.display = 'block';

    document.getElementById('current-user-email').innerText = currentSession.email;
    document.getElementById('current-role-badge').innerText = currentSession.role;

    // Carrega dados iniciais da visão geral
    fetchAdminDashboard();
}

function handleAdminLogout() {
    sessionStorage.removeItem('rota_iq_admin_session');
    currentSession = { token: null, email: null, role: 'ADMIN' };
    document.getElementById('admin-app').style.display = 'none';
    document.getElementById('admin-login-modal').style.display = 'flex';
}

// ==========================================================================
// 3. NAVEGAÇÃO ENTRE ABAS
// ==========================================================================

function switchTab(tabName) {
    const tabs = document.querySelectorAll('.nav-tab');
    tabs.forEach(t => t.classList.remove('active'));

    const panels = document.querySelectorAll('.tab-panel');
    panels.forEach(p => p.classList.remove('active'));

    const activeBtn = Array.from(tabs).find(t => t.getAttribute('onclick')?.includes(tabName));
    if (activeBtn) activeBtn.classList.add('active');

    const activePanel = document.getElementById(`tab-${tabName}`);
    if (activePanel) activePanel.classList.add('active');

    if (tabName === 'overview') {
        fetchAdminDashboard();
    } else if (tabName === 'drivers') {
        loadDriversTable(1);
    } else if (tabName === 'plans') {
        loadPlansCatalog();
    } else if (tabName === 'subs-payments') {
        loadSubscriptionsAndPayments();
    } else if (tabName === 'flags') {
        loadFeatureFlags();
    } else if (tabName === 'settings') {
        loadSettings();
    } else if (tabName === 'audit') {
        loadAuditLogs();
    }
}

// ==========================================================================
// 4. VISÃO GERAL (DASHBOARD REAL)
// ==========================================================================

async function fetchAdminDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/dashboard`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            const kpis = data.kpis || {};

            document.getElementById('kpi-mrr').innerText = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(kpis.mrr_reais || 0);

            document.getElementById('kpi-arr').innerText = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(kpis.arr_reais || 0);

            document.getElementById('kpi-users').innerText = kpis.total_users || 0;
            document.getElementById('kpi-drivers').innerText = kpis.total_drivers || 0;
            document.getElementById('kpi-active-drivers').innerText = kpis.active_drivers || 0;
            document.getElementById('kpi-blocked-drivers').innerText = kpis.blocked_drivers || 0;
            document.getElementById('kpi-subs').innerText = kpis.active_pro_subscribers || 0;
            document.getElementById('kpi-evals').innerText = kpis.total_evaluations_recorded || 0;

            document.getElementById('infra-snapshot-date').innerText = data.snapshot_date || new Date().toLocaleString('pt-BR');
        } else if (response.status === 401 || response.status === 403) {
            handleAdminLogout();
        }
    } catch (err) {
        console.warn('Erro ao carregar dashboard:', err);
    }
}

// ==========================================================================
// 5. MOTORISTAS — TABELA, BUSCA, PAGINAÇÃO, MODAL, BLOQUEIO
// ==========================================================================

function debounceDriverSearch() {
    clearTimeout(driverPagination.searchDebounceTimer);
    driverPagination.searchDebounceTimer = setTimeout(() => {
        loadDriversTable(1);
    }, 350);
}

async function loadDriversTable(page = 1) {
    const tbody = document.getElementById('drivers-table-body');
    tbody.innerHTML = '<tr><td colspan="7" class="table-loading">Consultando motoristas no PostgreSQL...</td></tr>';

    const search = document.getElementById('driver-search-input')?.value.trim() || '';
    const status = document.getElementById('driver-status-filter')?.value || 'ALL';

    try {
        const url = `${API_BASE_URL}/admin/drivers?paged=true&page=${page}&limit=10&search=${encodeURIComponent(search)}&status=${status}`;
        const response = await fetch(url, { headers: getAuthHeaders() });

        if (response.ok) {
            const data = await response.json();
            const items = data.items || [];
            driverPagination.currentPage = data.page || 1;
            driverPagination.totalPages = data.total_pages || 1;

            document.getElementById('pagination-current-count').innerText = items.length;
            document.getElementById('pagination-total-count').innerText = data.total || 0;
            document.getElementById('pagination-page-label').innerText = `Página ${data.page} de ${data.total_pages}`;

            document.getElementById('btn-page-prev').disabled = data.page <= 1;
            document.getElementById('btn-page-next').disabled = data.page >= data.total_pages;

            if (items.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 30px; color: #888;">Nenhum motorista encontrado com os filtros informados.</td></tr>';
                return;
            }

            tbody.innerHTML = '';
            items.forEach(d => {
                const tr = document.createElement('tr');
                const createdAt = d.created_at ? new Date(d.created_at).toLocaleDateString('pt-BR') : '—';
                const statusClass = d.status === 'BLOCKED' ? 'chip-blocked' : (d.status === 'ACTIVE' ? 'chip-active' : 'chip-inactive');
                const planClass = d.plan_code?.includes('pro') ? 'chip-pro' : 'chip-free';

                const blockBtnHtml = d.status === 'BLOCKED'
                    ? `<button class="btn-action btn-action-primary" onclick="handleUnblockDriver('${d.id}')">Desbloquear</button>`
                    : `<button class="btn-action btn-action-danger" onclick="openBlockDriverModal('${d.id}')">Bloquear</button>`;

                tr.innerHTML = `
                    <td>
                        <strong>${escapeHtml(d.name)}</strong>
                        <div style="font-size: 0.72rem; color: var(--text-muted); font-family: monospace;">${escapeHtml(d.id.substring(0, 13))}...</div>
                    </td>
                    <td>
                        <div>${escapeHtml(d.email)}</div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(d.phone || '—')}</div>
                    </td>
                    <td>${escapeHtml(d.city)} / ${escapeHtml(d.state)}</td>
                    <td><span class="chip ${statusClass}">${d.status}</span></td>
                    <td><span class="chip ${planClass}">${(d.plan_code || 'free').toUpperCase()}</span></td>
                    <td>${createdAt}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn-action" onclick="openDriverDetails('${d.id}')">Detalhes</button>
                            <button class="btn-action" onclick="openEditDriverModal('${d.id}')">Editar</button>
                            ${blockBtnHtml}
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: #FF334B; padding: 20px;">Falha ao obter motoristas (HTTP ${response.status})</td></tr>`;
        }
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color: #FF334B; padding: 20px;">Erro de conexão ao carregar motoristas.</td></tr>';
    }
}

function changeDriverPage(delta) {
    const newPage = driverPagination.currentPage + delta;
    if (newPage >= 1 && newPage <= driverPagination.totalPages) {
        loadDriversTable(newPage);
    }
}

// Modal Criar/Editar Motorista
function openDriverModal() {
    document.getElementById('modal-driver-title').innerHTML = 'Cadastrar <span>Novo Motorista</span>';
    document.getElementById('driver-edit-id').value = '';
    document.getElementById('form-driver').reset();
    openModal('modal-driver');
}

async function openEditDriverModal(driverId) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers/${driverId}`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Não foi possível carregar os dados.');
        const data = await response.json();
        const d = data.driver || {};
        const u = data.user || {};

        document.getElementById('modal-driver-title').innerHTML = 'Editar <span>Motorista</span>';
        document.getElementById('driver-edit-id').value = driverId;
        document.getElementById('driver-name').value = u.full_name || '';
        document.getElementById('driver-email').value = u.email || '';
        document.getElementById('driver-phone').value = u.phone || d.phone || '';
        document.getElementById('driver-city').value = d.city || 'São Paulo';
        document.getElementById('driver-state').value = d.state || 'SP';
        document.getElementById('driver-plan').value = d.plan_code || 'free';

        openModal('modal-driver');
    } catch (err) {
        showToast('Erro ao carregar dados do motorista: ' + err.message, 'error');
    }
}

async function handleSaveDriver(event) {
    event.preventDefault();
    const id = document.getElementById('driver-edit-id').value;
    const name = document.getElementById('driver-name').value.trim();
    const email = document.getElementById('driver-email').value.trim();
    const phone = document.getElementById('driver-phone').value.trim();
    const city = document.getElementById('driver-city').value.trim();
    const state = document.getElementById('driver-state').value.trim().toUpperCase();
    const plan_code = document.getElementById('driver-plan').value;

    const submitBtn = document.getElementById('btn-save-driver');
    submitBtn.innerText = 'Salvando no PostgreSQL...';
    submitBtn.disabled = true;

    try {
        let response;
        if (id) {
            // Edição
            response = await fetch(`${API_BASE_URL}/admin/drivers/${id}`, {
                method: 'PATCH',
                headers: getAuthHeaders(),
                body: JSON.stringify({ name, email, phone, city, state, plan_code })
            });
        } else {
            // Criação
            response = await fetch(`${API_BASE_URL}/admin/drivers`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ name, email, phone, city, state, plan_code, status: 'ACTIVE' })
            });
        }

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao salvar motorista.');
        }

        closeModal('modal-driver');
        showToast('Motorista salvo com sucesso no PostgreSQL!', 'success');
        loadDriversTable(driverPagination.currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.innerText = 'Salvar no PostgreSQL';
        submitBtn.disabled = false;
    }
}

// Modal Bloqueio
function openBlockDriverModal(driverId) {
    document.getElementById('block-driver-id').value = driverId;
    document.getElementById('block-reason').value = '';
    openModal('modal-block-driver');
}

async function confirmBlockDriver() {
    const id = document.getElementById('block-driver-id').value;
    const reason = document.getElementById('block-reason').value.trim();

    if (!reason) {
        alert('Por favor, informe o motivo formal do bloqueio para a auditoria.');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers/${id}/block`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ reason })
        });

        if (!response.ok) throw new Error('Falha ao bloquear motorista.');

        closeModal('modal-block-driver');
        showToast('Motorista bloqueado com sucesso no sistema.', 'success');
        loadDriversTable(driverPagination.currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleUnblockDriver(driverId) {
    if (!confirm('Deseja realmente reativar e desbloquear este motorista?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers/${driverId}/unblock`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (!response.ok) throw new Error('Falha ao desbloquear motorista.');

        showToast('Motorista reativado com sucesso!', 'success');
        loadDriversTable(driverPagination.currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Detalhes do Motorista
async function openDriverDetails(driverId) {
    const container = document.getElementById('driver-details-content');
    container.innerHTML = 'Carregando dados completos do banco...';
    openModal('modal-driver-details');

    try {
        const response = await fetch(`${API_BASE_URL}/admin/drivers/${driverId}`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Não foi possível obter dados.');
        const data = await response.json();
        const d = data.driver || {};
        const u = data.user || {};
        const sub = data.subscription;

        container.innerHTML = `
            <div style="background: rgba(255,255,255,0.03); padding: 16px; border-radius: 8px; margin-bottom: 16px;">
                <h4 style="color: #fff; margin-bottom: 8px;">Dados do Usuário</h4>
                <div><strong>Nome:</strong> ${escapeHtml(u.full_name || '—')}</div>
                <div><strong>E-mail:</strong> ${escapeHtml(u.email || '—')}</div>
                <div><strong>Telefone:</strong> ${escapeHtml(u.phone || d.phone || '—')}</div>
                <div><strong>Data de Cadastro:</strong> ${u.created_at ? new Date(u.created_at).toLocaleString('pt-BR') : '—'}</div>
            </div>

            <div style="background: rgba(255,255,255,0.03); padding: 16px; border-radius: 8px; margin-bottom: 16px;">
                <h4 style="color: #fff; margin-bottom: 8px;">Dados Operacionais</h4>
                <div><strong>ID Motorista:</strong> <code>${escapeHtml(d.id)}</code></div>
                <div><strong>Praça / Região:</strong> ${escapeHtml(d.city || '—')} / ${escapeHtml(d.state || '—')}</div>
                <div><strong>Status Atual:</strong> <span class="chip ${d.status === 'BLOCKED' ? 'chip-blocked' : 'chip-active'}">${d.status}</span></div>
                ${d.block_reason ? `<div style="color: #FF334B; margin-top: 6px;"><strong>Motivo do Bloqueio:</strong> ${escapeHtml(d.block_reason)}</div>` : ''}
            </div>

            <div style="background: rgba(255,255,255,0.03); padding: 16px; border-radius: 8px;">
                <h4 style="color: #fff; margin-bottom: 8px;">Situação de Assinatura</h4>
                ${sub ? `
                    <div><strong>Plano:</strong> <span class="chip chip-pro">${sub.tier || 'PRO'}</span></div>
                    <div><strong>Provedor:</strong> ${sub.provider || 'PIX'}</div>
                    <div><strong>Expiração:</strong> ${sub.expires_at ? new Date(sub.expires_at).toLocaleDateString('pt-BR') : '—'}</div>
                ` : `<div>Motorista utiliza o <strong>Plano Gratuito (Free Tier)</strong>.</div>`}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div style="color: #FF334B;">Erro ao carregar detalhes: ${err.message}</div>`;
    }
}

// ==========================================================================
// 6. PLANOS & PREÇOS — CRUD COMPLETO NO POSTGRESQL
// ==========================================================================

async function loadPlansCatalog() {
    const container = document.getElementById('plans-container');
    container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 30px; color: #888;">Consultando planos no PostgreSQL...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/plans`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao consultar planos.');

        const plans = await response.json();
        if (plans.length === 0) {
            container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #888;">Nenhum plano cadastrado. Clique em "+ Novo Plano" para criar o primeiro.</div>';
            return;
        }

        container.innerHTML = '';
        plans.forEach(p => {
            const priceReais = (p.price_cents / 100.0).toFixed(2).replace('.', ',');
            const intervalLabel = p.interval === 'month' ? '/mês' : (p.interval === 'year' ? '/ano' : '');
            const features = Array.isArray(p.features) ? p.features : [];

            const card = document.createElement('div');
            card.className = `plan-card ${p.code === 'pro_monthly' ? 'highlight' : ''}`;
            card.innerHTML = `
                <div class="plan-badge">${p.is_active ? 'ATIVO' : 'INATIVO'}</div>
                <h3>${escapeHtml(p.name)}</h3>
                <div class="plan-price">R$ ${priceReais}<span>${intervalLabel}</span></div>
                <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 14px;">${escapeHtml(p.description || '')}</p>
                <ul class="plan-features">
                    ${features.map(f => `<li>✓ ${escapeHtml(f)}</li>`).join('')}
                </ul>
                <div style="margin-top: 24px; display: flex; gap: 8px; flex-wrap: wrap;">
                    <button class="btn-secondary" style="flex: 1;" onclick="openEditPlanModal('${p.code}')">✏️ Editar Preço</button>
                    <button class="btn-secondary" onclick="togglePlanActive('${p.code}', ${!p.is_active})">${p.is_active ? 'Desativar' : 'Ativar'}</button>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #FF334B; padding: 30px;">Erro ao carregar catálogo de planos: ${err.message}</div>`;
    }
}

function openPlanModal() {
    document.getElementById('modal-plan-title').innerHTML = 'Cadastrar <span>Novo Plano</span>';
    document.getElementById('plan-is-edit').value = 'false';
    document.getElementById('plan-code').disabled = false;
    document.getElementById('form-plan').reset();
    openModal('modal-plan');
}

async function openEditPlanModal(planCode) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/plans`, { headers: getAuthHeaders() });
        const plans = await response.json();
        const p = plans.find(x => x.code === planCode);
        if (!p) throw new Error('Plano não localizado.');

        document.getElementById('modal-plan-title').innerHTML = 'Editar <span>Plano Comercial</span>';
        document.getElementById('plan-is-edit').value = 'true';
        document.getElementById('plan-code').value = p.code;
        document.getElementById('plan-code').disabled = true;
        document.getElementById('plan-name').value = p.name;
        document.getElementById('plan-price').value = (p.price_cents / 100.0).toFixed(2);
        document.getElementById('plan-interval').value = p.interval || 'month';
        document.getElementById('plan-desc').value = p.description || '';
        document.getElementById('plan-features').value = Array.isArray(p.features) ? p.features.join(', ') : '';

        openModal('modal-plan');
    } catch (err) {
        showToast('Erro ao abrir plano: ' + err.message, 'error');
    }
}

async function handleSavePlan(event) {
    event.preventDefault();
    const isEdit = document.getElementById('plan-is-edit').value === 'true';
    const code = document.getElementById('plan-code').value.trim();
    const name = document.getElementById('plan-name').value.trim();
    const price = parseFloat(document.getElementById('plan-price').value);
    const interval = document.getElementById('plan-interval').value;
    const description = document.getElementById('plan-desc').value.trim();
    const featuresRaw = document.getElementById('plan-features').value;
    const features = featuresRaw.split(',').map(f => f.trim()).filter(Boolean);

    const price_cents = Math.round(price * 100);

    const submitBtn = document.getElementById('btn-save-plan');
    submitBtn.innerText = 'Salvando no banco...';
    submitBtn.disabled = true;

    try {
        let response;
        if (isEdit) {
            response = await fetch(`${API_BASE_URL}/admin/plans/${code}`, {
                method: 'PATCH',
                headers: getAuthHeaders(),
                body: JSON.stringify({ name, price_cents, interval, description, features })
            });
        } else {
            response = await fetch(`${API_BASE_URL}/admin/plans`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ code, name, price_cents, interval, description, features, is_active: true })
            });
        }

        if (!response.ok) throw new Error('Falha ao salvar plano.');

        closeModal('modal-plan');
        showToast('Plano persistido com sucesso no PostgreSQL! O novo preço já está disponível na API.', 'success');
        loadPlansCatalog();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.innerText = 'Salvar Plano';
        submitBtn.disabled = false;
    }
}

async function togglePlanActive(planCode, nextActive) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/plans/${planCode}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ is_active: nextActive })
        });
        if (!response.ok) throw new Error('Falha ao alterar status.');
        showToast(`Plano ${nextActive ? 'ativado' : 'desativado'} com sucesso!`, 'success');
        loadPlansCatalog();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================================================
// 7. ASSINATURAS & PAGAMENTOS PIX
// ==========================================================================

async function loadSubscriptionsAndPayments() {
    const subsTbody = document.getElementById('subscriptions-table-body');
    const payTbody = document.getElementById('payments-table-body');

    subsTbody.innerHTML = '<tr><td colspan="6" class="table-loading">Carregando assinaturas...</td></tr>';
    payTbody.innerHTML = '<tr><td colspan="5" class="table-loading">Carregando transações Pix...</td></tr>';

    try {
        const [subsRes, payRes] = await Promise.all([
            fetch(`${API_BASE_URL}/admin/subscriptions`, { headers: getAuthHeaders() }),
            fetch(`${API_BASE_URL}/admin/payments`, { headers: getAuthHeaders() })
        ]);

        if (subsRes.ok) {
            const subs = await subsRes.json();
            if (subs.length === 0) {
                subsTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 20px; color: #888;">Nenhuma assinatura registrada no banco ainda.</td></tr>';
            } else {
                subsTbody.innerHTML = '';
                subs.forEach(s => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><code>${escapeHtml(s.id?.substring(0, 8))}...</code></td>
                        <td><code>${escapeHtml(s.driver_id?.substring(0, 8))}...</code></td>
                        <td><span class="chip chip-pro">${escapeHtml(s.plan_code || s.tier || 'PRO')}</span></td>
                        <td><span class="chip ${s.status === 'ACTIVE' ? 'chip-active' : 'chip-inactive'}">${escapeHtml(s.status)}</span></td>
                        <td>${escapeHtml(s.provider || 'PIX')}</td>
                        <td>${s.expires_at ? new Date(s.expires_at).toLocaleDateString('pt-BR') : '—'}</td>
                    `;
                    subsTbody.appendChild(tr);
                });
            }
        }

        if (payRes.ok) {
            const payments = await payRes.json();
            if (payments.length === 0) {
                payTbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px; color: #888;">Nenhuma transação Pix registrada ainda.</td></tr>';
            } else {
                payTbody.innerHTML = '';
                payments.forEach(p => {
                    const tr = document.createElement('tr');
                    const amount = (p.amount_cents / 100.0).toFixed(2).replace('.', ',');
                    tr.innerHTML = `
                        <td><code>${escapeHtml(p.order_id || p.tx_id || p.id?.substring(0, 8))}</code></td>
                        <td><strong>R$ ${amount}</strong></td>
                        <td>${escapeHtml(p.plan_code || '—')}</td>
                        <td><span class="chip ${p.status === 'PAID' ? 'chip-active' : 'chip-inactive'}">${escapeHtml(p.status)}</span></td>
                        <td>${p.created_at ? new Date(p.created_at).toLocaleString('pt-BR') : '—'}</td>
                    `;
                    payTbody.appendChild(tr);
                });
            }
        }
    } catch (err) {
        console.warn('Erro ao carregar assinaturas/pagamentos:', err);
    }
}

// ==========================================================================
// 8. FEATURE FLAGS CRUD
// ==========================================================================

async function loadFeatureFlags() {
    const container = document.getElementById('flags-container');
    container.innerHTML = '<div style="text-align:center; padding: 30px; color: #888;">Consultando Feature Flags...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/feature-flags`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao obter flags.');

        const flags = await response.json();
        if (flags.length === 0) {
            container.innerHTML = '<div style="text-align:center; padding: 30px; color: #888;">Nenhuma feature flag cadastrada.</div>';
            return;
        }

        container.innerHTML = '';
        flags.forEach(f => {
            const row = document.createElement('div');
            row.className = 'flag-row';
            row.innerHTML = `
                <div>
                    <strong>${escapeHtml(f.name || f.key)} <code style="font-size: 0.75rem; color: var(--orange-primary); font-weight: normal;">${escapeHtml(f.key)}</code></strong>
                    <p>${escapeHtml(f.description || '')}</p>
                </div>
                <label class="switch">
                    <input type="checkbox" ${f.is_enabled ? 'checked' : ''} onchange="toggleFlag('${f.key}', this.checked)">
                    <span class="slider"></span>
                </label>
            `;
            container.appendChild(row);
        });
    } catch (err) {
        container.innerHTML = `<div style="text-align:center; color: #FF334B; padding: 20px;">Erro ao carregar feature flags: ${err.message}</div>`;
    }
}

async function toggleFlag(flagKey, isEnabled) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/feature-flags/${flagKey}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ is_enabled: isEnabled })
        });
        if (!response.ok) throw new Error('Falha ao alternar feature flag.');
        showToast(`Feature Flag '${flagKey}' atualizada para ${isEnabled ? 'ATIVA' : 'INATIVA'}!`, 'success');
    } catch (err) {
        showToast(err.message, 'error');
        loadFeatureFlags();
    }
}

function openFlagModal() {
    document.getElementById('form-flag').reset();
    openModal('modal-flag');
}

async function handleSaveFlag(event) {
    event.preventDefault();
    const key = document.getElementById('flag-key').value.trim().toUpperCase();
    const name = document.getElementById('flag-name').value.trim();
    const description = document.getElementById('flag-desc').value.trim();

    try {
        const response = await fetch(`${API_BASE_URL}/admin/feature-flags`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ key, name, description, is_enabled: true })
        });
        if (!response.ok) throw new Error('Falha ao cadastrar feature flag.');

        closeModal('modal-flag');
        showToast(`Feature Flag '${key}' cadastrada com sucesso!`, 'success');
        loadFeatureFlags();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================================================
// 9. CONFIGURAÇÕES DO SISTEMA (CRUD REAL)
// ==========================================================================

async function loadSettings() {
    const container = document.getElementById('settings-container');
    container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 30px; color: #888;">Carregando configurações...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/settings`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao obter configurações.');

        const settings = await response.json();
        container.innerHTML = '';
        settings.forEach(s => {
            const card = document.createElement('div');
            card.className = 'setting-card';
            card.innerHTML = `
                <div>
                    <div class="setting-header">
                        <span class="setting-category">${escapeHtml(s.category)}</span>
                        <code style="font-size: 0.72rem; color: var(--text-muted);">${escapeHtml(s.environment)}</code>
                    </div>
                    <div class="setting-title">${escapeHtml(s.key)}</div>
                    <div class="setting-desc">${escapeHtml(s.description || '')}</div>
                </div>
                <div class="setting-input-row">
                    <input type="text" id="setting-input-${s.key}" value="${escapeHtml(s.value)}">
                    <button class="btn-action btn-action-primary" onclick="saveSetting('${s.key}')">Salvar</button>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: #FF334B; padding: 20px;">Erro: ${err.message}</div>`;
    }
}

async function saveSetting(key) {
    const input = document.getElementById(`setting-input-${key}`);
    const value = input ? input.value.trim() : '';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/settings/${key}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ value })
        });
        if (!response.ok) throw new Error('Falha ao atualizar configuração.');
        showToast(`Configuração '${key}' salva no PostgreSQL com sucesso!`, 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================================================
// 10. AUDITORIA & LOGS
// ==========================================================================

async function loadAuditLogs() {
    const tbody = document.getElementById('audit-table-body');
    tbody.innerHTML = '<tr><td colspan="5" class="table-loading">Buscando logs de auditoria...</td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/audit-logs?limit=50`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao carregar auditoria.');

        const logs = await response.json();
        if (logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px; color: #888;">Nenhum log de auditoria registrado ainda.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        logs.forEach(l => {
            const tr = document.createElement('tr');
            const dateStr = l.created_at ? new Date(l.created_at).toLocaleString('pt-BR') : '—';
            const details = l.new_value ? JSON.stringify(l.new_value) : (l.old_value ? JSON.stringify(l.old_value) : '—');

            tr.innerHTML = `
                <td>${dateStr}</td>
                <td><strong>${escapeHtml(l.admin_email)}</strong></td>
                <td><span class="chip chip-active">${escapeHtml(l.action)}</span></td>
                <td><code>${escapeHtml(l.resource)}</code> ${l.resource_id ? `(${escapeHtml(l.resource_id)})` : ''}</td>
                <td style="font-size: 0.78rem; font-family: monospace; max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${escapeHtml(details)}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: #FF334B; padding: 20px;">Erro: ${err.message}</td></tr>`;
    }
}

// ==========================================================================
// 11. UTILITÁRIOS, MODAIS & TOASTS
// ==========================================================================

function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : (type === 'error' ? '⚠️' : 'ℹ️')}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
