/**
 * ROTA IQ — SCRIPTS DA PÁGINA PÚBLICA DE VENDAS (LANDING PAGE)
 * Inclui:
 * 1. Simulador Interativo de Corridas com HUD e Voz TTS
 * 2. Calculadora Dinâmica de Economia & Lucro Real
 * 3. Alternador de Preços (Mensal / Anual)
 * 4. FAQ Interativo
 * 5. Modal de Checkout PIX & Download APK
 */

// ==========================================================================
// 1. DADOS DE SIMULAÇÃO DE CORRIDAS (HUD)
// ==========================================================================
const OFFERS_DATA = {
    uber_good: {
        platform: "UBERX",
        fare: "R$ 38,50",
        profit: "R$ 27,40",
        hourly: "R$ 82,20",
        km: "R$ 2,88",
        score: "SCORE 94",
        scoreColor: "#00E676",
        reason: "✓ Excelente margem líquida (+65%) e R$ 82/h limpo no seu bolso.",
        tts: "Corrida Excelente! Sobra 27 reais limpo, 82 reais por hora trabalhada."
    },
    deadhead_trap: {
        platform: "UBER COMFORT",
        fare: "R$ 52,00",
        profit: "R$ 11,20",
        hourly: "R$ 14,00",
        km: "R$ 0,78",
        score: "ALERTA DEADHEAD",
        scoreColor: "#FF334B",
        reason: "⚠️ Destino em Zona Morta. Volta vazia estimada de 24 km destrói o lucro!",
        tts: "Cuidado! Armadilha de volta vazia. Região sem passageiros derruba seu ganho para 14 reais por hora."
    },
    indrive_counter: {
        platform: "INDRIVE",
        fare: "R$ 19,00",
        profit: "R$ 24,00 (Contraproposta)",
        hourly: "R$ 48,00",
        km: "R$ 2,45",
        score: "CONTRAPROPOSTA",
        scoreColor: "#FFD600",
        reason: "★ Passageiro ofereceu R$ 19. Faça contraproposta de R$ 25 (+R$ 6) para lucrar bem!",
        tts: "Oferta baixa do passageiro. Contraproposta recomendada: pedir 25 reais."
    },
    uber_loss: {
        platform: "UBERX",
        fare: "R$ 10,50",
        profit: "R$ 1,80",
        hourly: "R$ 4,50",
        km: "R$ 0,42",
        score: "RECUSA CRÍTICA",
        scoreColor: "#FF334B",
        reason: "⛔ Paga para trabalhar! Trânsito pesado e 14 km consumirão todo o combustível.",
        tts: "Recuse imediatamente. Esta corrida dá prejuízo e paga apenas 4 reais por hora."
    }
};

let currentTtsEnabled = false;

function setSimulationOffer(type) {
    const data = OFFERS_DATA[type];
    if (!data) return;

    const hudBadge = document.getElementById("hud-score-badge");
    const hudPlatform = document.getElementById("hud-platform-tag");
    const hudProfit = document.getElementById("hud-profit-val");
    const hudHourly = document.getElementById("hud-hourly-val");
    const hudKm = document.getElementById("hud-km-val");
    const hudReason = document.getElementById("hud-reason-text");
    const hudCard = document.getElementById("hud-overlay");

    if (hudBadge) {
        hudBadge.innerText = data.score;
        hudBadge.style.background = data.scoreColor;
    }
    if (hudPlatform) hudPlatform.innerText = data.platform;
    if (hudProfit) {
        hudProfit.innerText = data.profit;
        hudProfit.style.color = data.scoreColor;
    }
    if (hudHourly) hudHourly.innerText = data.hourly;
    if (hudKm) hudKm.innerText = data.km;
    if (hudReason) hudReason.innerText = data.reason;
    if (hudCard) {
        hudCard.style.borderColor = data.scoreColor;
        hudCard.style.boxShadow = `0 10px 30px rgba(0,0,0,0.6), 0 0 25px ${data.scoreColor}40`;
    }

    // Vocalização auditiva TTS se habilitada
    const ttsToggle = document.getElementById("chk-voice-tts");
    if (ttsToggle && ttsToggle.checked && 'speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(data.tts);
        utterance.lang = "pt-BR";
        utterance.rate = 1.1;
        window.speechSynthesis.speak(utterance);
    }
}

// ==========================================================================
// 2. CALCULADORA DINÂMICA DE ECONOMIA
// ==========================================================================
function updateProfitCalculator() {
    const kmPerDay = parseFloat(document.getElementById("calc-km").value) || 180;
    const kmPerLiter = parseFloat(document.getElementById("calc-consumption").value) || 10.5;
    const fuelPrice = parseFloat(document.getElementById("calc-fuel-price").value) || 5.89;
    const daysWorked = parseFloat(document.getElementById("calc-days").value) || 24;

    // Displays
    document.getElementById("disp-km").innerText = `${kmPerDay} km`;
    document.getElementById("disp-consumption").innerText = `${kmPerLiter.toFixed(1)} km/l`;
    document.getElementById("disp-fuel-price").innerText = `R$ ${fuelPrice.toFixed(2).replace('.', ',')}`;
    document.getElementById("disp-days").innerText = `${daysWorked} dias`;

    // Custo do km só combustível
    const fuelCostPerKm = fuelPrice / kmPerLiter;
    // Custo de manutenção preventiva estimada (pneu, óleo, freio, suspensão)
    const wearCostPerKm = 0.22;
    const totalCostPerKm = fuelCostPerKm + wearCostPerKm;

    // Km mensal rodado
    const totalMonthlyKm = kmPerDay * daysWorked;
    
    // Média de economia recusando 22% de corridas deficitárias / volta vazia
    // e convertendo em corridas eficientes
    const estimatedSavedWastedKm = totalMonthlyKm * 0.22;
    const monthlyFuelSaved = estimatedSavedWastedKm * fuelCostPerKm;
    
    // Lucro adicional por hora e contrapropostas inDrive
    const extraProfitFromGoodRides = (daysWorked * 42.0); // Média de +R$ 42 limpos por dia
    const totalMonthlyExtra = monthlyFuelSaved + extraProfitFromGoodRides;

    const formattedProfit = totalMonthlyExtra.toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    document.getElementById("calc-extra-profit").innerText = `+ R$ ${formattedProfit}`;
    document.getElementById("calc-cost-per-km").innerText = `R$ ${totalCostPerKm.toFixed(2).replace('.', ',')}`;
    document.getElementById("calc-fuel-saved").innerText = `R$ ${monthlyFuelSaved.toFixed(0)}`;
}

// ==========================================================================
// 3. ALTERNADOR DE PREÇOS (MENSAL VS ANUAL)
// ==========================================================================
function togglePricing(isAnnual) {
    const pricePro = document.getElementById("price-pro-val");
    const periodPro = document.getElementById("price-pro-period");
    const proSubtext = document.getElementById("pro-subtext");
    const labelMonthly = document.getElementById("label-monthly");
    const labelAnnual = document.getElementById("label-annual");

    if (isAnnual) {
        if (pricePro) pricePro.innerText = "19,99";
        if (periodPro) periodPro.innerText = "/mês (cobrado R$ 239,90/ano)";
        if (proSubtext) proSubtext.innerText = "Economia de 33% • Menos de R$ 0,66 por dia";
        if (labelAnnual) labelAnnual.classList.add("active");
        if (labelMonthly) labelMonthly.classList.remove("active");
    } else {
        if (pricePro) pricePro.innerText = "29,90";
        if (periodPro) periodPro.innerText = "/mês (sem fidelidade)";
        if (proSubtext) proSubtext.innerText = "Cancele quando quiser • Menos de R$ 1,00/dia";
        if (labelMonthly) labelMonthly.classList.add("active");
        if (labelAnnual) labelAnnual.classList.remove("active");
    }
}

// ==========================================================================
// 4. FAQ ACCORDION
// ==========================================================================
function toggleFaq(button) {
    const item = button.closest('.faq-item');
    const isActive = item.classList.contains('active');
    
    // Fechar outros
    document.querySelectorAll('.faq-item').forEach(el => {
        el.classList.remove('active');
    });

    if (!isActive) {
        item.classList.add('active');
    }
}

// ==========================================================================
// 5. MODAL DE CHECKOUT PIX
// ==========================================================================
function openPixModal(planName, priceStr) {
    const modal = document.getElementById("modal-pix-checkout");
    document.getElementById("pix-plan-name").innerText = planName;
    document.getElementById("pix-plan-price").innerText = priceStr;
    
    // Gera código PIX Copia e Cola simulado / QR
    const randomPixCode = `00020126580014BR.GOV.BCB.PIX0136${Math.random().toString(36).substring(2, 15)}520400005303986540${priceStr.replace(/[^0-9]/g, '')}5802BR5913ROTA_IQ_PAG6009SAO_PAULO62070503***6304E8A2`;
    document.getElementById("pix-copy-input").value = randomPixCode;

    if (modal) modal.style.display = "flex";
}

function closePixModal() {
    const modal = document.getElementById("modal-pix-checkout");
    if (modal) modal.style.display = "none";
}

function copyPixCode() {
    const input = document.getElementById("pix-copy-input");
    input.select();
    input.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(input.value);

    const btn = document.getElementById("btn-copy-pix");
    const originalText = btn.innerText;
    btn.innerText = "✓ CÓDIGO PIX COPIADO!";
    btn.style.background = "#00E676";
    btn.style.color = "#000";

    setTimeout(() => {
        btn.innerText = originalText;
        btn.style.background = "";
        btn.style.color = "";
    }, 2500);
}

// Fechar modal ao clicar fora
window.addEventListener("click", (e) => {
    const modal = document.getElementById("modal-pix-checkout");
    if (e.target === modal) {
        closePixModal();
    }
});

// Inicialização automática
document.addEventListener("DOMContentLoaded", () => {
    updateProfitCalculator();
    
    // Simulação inicial
    setTimeout(() => {
        setSimulationOffer('uber_good');
    }, 600);
});
