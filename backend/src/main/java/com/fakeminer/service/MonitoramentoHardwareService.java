package com.fakeminer.service;

import com.fakeminer.model.MetricasHardware;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MonitoramentoHardwareService {

    private static final double BYTES_POR_GB = 1024.0 * 1024.0 * 1024.0;
    private static final double HZ_POR_GHZ = 1_000_000_000.0;
    private static final long JANELA_MEDICAO_CPU_MS = 1000;

    private static final double TEMPERATURA_BASE = 45.0;
    private static final double TEMPERATURA_MAXIMA = 95.0;
    private static final double AUMENTO_TEMPERATURA_CARGA_TOTAL = 40.0;
    private static final double FATOR_AMORTECIMENTO = 0.1;

    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem sistemaOperacional;
    private double temperaturaAtual = TEMPERATURA_BASE;

    public MonitoramentoHardwareService(SystemInfo systemInfo) {
        this.hardware = systemInfo.getHardware();
        this.sistemaOperacional = systemInfo.getOperatingSystem();
    }

    public synchronized MetricasHardware coletarMetricas() {
        CentralProcessor processador = hardware.getProcessor();
        GlobalMemory memoria = hardware.getMemory();

        double usoCpu = processador.getSystemCpuLoad(JANELA_MEDICAO_CPU_MS) * 100;
        long memoriaTotal = memoria.getTotal();
        long memoriaUsada = memoriaTotal - memoria.getAvailable();

        return MetricasHardware.builder()
                .temperaturaCpu(calcularTemperaturaSimulada(usoCpu))
                .usoCpu(usoCpu)
                .frequenciaCpuGhz(calcularFrequenciaMediaGhz(processador))
                .memoriaUsadaGb(memoriaUsada / BYTES_POR_GB)
                .memoriaTotalGb(memoriaTotal / BYTES_POR_GB)
                .percentualMemoriaUsada((double) memoriaUsada / memoriaTotal * 100)
                .swapUsadaGb(memoria.getVirtualMemory().getSwapUsed() / BYTES_POR_GB)
                .uptimeSegundos(sistemaOperacional.getSystemUptime())
                .build();
    }

    private double calcularFrequenciaMediaGhz(CentralProcessor processador) {
        return Arrays.stream(processador.getCurrentFreq())
                .average()
                .orElse(0) / HZ_POR_GHZ;
    }

    private double calcularTemperaturaSimulada(double usoCpu) {
        double temperaturaAlvo = TEMPERATURA_BASE + usoCpu / 100.0 * AUMENTO_TEMPERATURA_CARGA_TOTAL;
        double ruido = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
        temperaturaAtual += (temperaturaAlvo - temperaturaAtual) * FATOR_AMORTECIMENTO + ruido;
        temperaturaAtual = Math.clamp(temperaturaAtual, TEMPERATURA_BASE, TEMPERATURA_MAXIMA);
        return temperaturaAtual;
    }
}
