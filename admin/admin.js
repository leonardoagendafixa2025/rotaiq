/**
 * ROTA IQ ADMIN — CLIENTE WEB ADMINISTRATIVO & BACKOFFICE REAL
 * Comunicação direta com a API REST oficial.
 * Mutações persistentes em tempo real.
 */

const isLocal = typeof window !== 'undefined' && 
    (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

const API_BASE_URL = isLocal 
    ? `http://${window.location.hostname}:8000/api/v1` 
    : `${window.location.origin}/api/v1`;

const HEALTH_URL = isLocal 
    ? `http://${window.location.hostname}:8000/health` 
    : `${window.location.origin}/health`;

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
// 1. INICIALIZAÇÃO & VERIFICAÇÃO DE STATUS
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    checkDatabaseHealth();
    // Verificação periódica de conectividade a cada 15 segundos
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
    const dot = document.getElementById('conn-status-dot');
    const text = document.getElementById('conn-status-text');

    try {
        const response = await fetch(HEALTH_URL);
        if (response.ok) {
            const data = await response.json();
            if (data.status === 'ok' || data.database === 'connected') {
                if (dot) dot.style.background = '#00E676';
                if (text) text.innerText = 'SISTEMA ONLINE';
            } else {
                if (dot) dot.style.background = '#FFB800';
                if (text) text.innerText = 'ATENÇÃO OPERACIONAL';
            }
        } else {
            if (dot) dot.style.background = '#FF334B';
            if (text) text.innerText = 'INSTABILIDADE';
        }
    } catch (err) {
        if (dot) dot.style.background = '#FF334B';
        if (text) text.innerText = 'OFFLINE';
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
// 2. AUTENTICAÇÃO
// ==========================================================================

async function handleAdminLogin(event) {
    event.preventDefault();
    const email = document.getElementById('admin-email').value.trim();
    const password = document.getElementById('admin-password').value;
    const role = document.getElementById('admin-role').value;
    const errorMsg = document.getElementById('login-error-msg');
    const submitBtn = document.getElementById('btn-login-submit');

    errorMsg.style.display = 'none';
    submitBtn.innerText = 'AUTENTICANDO...';
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

// ==========================================================================
// ESTADO DA SINCRONIA EM TEMPO REAL & TELEMETRIA AO VIVO
// ==========================================================================

let liveSyncState = {
    isEnabled: true,
    intervalMs: 3500,
    intervalId: null,
    previousValues: {},
    activityLog: [],
    activeTab: 'overview'
};

function showAdminDashboard() {
    document.getElementById('admin-login-modal').style.display = 'none';
    document.getElementById('admin-app').style.display = 'block';

    const emailEl = document.getElementById('current-user-email');
    const roleEl = document.getElementById('current-role-badge');
    const drawerEmailEl = document.getElementById('drawer-user-email');
    const drawerRoleEl = document.getElementById('drawer-role-badge');

    if (emailEl) emailEl.innerText = currentSession.email;
    if (roleEl) roleEl.innerText = currentSession.role;
    if (drawerEmailEl) drawerEmailEl.innerText = currentSession.email;
    if (drawerRoleEl) drawerRoleEl.innerText = currentSession.role;

    // Carrega dados iniciais da visão geral e inicia sincronia contínua
    fetchAdminDashboard();
    startLiveSync();
}

function handleAdminLogout() {
    stopLiveSync();
    toggleMobileDrawer(false);
    sessionStorage.removeItem('rota_iq_admin_session');
    currentSession = { token: null, email: null, role: 'ADMIN' };
    document.getElementById('admin-app').style.display = 'none';
    document.getElementById('admin-login-modal').style.display = 'flex';
}

function startLiveSync() {
    if (liveSyncState.intervalId) clearInterval(liveSyncState.intervalId);
    liveSyncState.isEnabled = true;
    updateLiveSyncBadgeUI();
    liveSyncState.intervalId = setInterval(() => {
        if (!liveSyncState.isEnabled || !currentSession.token) return;
        
        // Se estiver na aba de Visão Geral, atualiza métricas e feed
        if (liveSyncState.activeTab === 'overview') {
            fetchAdminDashboard(false);
        } else if (liveSyncState.activeTab === 'campaigns') {
            loadCampaignsDashboard();
        }
    }, liveSyncState.intervalMs);
}

function stopLiveSync() {
    if (liveSyncState.intervalId) {
        clearInterval(liveSyncState.intervalId);
        liveSyncState.intervalId = null;
    }
    liveSyncState.isEnabled = false;
    updateLiveSyncBadgeUI();
}

function toggleLiveSync() {
    if (liveSyncState.isEnabled) {
        stopLiveSync();
        showToast('Sincronia automática pausada.', 'info');
    } else {
        startLiveSync();
        showToast('Sincronia automática em tempo real reativada (3.5s)!', 'success');
        fetchAdminDashboard(true);
    }
}

function updateLiveSyncBadgeUI() {
    const badge = document.getElementById('live-sync-toggle-badge');
    const text = document.getElementById('live-sync-text');
    if (!badge || !text) return;

    if (liveSyncState.isEnabled) {
        badge.classList.remove('paused');
        text.innerText = 'SINCRONIA AO VIVO (3.5s)';
    } else {
        badge.classList.add('paused');
        text.innerText = 'SINCRONIA PAUSADA';
    }
}

function triggerCardGlow(cardId) {
    const el = document.getElementById(cardId);
    if (!el) return;
    el.classList.remove('kpi-updated');
    // Force reflow
    void el.offsetWidth;
    el.classList.add('kpi-updated');
    setTimeout(() => el.classList.remove('kpi-updated'), 900);
}

// ==========================================================================
// 3. NAVEGAÇÃO ENTRE ABAS & MOBILE DRAWER
// ==========================================================================

function toggleMobileDrawer(open) {
    const drawer = document.getElementById('admin-mobile-drawer');
    const backdrop = document.getElementById('drawer-backdrop');
    if (open) {
        drawer?.classList.add('active');
        backdrop?.classList.add('active');
        document.body.style.overflow = 'hidden';
    } else {
        drawer?.classList.remove('active');
        backdrop?.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function switchTab(tabName) {
    liveSyncState.activeTab = tabName;

    // 1. Sincroniza estado ativo em todos os menus (Desktop Sidebar, Drawer Mobile e Bottom Nav)
    document.querySelectorAll('[data-tab]').forEach(el => {
        if (el.getAttribute('data-tab') === tabName) {
            el.classList.add('active');
        } else {
            el.classList.remove('active');
        }
    });

    // 2. Alterna os painéis de abas
    const panels = document.querySelectorAll('.tab-panel');
    panels.forEach(p => p.classList.remove('active'));

    const activePanel = document.getElementById(`tab-${tabName}`);
    if (activePanel) activePanel.classList.add('active');

    // 3. Atualiza texto do breadcrumb
    const breadcrumb = document.getElementById('breadcrumb-current-tab');
    const tabTitles = {
        'overview': 'Visão Geral',
        'drivers': 'Motoristas Cadastrados',
        'campaigns': 'Campanhas & Push Notifications',
        'plans': 'Planos & Preços',
        'subs-payments': 'Assinaturas & Pix',
        'flags': 'Funcionalidades do App',
        'settings': 'Configurações Operacionais'
    };
    if (breadcrumb) {
        breadcrumb.innerText = tabTitles[tabName] || tabName;
    }

    // 4. Fecha drawer mobile automaticamente e restaura scroll
    toggleMobileDrawer(false);

    // 5. Scroll suave ao topo da visualização
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // 6. Carrega dados frescos da respectiva área
    if (tabName === 'overview') {
        fetchAdminDashboard(true);
    } else if (tabName === 'drivers') {
        loadDriversTable(1);
    } else if (tabName === 'campaigns') {
        loadCampaignsDashboard();
        loadCampaignsTable();
    } else if (tabName === 'plans') {
        loadPlansCatalog();
    } else if (tabName === 'subs-payments') {
        loadSubscriptionsAndPayments();
    } else if (tabName === 'flags') {
        loadFeatureFlags();
    } else if (tabName === 'settings') {
        loadSettings();
    }
}

function openDriverFiltersSheet() {
    const deskFilter = document.getElementById('driver-status-filter');
    const mobileFilter = document.getElementById('mobile-driver-status-filter');
    if (deskFilter && mobileFilter) {
        mobileFilter.value = deskFilter.value;
    }
    openModal('sheet-driver-filters');
}

function applyDriverFiltersMobile() {
    const deskFilter = document.getElementById('driver-status-filter');
    const mobileFilter = document.getElementById('mobile-driver-status-filter');
    if (deskFilter && mobileFilter) {
        deskFilter.value = mobileFilter.value;
    }
    closeModal('sheet-driver-filters');
    loadDriversTable(1);
}

function resetDriverFiltersMobile() {
    const deskFilter = document.getElementById('driver-status-filter');
    const mobileFilter = document.getElementById('mobile-driver-status-filter');
    if (deskFilter) deskFilter.value = 'ALL';
    if (mobileFilter) mobileFilter.value = 'ALL';
    closeModal('sheet-driver-filters');
    loadDriversTable(1);
}

// ==========================================================================
// 4. VISÃO GERAL (DASHBOARD REAL & LIVE FEED)
// ==========================================================================

async function fetchAdminDashboard(isManual = false) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/dashboard`, {
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            const kpis = data.kpis || {};

            const mrr = kpis.mrr_reais || 0;
            const arr = kpis.arr_reais || 0;
            const totalUsers = kpis.total_users || 0;
            const totalDrivers = kpis.total_drivers || 0;
            const activeDrivers = kpis.active_drivers || 0;
            const activeSubs = kpis.active_pro_subscribers || 0;
            const evals = kpis.total_evaluations_recorded || 0;

            const prev = liveSyncState.previousValues;

            // Animação de glow quando os valores mudam
            if (prev.mrr !== undefined && prev.mrr !== mrr) triggerCardGlow('card-kpi-mrr');
            if (prev.arr !== undefined && prev.arr !== arr) triggerCardGlow('card-kpi-arr');
            if (prev.totalUsers !== undefined && prev.totalUsers !== totalUsers) triggerCardGlow('card-kpi-users');
            if (prev.totalDrivers !== undefined && prev.totalDrivers !== totalDrivers) triggerCardGlow('card-kpi-drivers');
            if (prev.activeSubs !== undefined && prev.activeSubs !== activeSubs) triggerCardGlow('card-kpi-subs');
            if (prev.evals !== undefined && prev.evals !== evals) triggerCardGlow('card-kpi-evals');

            // Armazena valores atuais
            liveSyncState.previousValues = { mrr, arr, totalUsers, totalDrivers, activeSubs, evals };

            document.getElementById('kpi-mrr').innerText = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(mrr);

            document.getElementById('kpi-arr').innerText = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(arr);

            document.getElementById('kpi-users').innerText = totalUsers;
            document.getElementById('kpi-drivers').innerText = totalDrivers;
            document.getElementById('kpi-active-drivers').innerText = activeDrivers;
            document.getElementById('kpi-blocked-drivers').innerText = kpis.blocked_drivers || 0;
            document.getElementById('kpi-subs').innerText = activeSubs;
            document.getElementById('kpi-evals').innerText = evals;

            // Métricas Comerciais e de Conversão
            const convRate = totalDrivers > 0 ? ((activeSubs / totalDrivers) * 100).toFixed(1) + '%' : '0%';
            const avgTicket = activeSubs > 0 
                ? new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(mrr / activeSubs)
                : 'R$ 0,00';
            const actRate = totalDrivers > 0 ? ((activeDrivers / totalDrivers) * 100).toFixed(0) + '%' : '100%';

            const convEl = document.getElementById('kpi-conversion-rate');
            if (convEl) convEl.innerText = convRate;

            const ticketEl = document.getElementById('kpi-avg-ticket');
            if (ticketEl) ticketEl.innerText = avgTicket;

            const actEl = document.getElementById('kpi-activation-rate');
            if (actEl) actEl.innerText = actRate;

            // Atualiza Feed de Atividades ao Vivo
            renderLiveActivityFeed(data.recent_activity || []);

            if (isManual) {
                showToast('Indicadores atualizados com sucesso!', 'success');
            }
        } else if (response.status === 401 || response.status === 403) {
            handleAdminLogout();
        }
    } catch (err) {
        console.warn('Erro ao carregar dashboard em tempo real:', err);
    }
}

function renderLiveActivityFeed(recentDrivers) {
    const feedContainer = document.getElementById('live-activity-feed');
    if (!feedContainer) return;

    // Combina eventos de motoristas reais do banco com eventos de simulação da sessão
    const events = [];

    // Adiciona motoristas recentes do PostgreSQL
    recentDrivers.forEach(d => {
        const timeFormatted = d.created_at ? new Date(d.created_at).toLocaleTimeString('pt-BR') : 'recente';
        events.push({
            type: 'DRIVER',
            title: `Motorista ${d.city ? `(${d.city}/${d.state})` : 'cadastrado'}`,
            detail: `ID: ${d.id.substring(0, 8)}... • Status: ${d.status}`,
            tag: '🚗 MOTORISTA',
            tagBg: 'rgba(0, 229, 255, 0.15)',
            tagColor: '#00E5FF',
            time: timeFormatted
        });
    });

    // Se temos itens manuais no activityLog
    liveSyncState.activityLog.slice(0, 10).forEach(logItem => {
        events.unshift(logItem);
    });

    if (events.length === 0) {
        feedContainer.innerHTML = `
            <div style="text-align: center; padding: 25px; color: var(--text-muted); font-size: 0.85rem;">
                Aguardando novas operações de motoristas em tempo real...
            </div>
        `;
        return;
    }

    feedContainer.innerHTML = '';
    events.slice(0, 8).forEach(item => {
        const row = document.createElement('div');
        row.className = 'activity-feed-item';
        row.innerHTML = `
            <div style="display: flex; align-items: center; gap: 12px;">
                <span class="activity-feed-tag" style="background: ${item.tagBg}; color: ${item.tagColor};">
                    ${item.tag}
                </span>
                <div>
                    <div style="font-weight: 700; color: #fff; font-size: 0.88rem;">${escapeHtml(item.title)}</div>
                    <div style="font-size: 0.78rem; color: var(--text-secondary); margin-top: 2px;">${escapeHtml(item.detail)}</div>
                </div>
            </div>
            <div style="font-size: 0.75rem; color: var(--text-muted); font-family: monospace;">
                ${item.time}
            </div>
        `;
        feedContainer.appendChild(row);
    });
}

/**
 * ==========================================================================
 * SIMULADOR DE DISPARO DE USUÁRIOS AO VIVO (TESTE REAL MULTI-USUÁRIO)
 * ==========================================================================
 */

let simulationCounter = 1;

async function triggerLiveDriverTraffic() {
    const btn = document.querySelector('.btn-simulate-live');
    if (btn) {
        btn.disabled = true;
        btn.innerText = '⚡ PROCESSANDO...';
    }

    try {
        const simId = `${Date.now()}_${Math.floor(Math.random() * 8999 + 1000)}`;
        const driverName = `Motorista Teste #${simulationCounter++} (${simId.slice(-4)})`;
        const driverEmail = `motorista_${simId}@rotai.app`;
        const city = ['São Paulo', 'Campinas', 'Rio de Janeiro', 'Curitiba', 'Belo Horizonte'][Math.floor(Math.random() * 5)];
        const state = city === 'Rio de Janeiro' ? 'RJ' : (city === 'Curitiba' ? 'PR' : (city === 'Belo Horizonte' ? 'MG' : 'SP'));

        const simPhone = '119' + Math.floor(10000000 + Math.random() * 89999999).toString();

        // 1. Cadastra o motorista no banco real
        const regResp = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: driverEmail,
                password: 'SenhaForte@2026',
                full_name: driverName,
                phone: simPhone
            })
        });

        if (!regResp.ok) {
            const err = await regResp.json().catch(() => ({}));
            throw new Error(err.detail || 'Falha ao registrar motorista na simulação.');
        }

        const regData = await regResp.json();
        const driverToken = regData.access_token;
        const driverId = regData.driver_id || regData.user_id;

        // 2. Registra o Token FCM do aparelho Android do motorista
        const fcmToken = `fcm_device_token_${simId}_${Math.random().toString(36).substring(7)}`;
        await fetch(`${API_BASE_URL}/devices/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                user_id: driverId,
                fcm_token: fcmToken,
                platform: 'android',
                device_id: `samsung_galaxy_s24_${simId.slice(-4)}`,
                app_version: '1.0.0',
                os_version: 'Android 14 (API 34)',
                notifications_enabled: true
            })
        }).catch(e => console.warn('Aviso no registro do FCM:', e));

        // 3. Sincroniza uma corrida e um abastecimento via /api/v1/sync/push
        const syncResp = await fetch(`${API_BASE_URL}/sync/push`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${driverToken}`
            },
            body: JSON.stringify({
                device_id: `device_${simId.slice(-4)}`,
                client_timestamp: Date.now(),
                evaluations: [{
                    platform: 'UBERX',
                    gross_fare: 42.80,
                    distance_km: 12.5,
                    duration_minutes: 27.0,
                    estimated_cost: 11.40,
                    net_profit: 31.40,
                    score: 95,
                    classification: 'EXCELLENT',
                    was_accepted: true
                }],
                fuel_records: [{
                    date: new Date().toISOString().split('T')[0],
                    odometer_km: 74200,
                    liters: 35.0,
                    price_per_liter: 5.69,
                    total_paid: 199.15,
                    fuel_type: 'GASOLINE',
                    is_full_tank: true
                }]
            })
        });

        if (!syncResp.ok) {
            const err = await syncResp.json().catch(() => ({}));
            throw new Error(err.detail || 'Falha ao sincronizar corrida e abastecimento.');
        }

        const syncResult = await syncResp.json();

        // 4. Cria e confirma uma assinatura PRO via Pix para 50% dos motoristas simulados
        let pixUpgraded = false;
        if (Math.random() > 0.3) {
            const pixResp = await fetch(`${API_BASE_URL}/subscription/pix-create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${driverToken}`
                },
                body: JSON.stringify({ plan_code: 'pro_monthly' })
            });

            if (pixResp.ok) {
                const pixData = await pixResp.json();
                // Confirma o Pix instantaneamente simulando o webhook do Banco Central
                await fetch(`${API_BASE_URL}/subscription/pix-confirm/${pixData.order_id}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${driverToken}`
                    }
                });
                pixUpgraded = true;
            }
        }

        // Adiciona evento ao Feed de Atividades
        const now = new Date().toLocaleTimeString('pt-BR');
        liveSyncState.activityLog.unshift({
            type: 'LIVE_SIM',
            title: `${driverName} (${city})`,
            detail: pixUpgraded 
                ? `⚡ Avaliou corrida R$ 42,80 • Sincronizou tanque • Assinou PRO via Pix (R$ 29,90)!` 
                : `⚡ Avaliou corrida R$ 42,80 • Sincronizou tanque via Sync/Push`,
            tag: pixUpgraded ? '💎 ASSINANTE PRO' : '⚡ AVALIAÇÃO + SYNC',
            tagBg: pixUpgraded ? 'rgba(255, 122, 0, 0.2)' : 'rgba(0, 230, 118, 0.15)',
            tagColor: pixUpgraded ? '#FF7A00' : '#00E676',
            time: now
        });

        showToast(
            pixUpgraded 
                ? `✓ ${driverName} avaliou corrida e virou PRO (+R$ 29,90 no MRR)!`
                : `✓ ${driverName} avaliou corrida e sincronizou com sucesso!`,
            'success'
        );

        // Atualiza imediatamente o painel com animação de glow
        await fetchAdminDashboard(false);

    } catch (err) {
        showToast(`Erro na simulação: ${err.message}`, 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerText = '⚡ SIMULAR MOTORISTAS AO VIVO';
        }
    }
}

// ==========================================================================
// 5. MOTORISTAS — GESTÃO, BUSCA, PAGINAÇÃO, MODAL, BLOQUEIO
// ==========================================================================

function debounceDriverSearch() {
    clearTimeout(driverPagination.searchDebounceTimer);
    driverPagination.searchDebounceTimer = setTimeout(() => {
        loadDriversTable(1);
    }, 350);
}

async function loadDriversTable(page = 1) {
    const tbody = document.getElementById('drivers-table-body');
    tbody.innerHTML = `
        <tr><td colspan="7" style="padding: 14px;"><div class="skeleton" style="height: 38px; width: 100%; border-radius: 8px;"></div></td></tr>
        <tr><td colspan="7" style="padding: 14px;"><div class="skeleton" style="height: 38px; width: 100%; border-radius: 8px;"></div></td></tr>
        <tr><td colspan="7" style="padding: 14px;"><div class="skeleton" style="height: 38px; width: 100%; border-radius: 8px;"></div></td></tr>
    `;

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
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align:center; padding: 40px 20px; color: var(--text-muted);">
                            <div style="font-size: 2.2rem; margin-bottom: 8px;">🚗</div>
                            <div style="font-size: 1.05rem; font-weight: 700; color: #fff;">Nenhum motorista encontrado</div>
                            <div style="font-size: 0.82rem; margin-top: 4px; color: var(--text-secondary);">Não localizamos cadastros com os filtros informados.</div>
                            <button class="btn-primary-action" style="margin-top: 14px; width: auto; padding: 8px 18px;" onclick="openDriverModal()">+ Cadastrar Motorista</button>
                        </td>
                    </tr>
                `;
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
                    <td data-label="Motorista">
                        <strong>${escapeHtml(d.name)}</strong>
                        <div style="font-size: 0.72rem; color: var(--text-muted);">${escapeHtml(d.id.substring(0, 10))}</div>
                    </td>
                    <td data-label="Contato">
                        <div>${escapeHtml(d.email)}</div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(d.phone || '—')}</div>
                    </td>
                    <td data-label="Cidade / UF">${escapeHtml(d.city)} / ${escapeHtml(d.state)}</td>
                    <td data-label="Status"><span class="chip ${statusClass}">${d.status}</span></td>
                    <td data-label="Plano"><span class="chip ${planClass}">${(d.plan_code || 'free').toUpperCase()}</span></td>
                    <td data-label="Cadastro">${createdAt}</td>
                    <td data-label="Ações">
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
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: #FF334B; padding: 25px;">Falha ao carregar motoristas (HTTP ${response.status})</td></tr>`;
        }
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color: #FF334B; padding: 25px;">Erro de conexão ao carregar motoristas.</td></tr>';
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
    submitBtn.innerText = 'Salvando...';
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
        showToast('Motorista salvo com sucesso!', 'success');
        loadDriversTable(driverPagination.currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        submitBtn.innerText = 'Salvar Motorista';
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
        alert('Por favor, informe o motivo do bloqueio.');
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
        showToast('Motorista bloqueado com sucesso.', 'success');
        loadDriversTable(driverPagination.currentPage);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleUnblockDriver(driverId) {
    if (!confirm('Deseja realmente reativar este motorista?')) return;

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
    container.innerHTML = 'Carregando detalhes...';
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
                <h4 style="color: #fff; margin-bottom: 8px;">Situação Cadastral</h4>
                <div><strong>ID do Motorista:</strong> <code>${escapeHtml(d.id)}</code></div>
                <div><strong>Praça / Região:</strong> ${escapeHtml(d.city || '—')} / ${escapeHtml(d.state || '—')}</div>
                <div><strong>Status:</strong> <span class="chip ${d.status === 'BLOCKED' ? 'chip-blocked' : 'chip-active'}">${d.status}</span></div>
                ${d.block_reason ? `<div style="color: #FF334B; margin-top: 6px;"><strong>Motivo do Bloqueio:</strong> ${escapeHtml(d.block_reason)}</div>` : ''}
            </div>

            <div style="background: rgba(255,255,255,0.03); padding: 16px; border-radius: 8px;">
                <h4 style="color: #fff; margin-bottom: 8px;">Assinatura</h4>
                ${sub ? `
                    <div><strong>Plano Ativo:</strong> <span class="chip chip-pro">${sub.tier || 'PRO'}</span></div>
                    <div><strong>Forma de Cobrança:</strong> ${sub.provider || 'PIX'}</div>
                    <div><strong>Expira em:</strong> ${sub.expires_at ? new Date(sub.expires_at).toLocaleDateString('pt-BR') : '—'}</div>
                ` : `<div>O motorista está no <strong>Plano Gratuito</strong>.</div>`}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div style="color: #FF334B;">Erro ao carregar detalhes: ${err.message}</div>`;
    }
}

// ==========================================================================
// 6. PLANOS & PREÇOS — GESTÃO COMERCIAL
// ==========================================================================

async function loadPlansCatalog() {
    const container = document.getElementById('plans-container');
    container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 30px; color: #888;">Carregando planos...</div>';

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
    submitBtn.innerText = 'Salvando plano...';
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
        showToast('Plano salvo com sucesso! O novo valor já está disponível no aplicativo.', 'success');
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

    subsTbody.innerHTML = `
        <tr><td colspan="6" style="padding: 12px;"><div class="skeleton" style="height: 36px; width: 100%; border-radius: 8px;"></div></td></tr>
        <tr><td colspan="6" style="padding: 12px;"><div class="skeleton" style="height: 36px; width: 100%; border-radius: 8px;"></div></td></tr>
    `;
    payTbody.innerHTML = `
        <tr><td colspan="5" style="padding: 12px;"><div class="skeleton" style="height: 36px; width: 100%; border-radius: 8px;"></div></td></tr>
        <tr><td colspan="5" style="padding: 12px;"><div class="skeleton" style="height: 36px; width: 100%; border-radius: 8px;"></div></td></tr>
    `;

    try {
        const [subsRes, payRes] = await Promise.all([
            fetch(`${API_BASE_URL}/admin/subscriptions`, { headers: getAuthHeaders() }),
            fetch(`${API_BASE_URL}/admin/payments`, { headers: getAuthHeaders() })
        ]);

        if (subsRes.ok) {
            const subs = await subsRes.json();
            if (subs.length === 0) {
                subsTbody.innerHTML = `
                    <tr>
                        <td colspan="6" style="text-align:center; padding: 35px 20px; color: var(--text-muted);">
                            <div style="font-size: 2rem; margin-bottom: 6px;">💳</div>
                            <div style="font-size: 1rem; font-weight: 700; color: #fff;">Nenhuma assinatura ativa</div>
                            <div style="font-size: 0.8rem; margin-top: 4px;">Assinaturas confirmadas de motoristas aparecerão aqui automaticamente.</div>
                        </td>
                    </tr>
                `;
            } else {
                subsTbody.innerHTML = '';
                subs.forEach(s => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td data-label="ID Assinatura"><code>${escapeHtml(s.id?.substring(0, 8))}...</code></td>
                        <td data-label="Motorista"><code>${escapeHtml(s.driver_id?.substring(0, 8))}...</code></td>
                        <td data-label="Plano"><span class="chip chip-pro">${escapeHtml(s.plan_code || s.tier || 'PRO')}</span></td>
                        <td data-label="Status"><span class="chip ${s.status === 'ACTIVE' ? 'chip-active' : 'chip-inactive'}">${escapeHtml(s.status)}</span></td>
                        <td data-label="Forma">${escapeHtml(s.provider || 'PIX')}</td>
                        <td data-label="Expira Em">${s.expires_at ? new Date(s.expires_at).toLocaleDateString('pt-BR') : '—'}</td>
                    `;
                    subsTbody.appendChild(tr);
                });
            }
        }

        if (payRes.ok) {
            const payments = await payRes.json();
            if (payments.length === 0) {
                payTbody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align:center; padding: 35px 20px; color: var(--text-muted);">
                            <div style="font-size: 2rem; margin-bottom: 6px;">⚡</div>
                            <div style="font-size: 1rem; font-weight: 700; color: #fff;">Nenhuma transação Pix registrada</div>
                            <div style="font-size: 0.8rem; margin-top: 4px;">Pagamentos e pedidos Pix gerados no app serão listados aqui em tempo real.</div>
                        </td>
                    </tr>
                `;
            } else {
                payTbody.innerHTML = '';
                payments.forEach(p => {
                    const tr = document.createElement('tr');
                    const amount = (p.amount_cents / 100.0).toFixed(2).replace('.', ',');
                    tr.innerHTML = `
                        <td data-label="Pedido ID"><code>${escapeHtml(p.order_id || p.tx_id || p.id?.substring(0, 8))}</code></td>
                        <td data-label="Valor"><strong style="color: var(--green-accent);">R$ ${amount}</strong></td>
                        <td data-label="Plano">${escapeHtml(p.plan_code || '—')}</td>
                        <td data-label="Status"><span class="chip ${p.status === 'PAID' ? 'chip-active' : 'chip-inactive'}">${escapeHtml(p.status)}</span></td>
                        <td data-label="Data Transação">${p.created_at ? new Date(p.created_at).toLocaleString('pt-BR') : '—'}</td>
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
// 8. CONTROLE DE FUNCIONALIDADES (FEATURE FLAGS)
// ==========================================================================

async function loadFeatureFlags() {
    const container = document.getElementById('flags-container');
    container.innerHTML = '<div style="text-align:center; padding: 30px; color: #888;">Carregando funcionalidades...</div>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/feature-flags`, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao obter funcionalidades.');

        const flags = await response.json();
        if (flags.length === 0) {
            container.innerHTML = '<div style="text-align:center; padding: 30px; color: #888;">Nenhuma funcionalidade cadastrada.</div>';
            return;
        }

        container.innerHTML = '';
        flags.forEach(f => {
            const row = document.createElement('div');
            row.className = 'flag-row';
            row.innerHTML = `
                <div>
                    <strong>${escapeHtml(f.name || f.key)}</strong>
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
        container.innerHTML = `<div style="text-align:center; color: #FF334B; padding: 20px;">Erro ao carregar funcionalidades: ${err.message}</div>`;
    }
}

async function toggleFlag(flagKey, isEnabled) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/feature-flags/${flagKey}`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ is_enabled: isEnabled })
        });
        if (!response.ok) throw new Error('Falha ao alternar funcionalidade.');
        showToast(`Recurso ${isEnabled ? 'ativado' : 'desativado'} com sucesso!`, 'success');
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
        if (!response.ok) throw new Error('Falha ao cadastrar funcionalidade.');

        closeModal('modal-flag');
        showToast(`Funcionalidade cadastrada com sucesso!`, 'success');
        loadFeatureFlags();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================================================
// 9. CONFIGURAÇÕES OPERACIONAIS DO SISTEMA
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
                    </div>
                    <div class="setting-title">${escapeHtml(s.description || s.key)}</div>
                    <div class="setting-desc">Chave: <code>${escapeHtml(s.key)}</code></div>
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
        showToast(`Configuração salva com sucesso!`, 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================================================
// 10. 📣 MÓDULO REAL DE CAMPANHAS E PUSH NOTIFICATIONS (FIREBASE FCM)
// ==========================================================================

let currentCampaignsStatusFilter = 'ALL';
let currentAudienceStats = {
    total_users: 0,
    eligible_devices: 0,
    users_without_token: 0,
    segment: 'Geral'
};
let editingCampaignId = null;

/**
 * Carrega indicadores do dashboard de campanhas
 */
async function loadCampaignsDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/dashboard`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Falha ao carregar métricas de campanhas.');

        const stats = await response.json();
        const setVal = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.innerText = val !== null && val !== undefined ? val : 'N/D';
        };

        setVal('kpi-camp-total', stats.total_campaigns);
        setVal('kpi-camp-sent', stats.sent_campaigns);
        setVal('kpi-camp-scheduled', stats.scheduled_campaigns);
        setVal('kpi-camp-drafts', stats.draft_campaigns);
        setVal('kpi-camp-devices', stats.eligible_devices ?? stats.active_eligible_devices);
        setVal('kpi-camp-total-sent', stats.total_notifications_sent);
        setVal('kpi-camp-failures', stats.total_notifications_failed);
        setVal('kpi-camp-open-rate', stats.open_rate ?? stats.average_open_rate ?? 'N/D');
    } catch (err) {
        console.warn('Erro ao carregar dashboard de campanhas:', err);
    }
}

/**
 * Filtra campanhas por status através dos pills de navegação
 */
function filterCampaignsByStatus(status, btnElement) {
    currentCampaignsStatusFilter = status;
    const pills = document.querySelectorAll('.status-filter-pills .pill-btn');
    pills.forEach(p => p.classList.remove('active'));
    if (btnElement) btnElement.classList.add('active');
    loadCampaignsTable(status);
}

/**
 * Carrega a tabela de campanhas com filtros reais
 */
async function loadCampaignsTable(filterStatus = null) {
    const tbody = document.getElementById('campaigns-table-body');
    if (!tbody) return;

    const status = filterStatus || currentCampaignsStatusFilter;
    tbody.innerHTML = `
        <tr><td colspan="9" style="padding: 14px;"><div class="skeleton" style="height: 38px; width: 100%; border-radius: 8px;"></div></td></tr>
        <tr><td colspan="9" style="padding: 14px;"><div class="skeleton" style="height: 38px; width: 100%; border-radius: 8px;"></div></td></tr>
    `;

    try {
        let url = `${API_BASE_URL}/admin/campaigns`;
        if (status && status !== 'ALL') {
            url += `?status=${encodeURIComponent(status)}`;
        }

        const response = await fetch(url, { headers: getAuthHeaders() });
        if (!response.ok) throw new Error('Falha ao listar campanhas.');

        const data = await response.json();
        const campaigns = data.campaigns || [];

        if (campaigns.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; padding: 40px 20px; color: var(--text-muted);">
                        <div style="font-size: 2.2rem; margin-bottom: 8px;">📣</div>
                        <div style="font-size: 1.05rem; font-weight: 700; color: #fff;">Nenhuma campanha encontrada</div>
                        <div style="font-size: 0.82rem; margin-top: 4px; color: var(--text-secondary);">Crie sua primeira campanha para disparar notificações push aos motoristas.</div>
                        <button class="btn-primary-action" style="margin-top: 14px; width: auto; padding: 8px 18px;" onclick="openNewCampaignWizard()">+ Nova Campanha</button>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = '';
        campaigns.forEach(c => {
            const tr = document.createElement('tr');

            // Formatação do Tipo
            const typeClass = `chip-${(c.type || 'informativa').toLowerCase()}`;
            const typeLabel = escapeHtml(c.type || 'INFORMATIVA');

            // Formatação do Status
            let statusBadge = `<span class="badge badge-warning">${escapeHtml(c.status)}</span>`;
            if (c.status === 'SENT') {
                statusBadge = `<span class="badge badge-success">ENVIADA</span>`;
            } else if (c.status === 'SCHEDULED') {
                statusBadge = `<span class="badge badge-info">AGENDADA</span>`;
            } else if (c.status === 'DRAFT') {
                statusBadge = `<span class="badge badge-secondary">RASCUNHO</span>`;
            } else if (c.status === 'PROCESSING') {
                statusBadge = `<span class="badge badge-primary">PROCESSANDO</span>`;
            } else if (c.status === 'CANCELLED') {
                statusBadge = `<span class="badge badge-danger">CANCELADA</span>`;
            } else if (c.status === 'FAILED') {
                statusBadge = `<span class="badge badge-danger">FALHOU</span>`;
            }

            // Data / Agendamento
            const dateStr = c.sent_at || c.scheduled_at || c.created_at;
            const formattedDate = dateStr ? new Date(dateStr).toLocaleString('pt-BR') : 'N/D';

            // Destinatários e métricas reais
            const recipients = c.recipient_count || 'N/D';
            const sentAndFailures = `${c.sent_count || 0} / <span style="color: ${c.failure_count > 0 ? '#FF334B' : 'inherit'}">${c.failure_count || 0}</span>`;
            const openings = c.open_count !== null && c.open_count !== undefined ? c.open_count : 'N/D';

            // Ações disponíveis com base no estado
            let actionsHtml = `
                <div style="display: flex; gap: 6px; justify-content: flex-end;">
                    <button class="btn-action" title="Ver Relatório Detalhado" onclick="openCampaignReport('${c.id}')">📊 Relatório</button>
                    <button class="btn-action" title="Duplicar Campanha" onclick="handleDuplicateCampaign('${c.id}')">📋 Duplicar</button>
            `;

            if (c.status === 'SCHEDULED') {
                actionsHtml += `<button class="btn-action btn-action-danger" title="Cancelar Agendamento" onclick="handleCancelCampaign('${c.id}')">❌ Cancelar</button>`;
            } else if (c.status === 'DRAFT') {
                actionsHtml += `
                    <button class="btn-action btn-action-primary" title="Disparar Agora" onclick="handleSendCampaignNow('${c.id}')">🚀 Enviar</button>
                    <button class="btn-action btn-action-danger" title="Excluir Rascunho" onclick="handleDeleteCampaign('${c.id}')">🗑️ Excluir</button>
                `;
            }

            actionsHtml += `</div>`;

            tr.innerHTML = `
                <td data-label="Campanha">
                    <div style="font-weight: 600; color: #fff;">${escapeHtml(c.name)}</div>
                    <div style="font-size: 0.8rem; color: var(--text-secondary); max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(c.title)}</div>
                </td>
                <td data-label="Tipo"><span class="chip-type ${typeClass}">${typeLabel}</span></td>
                <td data-label="Público"><span style="font-size: 0.82rem; color: #eee;">${escapeHtml(formatAudienceName(c.audience_type))}</span></td>
                <td data-label="Status">${statusBadge}</td>
                <td data-label="Data">${formattedDate}</td>
                <td data-label="Destinatários" style="font-weight: 600; color: #fff;">${recipients}</td>
                <td data-label="Envios / Falhas" style="font-size: 0.85rem;">${sentAndFailures}</td>
                <td data-label="Aberturas" style="font-size: 0.85rem; color: var(--blue-accent);">${openings}</td>
                <td data-label="Ações" style="text-align: right;">${actionsHtml}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: #FF334B; padding: 25px;">Erro: ${err.message}</td></tr>`;
    }
}

function formatAudienceName(type) {
    const map = {
        'ALL': '📢 Todos os Usuários',
        'FREE': '🆓 Apenas Free',
        'PRO': '💎 Apenas Pro',
        'ACTIVE': '⚡ Usuários Ativos',
        'INACTIVE': '💤 Usuários Inativos',
        'DRIVERS': '🚗 Todos os Motoristas',
        'CUSTOM_GEO': '📍 Por Cidade / Estado'
    };
    return map[type] || type || 'Todos';
}

/**
 * ==========================================================================
 * FLUXO WIZARD DE CRIAÇÃO DA CAMPANHA (5 ETAPAS)
 * ==========================================================================
 */

function openNewCampaignWizard() {
    editingCampaignId = null;
    document.getElementById('wizard-title').innerHTML = `Nova Campanha <span>Push Notification</span>`;
    
    // Reset dos campos
    document.getElementById('camp-name').value = '';
    document.getElementById('camp-type').value = 'INFORMATIVA';
    document.getElementById('camp-channel').value = 'rotaiq_general';
    document.getElementById('camp-title').value = '';
    document.getElementById('camp-body').value = '';
    document.getElementById('camp-image').value = '';
    document.getElementById('camp-deeplink-select').value = 'rotaiq://home';
    document.getElementById('camp-deeplink-custom').value = '';
    document.getElementById('camp-deeplink-custom').style.display = 'none';

    // Reset público e ação
    const allRadio = document.querySelector('input[name="audience_target"][value="ALL"]');
    if (allRadio) allRadio.checked = true;
    handleAudienceTypeChange('ALL');

    const sendNowRadio = document.querySelector('input[name="campaign_action_choice"][value="SEND_NOW"]');
    if (sendNowRadio) sendNowRadio.checked = true;
    handleActionChoiceChange('SEND_NOW');

    // Reset contadores e preview
    updateLivePreview();
    calculateAudiencePreview();

    // Navegar para o passo 1
    goToWizardStep(1);
    openModal('modal-campaign-wizard');
}

function goToWizardStep(stepNumber) {
    // Validações antes de avançar
    if (stepNumber > 1) {
        const name = document.getElementById('camp-name').value.trim();
        const title = document.getElementById('camp-title').value.trim();
        const body = document.getElementById('camp-body').value.trim();

        if (!name) {
            showToast('Informe o nome interno da campanha.', 'error');
            return;
        }
        if (!title) {
            showToast('Informe o título da notificação push.', 'error');
            return;
        }
        if (!body) {
            showToast('Informe a mensagem da notificação.', 'error');
            return;
        }
    }

    if (stepNumber === 4) {
        // Prepara dados da tela de revisão
        populateReviewStep();
    }

    wizardCurrentStep = stepNumber;

    // Atualiza stepper pills
    for (let i = 1; i <= 5; i++) {
        const pill = document.getElementById(`step-pill-${i}`);
        const line = document.getElementById(`step-line-${i}`);
        const content = document.getElementById(`wizard-step-${i}`);

        if (pill) {
            if (i <= stepNumber) {
                pill.classList.add('active');
            } else {
                pill.classList.remove('active');
            }
        }
        if (line) {
            if (i < stepNumber) {
                line.classList.add('active');
            } else {
                line.classList.remove('active');
            }
        }
        if (content) {
            content.style.display = i === stepNumber ? 'block' : 'none';
        }
    }
}

/**
 * Atualiza prévia nativa do Android em tempo real e contadores de caracteres
 */
function updateLivePreview() {
    const titleInput = document.getElementById('camp-title');
    const bodyInput = document.getElementById('camp-body');
    const imageInput = document.getElementById('camp-image');

    const titleVal = titleInput ? titleInput.value : '';
    const bodyVal = bodyInput ? bodyInput.value : '';
    const imageVal = imageInput ? imageInput.value.trim() : '';

    // Contadores
    const countTitle = document.getElementById('char-count-title');
    const countBody = document.getElementById('char-count-body');
    if (countTitle) countTitle.innerText = `${titleVal.length}/60`;
    if (countBody) countBody.innerText = `${bodyVal.length}/160`;

    // Atualiza frame do Android
    const previewTitle = document.getElementById('preview-notif-title');
    const previewBody = document.getElementById('preview-notif-body');
    const previewImgContainer = document.getElementById('preview-notif-image-container');
    const previewImg = document.getElementById('preview-notif-image');

    if (previewTitle) {
        previewTitle.innerText = titleVal.trim() || '🚗 Título da Notificação';
    }
    if (previewBody) {
        previewBody.innerText = bodyVal.trim() || 'A mensagem que você escrever no painel aparecerá aqui exatamente como no Android do motorista.';
    }

    if (imageVal && isValidHttpUrl(imageVal)) {
        if (previewImg) previewImg.src = imageVal;
        if (previewImgContainer) previewImgContainer.style.display = 'block';
    } else {
        if (previewImgContainer) previewImgContainer.style.display = 'none';
    }
}

function isValidHttpUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === "http:" || url.protocol === "https:";
    } catch (_) {
        return false;
    }
}

function handleDeepLinkSelect(val) {
    const customInput = document.getElementById('camp-deeplink-custom');
    if (customInput) {
        customInput.style.display = val === 'custom' ? 'block' : 'none';
    }
}

function handleAudienceTypeChange(audienceType) {
    const geoFilters = document.getElementById('audience-geo-filters');
    if (geoFilters) {
        geoFilters.style.display = audienceType === 'CUSTOM_GEO' ? 'block' : 'none';
    }
    calculateAudiencePreview();
}

/**
 * Consulta a API real para obter audiência elegível (sem mocks)
 */
async function calculateAudiencePreview() {
    const selectedRadio = document.querySelector('input[name="audience_target"]:checked');
    const audienceType = selectedRadio ? selectedRadio.value : 'ALL';
    const city = document.getElementById('filter-city')?.value.trim() || null;
    const state = document.getElementById('filter-state')?.value.trim() || null;

    const payload = {
        audience_type: audienceType,
        audience_filter: { city, state }
    };

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/audience-preview`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json();
            currentAudienceStats = data;

            document.getElementById('aud-count-users').innerText = data.total_users ?? 0;
            document.getElementById('aud-count-devices').innerText = data.eligible_devices ?? 0;
            document.getElementById('aud-count-no-token').innerText = data.users_without_token ?? 0;
            document.getElementById('aud-segment-name').innerText = data.segment_description || 'Geral';
        }
    } catch (err) {
        console.warn('Erro ao consultar estimativa de audiência:', err);
    }
}

function handleActionChoiceChange(actionChoice) {
    const schedContainer = document.getElementById('schedule-fields-container');
    if (schedContainer) {
        schedContainer.style.display = actionChoice === 'SCHEDULE' ? 'block' : 'none';
    }
}

/**
 * Preenche a tela de revisão e ativa o banner de confirmação dupla quando necessário
 */
function populateReviewStep() {
    const name = document.getElementById('camp-name').value;
    const type = document.getElementById('camp-type').value;
    const title = document.getElementById('camp-title').value;
    const body = document.getElementById('camp-body').value;
    const deepSelect = document.getElementById('camp-deeplink-select').value;
    const deepCustom = document.getElementById('camp-deeplink-custom').value;
    const deepLink = deepSelect === 'custom' ? deepCustom : deepSelect;

    const selectedAudience = document.querySelector('input[name="audience_target"]:checked')?.value || 'ALL';
    const selectedAction = document.querySelector('input[name="campaign_action_choice"]:checked')?.value || 'SEND_NOW';

    document.getElementById('rev-name').innerText = name;
    document.getElementById('rev-type').innerText = type;
    document.getElementById('rev-title').innerText = title;
    document.getElementById('rev-body').innerText = body;
    document.getElementById('rev-audience').innerText = formatAudienceName(selectedAudience);
    document.getElementById('rev-devices').innerText = currentAudienceStats.eligible_devices ?? '0';
    document.getElementById('rev-deeplink').innerText = deepLink;

    let actionLabel = '🚀 Envio Imediato';
    if (selectedAction === 'SCHEDULE') {
        const sDate = document.getElementById('sched-date').value;
        const sTime = document.getElementById('sched-time').value;
        actionLabel = `⏰ Agendado para ${sDate} às ${sTime}`;
    } else if (selectedAction === 'DRAFT') {
        actionLabel = '📝 Salvar Rascunho';
    }
    document.getElementById('rev-action').innerText = actionLabel;

    // Dupla Confirmação para TODOS OS USUÁRIOS
    const warningDiv = document.getElementById('rev-all-users-warning');
    const confirmCheck = document.getElementById('rev-confirm-all-check');
    if (selectedAudience === 'ALL' && selectedAction === 'SEND_NOW') {
        if (warningDiv) warningDiv.style.display = 'block';
        if (confirmCheck) confirmCheck.checked = false;
    } else {
        if (warningDiv) warningDiv.style.display = 'none';
        if (confirmCheck) confirmCheck.checked = true;
    }
}

/**
 * Envia ou agenda a campanha após revisão
 */
async function submitCampaignWizard() {
    const selectedAudience = document.querySelector('input[name="audience_target"]:checked')?.value || 'ALL';
    const selectedAction = document.querySelector('input[name="campaign_action_choice"]:checked')?.value || 'SEND_NOW';

    // Verificação de dupla confirmação para envio global
    if (selectedAudience === 'ALL' && selectedAction === 'SEND_NOW') {
        const confirmCheck = document.getElementById('rev-confirm-all-check');
        if (confirmCheck && !confirmCheck.checked) {
            showToast('Marque a caixa de confirmação para autorizar o disparo para TODOS os dispositivos.', 'error');
            return;
        }
    }

    const name = document.getElementById('camp-name').value.trim();
    const type = document.getElementById('camp-type').value;
    const channelId = document.getElementById('camp-channel').value;
    const title = document.getElementById('camp-title').value.trim();
    const body = document.getElementById('camp-body').value.trim();
    const imageUrl = document.getElementById('camp-image').value.trim() || null;
    const deepSelect = document.getElementById('camp-deeplink-select').value;
    const deepCustom = document.getElementById('camp-deeplink-custom').value.trim();
    const deepLink = deepSelect === 'custom' ? deepCustom : deepSelect;

    let scheduledAt = null;
    if (selectedAction === 'SCHEDULE') {
        const sDate = document.getElementById('sched-date').value;
        const sTime = document.getElementById('sched-time').value;
        if (!sDate || !sTime) {
            showToast('Informe a data e o horário para agendamento.', 'error');
            goToWizardStep(3);
            return;
        }
        scheduledAt = `${sDate}T${sTime}:00`;
    }

    const payload = {
        name,
        type,
        title,
        body,
        image_url: imageUrl,
        deep_link: deepLink,
        channel_id: channelId,
        audience_type: selectedAudience,
        audience_filter: {
            city: document.getElementById('filter-city')?.value.trim() || null,
            state: document.getElementById('filter-state')?.value.trim() || null
        },
        action: selectedAction,
        scheduled_at: scheduledAt
    };

    // Navega para o passo 5 (progresso)
    goToWizardStep(5);
    document.getElementById('wizard-status-loading').style.display = 'block';
    document.getElementById('wizard-status-result').style.display = 'none';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao processar campanha.');
        }

        const result = await response.json();

        // Oculta loading e exibe resultado real
        document.getElementById('wizard-status-loading').style.display = 'none';
        document.getElementById('wizard-status-result').style.display = 'block';

        if (selectedAction === 'SEND_NOW') {
            document.getElementById('wizard-result-icon').innerText = '🚀';
            document.getElementById('wizard-result-title').innerText = 'Notificação Push Disparada!';
            document.getElementById('wizard-result-desc').innerText = `Campanha enviada via Firebase Cloud Messaging para ${result.sent_count || 0} dispositivos Android.`;
            document.getElementById('res-sent-count').innerText = result.sent_count || 0;
            document.getElementById('res-failure-count').innerText = result.failure_count || 0;
            document.getElementById('res-invalid-count').innerText = result.invalid_tokens || 0;
        } else if (selectedAction === 'SCHEDULE') {
            document.getElementById('wizard-result-icon').innerText = '⏰';
            document.getElementById('wizard-result-title').innerText = 'Campanha Agendada com Sucesso!';
            document.getElementById('wizard-result-desc').innerText = `Disparo programado para ${new Date(result.scheduled_at || scheduledAt).toLocaleString('pt-BR')}.`;
            document.getElementById('res-sent-count').innerText = '0 (Agendada)';
            document.getElementById('res-failure-count').innerText = '0';
            document.getElementById('res-invalid-count').innerText = '0';
        } else {
            document.getElementById('wizard-result-icon').innerText = '📝';
            document.getElementById('wizard-result-title').innerText = 'Rascunho Salvo com Sucesso!';
            document.getElementById('wizard-result-desc').innerText = 'Você pode revisar e disparar esta campanha a qualquer momento.';
            document.getElementById('res-sent-count').innerText = '0 (Rascunho)';
            document.getElementById('res-failure-count').innerText = '0';
            document.getElementById('res-invalid-count').innerText = '0';
        }

        showToast('Operação realizada com sucesso!', 'success');
        loadCampaignsDashboard();
        loadCampaignsTable();
    } catch (err) {
        document.getElementById('wizard-status-loading').style.display = 'none';
        document.getElementById('wizard-status-result').style.display = 'block';
        document.getElementById('wizard-result-icon').innerText = '⚠️';
        document.getElementById('wizard-result-title').innerText = 'Falha no Processamento';
        document.getElementById('wizard-result-desc').innerText = err.message;
        showToast(err.message, 'error');
    }
}

/**
 * Disparar campanha que estava como rascunho
 */
async function handleSendCampaignNow(id) {
    if (!confirm('Deseja realmente disparar esta notificação PUSH agora para os dispositivos elegíveis?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/${id}/send`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao enviar campanha.');
        }

        const data = await response.json();
        showToast(`Campanha disparada com sucesso para ${data.sent_count || 0} dispositivos!`, 'success');
        loadCampaignsDashboard();
        loadCampaignsTable();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/**
 * Cancelar campanha agendada
 */
async function handleCancelCampaign(id) {
    if (!confirm('Deseja cancelar o envio agendado desta campanha?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/${id}/cancel`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao cancelar agendamento.');
        }

        showToast('Agendamento cancelado com sucesso.', 'info');
        loadCampaignsDashboard();
        loadCampaignsTable();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/**
 * Duplicar campanha existente
 */
async function handleDuplicateCampaign(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/${id}/duplicate`, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao duplicar campanha.');
        }

        showToast('Campanha duplicada como Rascunho com sucesso!', 'success');
        loadCampaignsDashboard();
        loadCampaignsTable();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/**
 * Excluir campanha
 */
async function handleDeleteCampaign(id) {
    if (!confirm('Tem certeza que deseja excluir esta campanha?')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.detail || 'Falha ao excluir campanha.');
        }

        showToast('Campanha excluída com sucesso.', 'info');
        loadCampaignsDashboard();
        loadCampaignsTable();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/**
 * ==========================================================================
 * RELATÓRIO COMPLETO DA CAMPANHA
 * ==========================================================================
 */

async function openCampaignReport(id) {
    openModal('modal-campaign-report');
    document.getElementById('rep-camp-name').innerText = 'Carregando dados...';
    document.getElementById('rep-deliveries-table-body').innerHTML = `
        <tr><td colspan="5" style="text-align: center; padding: 20px; color: var(--text-muted);">Consultando histórico de entregas...</td></tr>
    `;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaigns/${id}/report`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw new Error('Falha ao obter relatório da campanha.');

        const data = await response.json();
        const camp = data.campaign || {};
        const deliveries = data.deliveries || [];

        document.getElementById('rep-camp-name').innerText = camp.name || 'Sem nome';
        document.getElementById('rep-notif-title').innerText = camp.title || '-';
        document.getElementById('rep-notif-body').innerText = camp.body || '-';
        document.getElementById('rep-badge-status').innerHTML = `<span class="badge badge-info">${escapeHtml(camp.status || '-')}</span>`;
        document.getElementById('rep-sent-at').innerText = camp.sent_at ? new Date(camp.sent_at).toLocaleString('pt-BR') : 'Ainda não enviada';

        // Métricas reais (sem invenções)
        document.getElementById('rep-devices-count').innerText = data.eligible_devices !== undefined ? data.eligible_devices : 'N/D';
        document.getElementById('rep-sent-count').innerText = data.messages_sent !== undefined ? data.messages_sent : '0';
        document.getElementById('rep-failed-count').innerText = data.failures !== undefined ? data.failures : '0';
        document.getElementById('rep-open-rate').innerText = data.open_rate || 'N/D';

        // Tabela de entregas
        const tbody = document.getElementById('rep-deliveries-table-body');
        if (deliveries.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 20px; color: var(--text-muted);">Nenhum registro de entrega encontrado para esta campanha.</td></tr>`;
            return;
        }

        tbody.innerHTML = '';
        deliveries.forEach(d => {
            const tr = document.createElement('tr');
            const statusColor = d.status === 'SENT' ? '#00E676' : (d.status === 'FAILED' ? '#FF334B' : '#FFB300');
            tr.innerHTML = `
                <td data-label="Dispositivo" style="font-family: monospace;">${escapeHtml(d.device_token_id ? d.device_token_id.substring(0, 12) + '...' : 'Geral')}</td>
                <td data-label="Status"><span style="color: ${statusColor}; font-weight: 600;">${escapeHtml(d.status)}</span></td>
                <td data-label="Msg FCM" style="font-family: monospace; font-size: 0.75rem;">${escapeHtml(d.fcm_message_id || 'N/D')}</td>
                <td data-label="Erro" style="color: #FF334B;">${escapeHtml(d.error_code || '-')}</td>
                <td data-label="Horário" style="color: var(--text-muted);">${d.sent_at ? new Date(d.sent_at).toLocaleTimeString('pt-BR') : '-'}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/**
 * ==========================================================================
 * MODELOS REUTILIZÁVEIS (TEMPLATES)
 * ==========================================================================
 */

async function openTemplatesModal() {
    openModal('modal-campaign-templates');
    const container = document.getElementById('templates-list-container');
    container.innerHTML = `<div style="text-align: center; padding: 30px; color: var(--text-muted);">Carregando modelos do sistema...</div>`;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/campaign-templates`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) throw new Error('Falha ao listar templates.');

        const templates = await response.json();
        if (templates.length === 0) {
            container.innerHTML = `<div style="text-align: center; padding: 30px; color: var(--text-muted);">Nenhum template cadastrado.</div>`;
            return;
        }

        container.innerHTML = '';
        templates.forEach(t => {
            const card = document.createElement('div');
            card.className = 'template-item-card';
            card.style.background = 'rgba(255,255,255,0.03)';
            card.style.border = '1px solid var(--border-subtle)';
            card.style.borderRadius = '8px';
            card.style.padding = '14px';
            card.style.display = 'flex';
            card.style.justifyContent = 'space-between';
            card.style.alignItems = 'center';

            card.innerHTML = `
                <div>
                    <div style="font-weight: 600; color: #fff; font-size: 0.95rem;">${escapeHtml(t.name)}</div>
                    <div style="font-size: 0.82rem; color: var(--primary-orange); margin-top: 2px;">${escapeHtml(t.title)}</div>
                    <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 2px; max-width: 420px;">${escapeHtml(t.body)}</div>
                </div>
                <button class="btn-primary-action" style="padding: 6px 14px; font-size: 0.82rem;" onclick="applyTemplate(${JSON.stringify(t).replace(/"/g, '&quot;')})">
                    Usar este Modelo
                </button>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        container.innerHTML = `<div style="text-align: center; color: #FF334B; padding: 20px;">Erro: ${err.message}</div>`;
    }
}

function applyTemplate(template) {
    closeModal('modal-campaign-templates');
    openNewCampaignWizard();

    document.getElementById('camp-name').value = `${template.name} - ${new Date().toLocaleDateString('pt-BR')}`;
    document.getElementById('camp-type').value = template.type || 'INFORMATIVA';
    document.getElementById('camp-title').value = template.title || '';
    document.getElementById('camp-body').value = template.body || '';

    if (template.deep_link) {
        const select = document.getElementById('camp-deeplink-select');
        const options = Array.from(select.options).map(o => o.value);
        if (options.includes(template.deep_link)) {
            select.value = template.deep_link;
            document.getElementById('camp-deeplink-custom').style.display = 'none';
        } else {
            select.value = 'custom';
            const custom = document.getElementById('camp-deeplink-custom');
            custom.style.display = 'block';
            custom.value = template.deep_link;
        }
    }

    updateLivePreview();
    showToast(`Modelo "${template.name}" aplicado!`, 'info');
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
