package com.fakeminer.service;

import com.fakeminer.model.AlertaHistoricoItem;
import com.fakeminer.model.EstadoAlerta;
import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Service
public class AlertaService {

    private static final double LIMITE_TEMPERATURA = 85.0;
    private static final double LIMITE_CPU = 90.0;
    private static final double LIMITE_RAM = 85.0;
    private static final int COLETAS_PARA_CONFIRMAR_ALERTA = 3;
    private static final int COLETAS_POR_ESCALONAMENTO = 5;
    private static final int TAMANHO_MAXIMO_HISTORICO = 100;

    private final TelegramNotificacaoService telegramService;
    private final MotorMineracaoService motorMineracao;
    private final Deque<AlertaHistoricoItem> historico = new ArrayDeque<>();

    private EstadoAlerta estadoAtual = EstadoAlerta.NORMAL;
    private int coletasCriticasConsecutivas = 0;

    public AlertaService(TelegramNotificacaoService telegramService, MotorMineracaoService motorMineracao) {
        this.telegramService = telegramService;
        this.motorMineracao = motorMineracao;
    }

    public synchronized void avaliar(MetricasHardware hardware, StatusMineracao mineracao) {
        boolean metricaCritica = isCritico(hardware);

        if (!metricaCritica) {
            if (estadoAtual == EstadoAlerta.NORMAL) {
                coletasCriticasConsecutivas = 0;
            } else {
                voltarAoNormal();
            }
            return;
        }

        switch (estadoAtual) {
            case NORMAL -> {
                if (++coletasCriticasConsecutivas >= COLETAS_PARA_CONFIRMAR_ALERTA) {
                    estadoAtual = EstadoAlerta.ALERTA_1;
                    coletasCriticasConsecutivas = 0;
                    registrarAlerta(montarPrimeiroAlerta(hardware, mineracao), "CRITICO");
                }
            }
            case ALERTA_1, VIGILANCIA -> {
                if (atingiuEscalonamento()) {
                    estadoAtual = estadoAtual == EstadoAlerta.ALERTA_1
                            ? EstadoAlerta.VIGILANCIA
                            : EstadoAlerta.ALERTA_2;
                    registrarAlerta(montarAtualizacaoVigilancia(hardware), "AVISO");
                }
            }
            case ALERTA_2 -> {
                if (atingiuEscalonamento()) {
                    estadoAtual = EstadoAlerta.ALERTA_3;
                    registrarAlerta(montarAlertaFinal(), "CRITICO");
                }
            }
            case ALERTA_3, DESLIGADO -> {
            }
        }
    }

    public synchronized String ativarVigilanciaConstante() {
        estadoAtual = EstadoAlerta.VIGILANCIA;
        coletasCriticasConsecutivas = 0;
        return "Vigilância constante ativada. Atualizações a cada 25 segundos enquanto houver métrica crítica.";
    }

    public synchronized String desligarMinerador() {
        estadoAtual = EstadoAlerta.DESLIGADO;
        motorMineracao.desligar();
        adicionarAoHistorico("Minerador desligado via comando do Telegram.", "INFO");
        return "Minerador desligado com segurança.";
    }

    public synchronized List<AlertaHistoricoItem> buscarHistorico() {
        return List.copyOf(historico);
    }

    public synchronized EstadoAlerta getEstadoAtual() {
        return estadoAtual;
    }

    private boolean isCritico(MetricasHardware hardware) {
        return hardware.getTemperaturaCpu() > LIMITE_TEMPERATURA
                || hardware.getUsoCpu() > LIMITE_CPU
                || hardware.getPercentualMemoriaUsada() > LIMITE_RAM;
    }

    private boolean atingiuEscalonamento() {
        coletasCriticasConsecutivas++;
        return coletasCriticasConsecutivas % COLETAS_POR_ESCALONAMENTO == 0;
    }

    private void voltarAoNormal() {
        estadoAtual = EstadoAlerta.NORMAL;
        coletasCriticasConsecutivas = 0;
        registrarAlerta("Métricas normalizadas. Sistema voltou ao estado normal.", "INFO");
    }

    private String montarPrimeiroAlerta(MetricasHardware hardware, StatusMineracao mineracao) {
        return String.format("""
                        ALERTA: MÉTRICA CRÍTICA DETECTADA

                        Temperatura: %.1f °C
                        CPU: %.2f%%
                        RAM: %.2f%%

                        Minerador: %s
                        Hash rate: %.2f MH/s
                        Consumo: %.2f W
                        Lucro acumulado: $%.6f

                        Responda com:
                        /vigilancia - ativar vigilância constante
                        /desligar - desligar o minerador""",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu(),
                hardware.getPercentualMemoriaUsada(),
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar());
    }

    private String montarAtualizacaoVigilancia(MetricasHardware hardware) {
        return String.format("""
                        VIGILÂNCIA: ATUALIZAÇÃO

                        Temperatura: %.1f °C
                        CPU: %.2f%%
                        RAM: %.2f%%

                        Responda com:
                        /vigilancia - manter vigilância
                        /desligar - desligar o minerador""",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu(),
                hardware.getPercentualMemoriaUsada());
    }

    private String montarAlertaFinal() {
        return """
                ALERTA CRÍTICO: terceiro aviso consecutivo.

                Sistema em risco. Ação necessária:
                /desligar - desligar o minerador agora""";
    }

    private void registrarAlerta(String mensagem, String severidade) {
        adicionarAoHistorico(mensagem, severidade);
        telegramService.enviarMensagem(mensagem);
    }

    private void adicionarAoHistorico(String mensagem, String severidade) {
        historico.addLast(AlertaHistoricoItem.of(mensagem, severidade));
        if (historico.size() > TAMANHO_MAXIMO_HISTORICO) {
            historico.removeFirst();
        }
    }
}
