package com.fakeminer.service;

import com.fakeminer.model.StatusMineracao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MotorMineracaoServiceTest {

    private final MotorMineracaoService motor = new MotorMineracaoService();

    @Test
    void deveRetornarStatusVazioQuandoDesligado() {
        StatusMineracao status = motor.gerarStatusAtual();

        assertThat(status.isAtivo()).isFalse();
        assertThat(status.getHashRateAtual()).isZero();
    }

    @Test
    void deveGerarHashRateDentroDaVariacaoPermitidaQuandoLigado() {
        motor.ligar();

        for (int i = 0; i < 100; i++) {
            StatusMineracao status = motor.gerarStatusAtual();
            assertThat(status.getHashRateAtual()).isBetween(47.5, 52.5);
            assertThat(status.getConsumoWatts()).isBetween(108.0, 132.0);
        }
    }

    @Test
    void deveAcumularLucroEntreColetas() {
        motor.ligar();
        double primeiroLucro = motor.gerarStatusAtual().getLucroEstimadoDolar();
        double segundoLucro = motor.gerarStatusAtual().getLucroEstimadoDolar();

        assertThat(segundoLucro).isGreaterThan(primeiroLucro);
    }

    @Test
    void desligarDeveSerIdempotente() {
        motor.desligar();
        motor.desligar();

        assertThat(motor.isAtivo()).isFalse();
    }

    @Test
    void alternarDeveInverterEstado() {
        motor.alternarEstado();
        assertThat(motor.isAtivo()).isTrue();

        motor.alternarEstado();
        assertThat(motor.isAtivo()).isFalse();
    }
}
