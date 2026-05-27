/**
 * auction.js — Lances em tempo real via WebSocket (SockJS + STOMP)
 * Depende das variáveis globais definidas no template:
 *   ITEM_ID  — Long: id do item
 *   IS_ACTIVE — boolean: se o leilão está ATIVO
 *   END_DATE  — String ISO (yyyy-MM-ddTHH:mm:ss): data de encerramento
 */

(function () {
    'use strict';

    // ── Referências do DOM ────────────────────────────────────────
    const bidForm      = document.getElementById('bid-form');
    const bidAmountInput = document.getElementById('bid-amount');
    const currentPriceEl = document.getElementById('current-price');
    const bidMessages  = document.getElementById('bid-messages');
    const bidsTbody    = document.getElementById('bids-tbody');
    const countdownEl  = document.getElementById('countdown');

    // ── Formatador de moeda (pt-BR) ───────────────────────────────
    function formatCurrency(value) {
        return 'R$ ' + Number(value).toLocaleString('pt-BR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    // ── Exibir mensagem de feedback ───────────────────────────────
    function showMessage(text, type) {
        if (!bidMessages) return;
        bidMessages.innerHTML =
            '<div class="msg-' + type + '">' + text + '</div>';
        if (type === 'error') {
            setTimeout(function () {
                bidMessages.innerHTML = '';
            }, 4000);
        }
    }

    // ── Adicionar linha no topo da tabela de lances ───────────────
    function prependBidRow(bidderName, amount, timestamp) {
        if (!bidsTbody) return;

        // Remove a linha "Nenhum lance ainda" se existir
        const emptyRow = bidsTbody.querySelector('.no-bids');
        if (emptyRow) emptyRow.parentElement.remove();

        const tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + timestamp + '</td>' +
            '<td>' + bidderName + '</td>' +
            '<td>' + formatCurrency(amount) + '</td>';

        bidsTbody.insertBefore(tr, bidsTbody.firstChild);
    }

    // ── Exibir banner de encerramento ─────────────────────────────
    function showClosedBanner(winnerName) {
        // Esconde o formulário de lance
        if (bidForm) bidForm.style.display = 'none';

        // Esconde o timer
        const timerBox = document.querySelector('.timer-box');
        if (timerBox) timerBox.style.display = 'none';

        // Para a contagem regressiva
        if (countdownTimerId) clearInterval(countdownTimerId);
        if (countdownEl) countdownEl.textContent = 'Encerrado';

        // Exibe banner
        const banner = document.createElement('div');
        banner.className = 'banner banner-encerrado';
        banner.innerHTML = '🏁 Leilão Encerrado' +
            (winnerName ? ' — Vencedor: <strong>' + winnerName + '</strong>' : ' — Sem lances');

        const infoPanel = document.querySelector('.info-panel');
        if (infoPanel) infoPanel.appendChild(banner);
    }

    // ── Timer de contagem regressiva ──────────────────────────────
    var countdownTimerId = null;

    function startCountdown() {
        if (!countdownEl || !END_DATE) return;

        var endTime = new Date(END_DATE).getTime();

        function tick() {
            var now  = new Date().getTime();
            var diff = endTime - now;

            if (diff <= 0) {
                countdownEl.textContent = 'Encerrado';
                clearInterval(countdownTimerId);
                return;
            }

            var h = Math.floor(diff / 3600000);
            var m = Math.floor((diff % 3600000) / 60000);
            var s = Math.floor((diff % 60000) / 1000);

            countdownEl.textContent =
                String(h).padStart(2, '0') + ':' +
                String(m).padStart(2, '0') + ':' +
                String(s).padStart(2, '0');
        }

        tick();
        countdownTimerId = setInterval(tick, 1000);
    }

    // ── Conexão WebSocket via SockJS + STOMP ──────────────────────
    function connectWebSocket() {
        var socket = new SockJS('/ws');
        var stompClient = Stomp.over(socket);

        // Silencia logs do STOMP no console
        stompClient.debug = null;

        stompClient.connect({}, function () {
            wsConnected = true;
            // Inscrever no tópico do leilão apenas se estiver ATIVO
            if (IS_ACTIVE) {
                stompClient.subscribe('/topic/auction/' + ITEM_ID, function (frame) {
                    handleMessage(JSON.parse(frame.body), stompClient);
                });
            }
        }, function (error) {
            wsConnected = false;
            console.warn('WebSocket desconectado — usando modo HTTP:', error);
        });

        return stompClient;
    }

    // ── Processar mensagem recebida ───────────────────────────────
    function handleMessage(data, stompClient) {
        if (data.closed) {
            // Leilão encerrado
            showClosedBanner(data.winnerName || null);
            return;
        }

        if (data.success) {
            // Lance bem-sucedido: atualiza preço e adiciona à tabela
            if (currentPriceEl) {
                currentPriceEl.textContent = formatCurrency(data.newCurrentPrice);
            }
            prependBidRow(data.bidderName, data.amount, data.timestamp);
            showMessage('', 'success'); // Limpa mensagens anteriores
        } else {
            // Lance inválido: exibe erro
            showMessage(data.errorMessage || 'Erro ao processar o lance.', 'error');
        }
    }

    // ── Interceptar submit do formulário ─────────────────────────
    // Se o WebSocket estiver conectado usa-o (experiência tempo real).
    // Caso contrário, deixa o form submeter normalmente via HTTP POST
    // (funciona sempre, inclusive via ngrok / celular / conexões restritas).
    var wsConnected = false;

    function setupBidForm(stompClient) {
        if (!bidForm) return;

        bidForm.addEventListener('submit', function (e) {
            if (!wsConnected) {
                // Fallback: deixa o form submeter normalmente via HTTP
                return;
            }

            e.preventDefault();

            var raw = bidAmountInput ? bidAmountInput.value.trim() : '';
            var amount = parseFloat(raw);

            if (!raw || isNaN(amount) || amount <= 0) {
                showMessage('Informe um valor de lance válido.', 'error');
                return;
            }

            stompClient.send('/app/bid', {}, JSON.stringify({
                itemId: ITEM_ID,
                amount: amount
            }));

            if (bidAmountInput) bidAmountInput.value = '';
        });
    }

    // ── Inicialização ─────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', function () {
        // Inicia timer
        if (IS_ACTIVE) {
            startCountdown();
        }

        // Conecta WebSocket e configura form
        var stompClient = connectWebSocket();
        setupBidForm(stompClient);
    });

})();
