package com.fakeminer.bot;

import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import com.fakeminer.service.AlertaService;
import com.fakeminer.service.MonitoramentoHardwareService;
import com.fakeminer.service.MotorMineracaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class FakeMinerBot extends TelegramLongPollingBot {

    private static final String AJUDA = """
            Comandos disponíveis:
            /status - estado completo do sistema
            /temps - temperatura e uso de CPU
            /vigilancia - ativa a vigilância constante
            /desligar - desliga o minerador""";

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;
    private final String username;
    private final String chatIdAutorizado;

    public FakeMinerBot(@Value("${telegram.bot.token}") String token,
                        @Value("${telegram.bot.username}") String username,
                        @Value("${telegram.bot.chat-id}") String chatIdAutorizado,
                        MonitoramentoHardwareService monitoramentoService,
                        MotorMineracaoService motorMineracao,
                        AlertaService alertaService) {
        super(token);
        this.username = username;
        this.chatIdAutorizado = chatIdAutorizado;
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String chatId = update.getMessage().getChatId().toString();
        if (!chatId.equals(chatIdAutorizado)) {
            log.warn("Comando ignorado de chat não autorizado: {}", chatId);
            return;
        }

        String resposta = switch (update.getMessage().getText().trim()) {
            case "/status" -> gerarMensagemStatus();
            case "/temps" -> gerarMensagemTemperatura();
            case "/vigilancia" -> alertaService.ativarVigilanciaConstante();
            case "/desligar" -> alertaService.desligarMinerador();
            default -> AJUDA;
        };

        enviarResposta(chatId, resposta);
    }

    private String gerarMensagemStatus() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();

        return String.format("""
                        STATUS DO SISTEMA

                        CPU: %.2f%% | %.2f GHz
                        Temperatura: %.1f °C
                        RAM: %.2f%% (%.2f GB / %.2f GB)

                        Minerador: %s
                        Hash rate: %.2f MH/s
                        Consumo: %.2f W
                        Lucro: $%.6f
                        Meta diária: %.2f%%""",
                hardware.getUsoCpu(),
                hardware.getFrequenciaCpuGhz(),
                hardware.getTemperaturaCpu(),
                hardware.getPercentualMemoriaUsada(),
                hardware.getMemoriaUsadaGb(),
                hardware.getMemoriaTotalGb(),
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar(),
                mineracao.getProgressoMetaDiaria());
    }

    private String gerarMensagemTemperatura() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        return String.format("Temperatura da CPU: %.1f °C%nUso de CPU: %.2f%%",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu());
    }

    private void enviarResposta(String chatId, String texto) {
        try {
            execute(new SendMessage(chatId, texto));
        } catch (TelegramApiException e) {
            log.error("Falha ao responder comando no Telegram: {}", e.getMessage());
        }
    }
}
