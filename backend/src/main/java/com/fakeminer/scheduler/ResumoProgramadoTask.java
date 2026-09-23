package com.fakeminer.scheduler;

import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import com.fakeminer.service.MonitoramentoHardwareService;
import com.fakeminer.service.MotorMineracaoService;
import com.fakeminer.service.TelegramNotificacaoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ResumoProgramadoTask {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final TelegramNotificacaoService telegramService;

    public ResumoProgramadoTask(MonitoramentoHardwareService monitoramentoService,
                                MotorMineracaoService motorMineracao,
                                TelegramNotificacaoService telegramService) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.telegramService = telegramService;
    }

    @Scheduled(fixedRateString = "${fakeminer.resumo.intervalo-ms}",
               initialDelayString = "${fakeminer.resumo.intervalo-ms}")
    public void enviarResumo() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();
        telegramService.enviarMensagem(montarResumo(hardware, mineracao));
    }

    private String montarResumo(MetricasHardware hardware, StatusMineracao mineracao) {
        Duration uptime = Duration.ofSeconds(hardware.getUptimeSegundos());
        double metaRestante = Math.max(0, 100.0 - mineracao.getProgressoMetaDiaria());

        return String.format("""
                        RESUMO PERIÓDICO

                        MINERADOR
                        Status: %s
                        Hash rate: %.2f MH/s
                        Consumo: %.2f W
                        Lucro acumulado: $%.6f
                        Meta diária: %.2f%% (faltam %.2f%%)

                        MÁQUINA
                        CPU: %.2f%% | %.2f GHz
                        Temperatura: %.1f °C
                        RAM: %.2f%% (%.2f GB / %.2f GB)
                        Uptime: %dh %dmin""",
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar(),
                mineracao.getProgressoMetaDiaria(),
                metaRestante,
                hardware.getUsoCpu(),
                hardware.getFrequenciaCpuGhz(),
                hardware.getTemperaturaCpu(),
                hardware.getPercentualMemoriaUsada(),
                hardware.getMemoriaUsadaGb(),
                hardware.getMemoriaTotalGb(),
                uptime.toHours(),
                uptime.toMinutesPart());
    }
}
