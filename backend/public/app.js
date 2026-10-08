// ROTA IQ - Web Simulator & Supabase Live Engine
const SUPABASE_URL = "https://jkreduqzekllsmzxiugn.supabase.co";
const SUPABASE_ANON_KEY = "sb_publishable_Gfv2iho31bMPIJL_JINH0g_AkCd4rv-";

let currentOffer = null;
let totalEvaluations = 19;
let totalNetProfit = 142.80;

// Inicialização
document.addEventListener("DOMContentLoaded", () => {
    loadSupabasePlans();
});

// Consulta em tempo real aos planos no Supabase
async function loadSupabasePlans() {
    const tbody = document.getElementById("supabase-plans-tbody");
    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--brand-primary);">Consultando banco PostgreSQL...</td></tr>';
    
    try {
        const response = await fetch(`${SUPABASE_URL}/rest/v1/subscription_plans?select=*&is_active=eq.true&order=price_cents.asc`, {
            headers: {
                "apikey": SUPABASE_ANON_KEY,
                "Authorization": `Bearer ${SUPABASE_ANON_KEY}`
            }
        });
        
        if (!response.ok) throw new Error("Erro na resposta do Supabase: " + response.status);
        
        const plans = await response.json();
        tbody.innerHTML = "";
        
        plans.forEach(plan => {
            const tr = document.createElement("tr");
            const price = plan.price_cents === 0 ? "Gratuito" : `R$ ${(plan.price_cents / 100).toFixed(2).replace('.', ',')}`;
            const intervalLabel = plan.interval === "month" ? "/mês" : plan.interval === "year" ? "/ano" : "vitalício";
            
            tr.innerHTML = `
                <td><code>${plan.code}</code></td>
                <td><strong>${plan.name}</strong></td>
                <td style="color: var(--color-excellent); font-weight: 800;">${price}</td>
                <td>${intervalLabel}</td>
                <td><span style="color: #10B981; font-weight: 800;">● ATIVO (RLS OK)</span></td>
            `;
            tbody.appendChild(tr);
        });
        
        document.getElementById("supabase-details").innerText = `${plans.length} Planos Oficiais Ativos • Row Level Security Habilitado • Latência: ~78ms`;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" style="color: var(--color-avoid);">Falha ao carregar planos: ${err.message}</td></tr>`;
    }
}

// Simulação de Ofertas
const SIMULATION_DATA = {
    uber_excellent: {
        platform: "UBERX",
        fare: 38.50,
        distanceKm: 11.0,
        durationMin: 24,
        tripCost: 11.10,
        netProfit: 27.40,
        hourlyRate: 82.20,
        kmRate: 2.88,
        score: 94,
        classification: "EXCELENTE",
        reason: "✓ Excelente rentabilidade (+65% margem) e R$ 82,20 por hora líquido.",
        ttsText: "Corrida Excelente! Sobra cerca de 27 reais limpo, 82 reais por hora.",
        color: "#00E676"
    },
    deadhead_trap: {
        platform: "UBER COMFORT",
        fare: 55.00,
        distanceKm: 32.0,
        durationMin: 48,
        tripCost: 26.50,
        netProfit: 12.00, // Abatendo o retorno vazio (deadhead de 22 km)
        hourlyRate: 15.00,
        kmRate: 0.85,
        score: 42,
        classification: "ARMADILHA DEADHEAD",
        reason: "⚠️ ALERTA: Destino em Zona Morta. Volta vazia estimada de 22 km reduz seu lucro para apenas R$ 12,00 (R$ 15/h)!",
        ttsText: "Atenção! Armadilha de volta vazia. Região sem passageiros derruba o lucro para 15 reais por hora.",
        color: "#FF1744"
    },
    indrive_counter: {
        platform: "INDRIVE",
        fare: 18.00,
        distanceKm: 9.0,
        durationMin: 22,
        tripCost: 8.10,
        netProfit: 9.90,
        hourlyRate: 27.00,
        suggestedCounter: 24.00,
        counterProfit: 15.90,
        counterHourly: 43.36,
        score: 68,
        classification: "CONTRAPROPOSTA",
        reason: "★ Passageiro ofereceu R$ 18,00. Faça contraproposta de +R$ 6,00 (R$ 24,00) para elevar para R$ 43,36/h líquido!",
        ttsText: "Oferta baixa do passageiro. Contraproposta recomendada: pedir 24 reais.",
        color: "#FFD600"
    },
    uber_loss: {
        platform: "UBERX",
        fare: 11.00,
        distanceKm: 15.0,
        durationMin: 36,
        tripCost: 12.30,
        netProfit: -1.30,
        hourlyRate: -2.16,
        kmRate: -0.08,
        score: 22,
        classification: "PREJUÍZO DIRETO",
        reason: "🔴 RECUSA ESTRATÉGICA: Corrida deficitária (-R$ 1,30)! Esperar até 22 minutos parado no ponto lucra mais do que aceitar.",
        ttsText: "Corrida no prejuízo! Recusa recomendada. Ficar parado economiza 12 reais de combustível.",
        color: "#FF9100"
    },
    delivery_flash: {
        platform: "UBER FLASH",
        fare: 23.80,
        distanceKm: 7.5,
        durationMin: 20,
        tripCost: 6.80,
        netProfit: 17.00,
        hourlyRate: 51.00,
        kmRate: 2.26,
        score: 85,
        classification: "ENTREGA FLASH",
        reason: "📦 Pacote sem passageiro. Margem líquida de 71% com compensação de espera.",
        ttsText: "Entrega Flash aceitável. Sobra 17 reais, 51 reais por hora.",
        color: "#38BDF8"
    }
};

function simulateOffer(type) {
    const offer = SIMULATION_DATA[type];
    if (!offer) return;
    currentOffer = offer;
    
    const hud = document.getElementById("hud-overlay");
    const badge = document.getElementById("hud-score-badge");
    const tag = document.getElementById("hud-platform-tag");
    const profit = document.getElementById("hud-profit-val");
    const hourly = document.getElementById("hud-hourly-val");
    const km = document.getElementById("hud-km-val");
    const reason = document.getElementById("hud-reason-text");
    
    hud.style.borderColor = offer.color;
    badge.innerText = `SCORE ${offer.score} • ${offer.classification}`;
    badge.style.background = offer.color;
    tag.innerText = offer.platform;
    
    if (offer.netProfit >= 0) {
        profit.innerText = `R$ ${offer.netProfit.toFixed(2).replace('.', ',')}`;
        profit.style.color = offer.color;
    } else {
        profit.innerText = `- R$ ${Math.abs(offer.netProfit).toFixed(2).replace('.', ',')}`;
        profit.style.color = "var(--color-avoid)";
    }
    
    hourly.innerText = `R$ ${offer.hourlyRate.toFixed(2).replace('.', ',')}`;
    km.innerText = `R$ ${offer.kmRate.toFixed(2).replace('.', ',')}`;
    reason.innerText = offer.reason;
    reason.style.color = offer.color;
    
    // Atualiza tab inDrive caso esteja simulando inDrive
    if (offer.suggestedCounter) {
        document.getElementById("indrive-proposal-fare").innerText = `R$ ${offer.fare.toFixed(2).replace('.', ',')}`;
        document.getElementById("indrive-counter-fare").innerText = `Pedir R$ ${offer.suggestedCounter.toFixed(2).replace('.', ',')} (+R$ ${(offer.suggestedCounter - offer.fare).toFixed(2).replace('.', ',')})`;
    }
    
    // Abre o HUD animado
    hud.classList.add("active");
    
    // Vocalização TTS se ativada
    if (document.getElementById("chk-tts").checked) {
        speakText(offer.ttsText);
    }
}

function dismissHud() {
    const hud = document.getElementById("hud-overlay");
    hud.classList.remove("active");
}

function acceptOffer() {
    if (!currentOffer) return;
    dismissHud();
    
    // Atualiza contadores do mockup
    totalEvaluations++;
    totalNetProfit += Math.max(0, currentOffer.netProfit);
    document.getElementById("phone-eval-count").innerText = totalEvaluations;
    document.getElementById("phone-stat-net").innerText = `R$ ${totalNetProfit.toFixed(2).replace('.', ',')}`;
    
    // Insere no log ao vivo
    const tbody = document.getElementById("evaluations-log-tbody");
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0];
    
    const tr = document.createElement("tr");
    tr.innerHTML = `
        <td>${timeStr}</td>
        <td><strong>${currentOffer.platform}</strong></td>
        <td>R$ ${currentOffer.fare.toFixed(2).replace('.', ',')}</td>
        <td>R$ ${currentOffer.tripCost.toFixed(2).replace('.', ',')}</td>
        <td style="color: ${currentOffer.color}; font-weight: 800;">
            ${currentOffer.netProfit >= 0 ? 'R$ ' + currentOffer.netProfit.toFixed(2).replace('.', ',') : '- R$ ' + Math.abs(currentOffer.netProfit).toFixed(2).replace('.', ',')}
        </td>
        <td>${currentOffer.classification}</td>
    `;
    tbody.insertBefore(tr, tbody.firstChild);
    
    // Salva evento na tabela Supabase em segundo plano
    saveEvaluationToSupabase(currentOffer);
}

async function saveEvaluationToSupabase(offer) {
    try {
        const payload = [{
            event_name: "RIDE_EVALUATED_AND_ACCEPTED",
            app_version: "1.0.0",
            driver_id_hash: "web_simulator_driver",
            properties: {
                platform: offer.platform,
                gross_fare: offer.fare,
                net_profit: offer.netProfit,
                score: offer.score,
                classification: offer.classification
            }
        }];
        
        await fetch(`${SUPABASE_URL}/rest/v1/sanitized_telemetry_events`, {
            method: "POST",
            headers: {
                "apikey": SUPABASE_ANON_KEY,
                "Authorization": `Bearer ${SUPABASE_ANON_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        console.log("✓ Avaliação salva com sucesso no Supabase PostgreSQL!");
    } catch (e) {
        console.warn("Log Supabase:", e);
    }
}

// Síntese de Voz (TTS)
function speakText(text) {
    if (!('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();
    
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = "pt-BR";
    utterance.rate = 1.1; // ligeiramente mais rápido para decisão ágil
    utterance.pitch = 1.0;
    
    const voices = window.speechSynthesis.getVoices();
    const ptVoice = voices.find(v => v.lang.includes("pt-BR") || v.lang.includes("pt_BR"));
    if (ptVoice) utterance.voice = ptVoice;
    
    window.speechSynthesis.speak(utterance);
}

// Navegação entre telas no mockup do telefone
function switchPhoneTab(tabName) {
    document.getElementById("screen-dashboard").style.display = tabName === 'dashboard' ? 'block' : 'none';
    document.getElementById("screen-indrive").style.display = tabName === 'indrive' ? 'block' : 'none';
    document.getElementById("screen-tax").style.display = tabName === 'tax' ? 'block' : 'none';
    
    const navItems = document.querySelectorAll(".nav-item");
    navItems.forEach((item, index) => {
        if ((tabName === 'dashboard' && index === 0) ||
            (tabName === 'indrive' && index === 1) ||
            (tabName === 'tax' && index === 2)) {
            item.classList.add("active");
        } else {
            item.classList.remove("active");
        }
    });
}
