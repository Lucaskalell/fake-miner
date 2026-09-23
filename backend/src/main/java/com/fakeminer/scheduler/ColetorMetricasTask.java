package com.fakeminer.scheduler;

import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import com.fakeminer.service.AlertaService;
import com.fakeminer.service.MonitoramentoHardwareService;
import com.fakeminer.service.MotorMineracaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ColetorMetricasTask {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;

    public ColetorMetricasTask(MonitoramentoHardwareService monitoramentoService,
                               MotorMineracaoService motorMineracao,
                               AlertaService alertaService) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ligarMineradorAoIniciar() {
        motorMineracao.ligar();
    }

    @Scheduled(fixedRateString = "${fakeminer.coleta.intervalo-ms}")
    public void executarColeta() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();

        registrarLog(hardware, mineracao);
        alertaService.avaliar(hardware, mineracao);
    }

    private void registrarLog(MetricasHardware hardware, StatusMineracao mineracao) {
        log.info("Hardware | CPU {}% @ {} GHz | Temp {} °C | RAM {}% ({}/{} GB)",
                formatar(hardware.getUsoCpu()),
                formatar(hardware.getFrequenciaCpuGhz()),
                formatar(hardware.getTemperaturaCpu()),
                formatar(hardware.getPercentualMemoriaUsada()),
                formatar(hardware.getMemoriaUsadaGb()),
                formatar(hardware.getMemoriaTotalGb()));

        if (!mineracao.isAtivo()) {
            log.info("Minerador | DESLIGADO");
            return;
        }

        log.info("Minerador | {} MH/s | {} W | lucro ${} | meta {}%",
                formatar(mineracao.getHashRateAtual()),
                formatar(mineracao.getConsumoWatts()),
                String.format("%.6f", mineracao.getLucroEstimadoDolar()),
                formatar(mineracao.getProgressoMetaDiaria()));
    }

    private String formatar(double valor) {
        return String.format("%.2f", valor);
    }
}
