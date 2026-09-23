package com.fakeminer.service;

import com.fakeminer.model.EstadoAlerta;
import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AlertaServiceTest {

    private static final MetricasHardware HARDWARE_NORMAL = hardware(50, 30, 40);
    private static final MetricasHardware HARDWARE_CRITICO = hardware(90, 95, 40);
    private static final StatusMineracao MINERACAO = StatusMineracao.builder().ativo(true).build();

    private TelegramNotificacaoService telegram;
    private MotorMineracaoService motor;
    private AlertaService alertaService;

    @BeforeEach
    void configurar() {
        telegram = mock(TelegramNotificacaoService.class);
        motor = new MotorMineracaoService();
        motor.ligar();
        alertaService = new AlertaService(telegram, motor);
    }

    @Test
    void naoDeveAlertarComMetricasNormais() {
        alertaService.avaliar(HARDWARE_NORMAL, MINERACAO);

        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.NORMAL);
        assertThat(alertaService.buscarHistorico()).isEmpty();
        verify(telegram, never()).enviarMensagem(anyString());
    }

    @Test
    void picoIsoladoNaoDeveGerarAlerta() {
        avaliarVezes(HARDWARE_CRITICO, 2);
        alertaService.avaliar(HARDWARE_NORMAL, MINERACAO);
        avaliarVezes(HARDWARE_CRITICO, 2);

        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.NORMAL);
        assertThat(alertaService.buscarHistorico()).isEmpty();
        verify(telegram, never()).enviarMensagem(anyString());
    }

    @Test
    void tresColetasCriticasSeguidasDevemGerarAlerta1() {
        avaliarVezes(HARDWARE_CRITICO, 3);

        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.ALERTA_1);
        assertThat(alertaService.buscarHistorico()).singleElement()
                .satisfies(item -> assertThat(item.severidade()).isEqualTo("CRITICO"));
        verify(telegram).enviarMensagem(anyString());
    }

    @Test
    void deveEscalonarAteAlerta3ComColetasCriticasConsecutivas() {
        avaliarVezes(HARDWARE_CRITICO, 3);
        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.ALERTA_1);

        avaliarVezes(HARDWARE_CRITICO, 5);
        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.VIGILANCIA);

        avaliarVezes(HARDWARE_CRITICO, 5);
        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.ALERTA_2);

        avaliarVezes(HARDWARE_CRITICO, 5);
        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.ALERTA_3);

        verify(telegram, times(4)).enviarMensagem(anyString());
    }

    @Test
    void deveVoltarAoNormalQuandoMetricasNormalizarem() {
        avaliarVezes(HARDWARE_CRITICO, 13);
        alertaService.avaliar(HARDWARE_NORMAL, MINERACAO);

        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.NORMAL);
        assertThat(alertaService.buscarHistorico().getLast().severidade()).isEqualTo("INFO");
    }

    @Test
    void desligarDeveManterMineradorDesligadoMesmoSeJaEstiverDesligado() {
        motor.desligar();

        alertaService.desligarMinerador();

        assertThat(motor.isAtivo()).isFalse();
        assertThat(alertaService.getEstadoAtual()).isEqualTo(EstadoAlerta.DESLIGADO);
    }

    @Test
    void historicoDeveManterNoMaximo100Itens() {
        for (int i = 0; i < 150; i++) {
            avaliarVezes(HARDWARE_CRITICO, 3);
            alertaService.avaliar(HARDWARE_NORMAL, MINERACAO);
        }

        assertThat(alertaService.buscarHistorico()).hasSize(100);
    }

    private void avaliarVezes(MetricasHardware hardware, int vezes) {
        for (int i = 0; i < vezes; i++) {
            alertaService.avaliar(hardware, MINERACAO);
        }
    }

    private static MetricasHardware hardware(double temperatura, double usoCpu, double percentualRam) {
        return MetricasHardware.builder()
                .temperaturaCpu(temperatura)
                .usoCpu(usoCpu)
                .percentualMemoriaUsada(percentualRam)
                .build();
    }
}
