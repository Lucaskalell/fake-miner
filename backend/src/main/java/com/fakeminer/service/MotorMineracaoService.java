package com.fakeminer.service;

import com.fakeminer.model.StatusMineracao;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MotorMineracaoService {

    private static final double HASH_RATE_BASE_MHS = 50.0;
    private static final double VARIACAO_HASH_RATE = 0.05;
    private static final double CONSUMO_BASE_WATTS = 120.0;
    private static final double VARIACAO_CONSUMO = 0.10;
    private static final double LUCRO_POR_MH = 0.00005;
    private static final double META_DIARIA_DOLAR = 5.0;

    private boolean ativo = false;
    private LocalDateTime inicioSessao;
    private double totalHashGerado = 0;

    public synchronized void ligar() {
        if (ativo) return;
        ativo = true;
        inicioSessao = LocalDateTime.now();
        totalHashGerado = 0;
    }

    public synchronized void desligar() {
        ativo = false;
    }

    public synchronized void alternarEstado() {
        if (ativo) {
            desligar();
        } else {
            ligar();
        }
    }

    public synchronized boolean isAtivo() {
        return ativo;
    }

    public synchronized StatusMineracao gerarStatusAtual() {
        if (!ativo) {
            return StatusMineracao.builder().ativo(false).build();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double hashRateAtual = HASH_RATE_BASE_MHS * (1 + random.nextDouble(-VARIACAO_HASH_RATE, VARIACAO_HASH_RATE));
        double consumoAtual = CONSUMO_BASE_WATTS * (1 + random.nextDouble(-VARIACAO_CONSUMO, VARIACAO_CONSUMO));

        totalHashGerado += hashRateAtual;
        double lucroEstimado = totalHashGerado * LUCRO_POR_MH;

        return StatusMineracao.builder()
                .ativo(true)
                .hashRateAtual(hashRateAtual)
                .hashRateMedio(HASH_RATE_BASE_MHS)
                .consumoWatts(consumoAtual)
                .eficiencia(hashRateAtual / consumoAtual)
                .lucroEstimadoDolar(lucroEstimado)
                .progressoMetaDiaria(Math.min(100, lucroEstimado / META_DIARIA_DOLAR * 100))
                .inicioSessao(inicioSessao)
                .build();
    }
}
