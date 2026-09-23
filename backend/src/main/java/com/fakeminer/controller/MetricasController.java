package com.fakeminer.controller;

import com.fakeminer.model.AlertaHistoricoItem;
import com.fakeminer.model.MetricasHardware;
import com.fakeminer.model.StatusMineracao;
import com.fakeminer.service.AlertaService;
import com.fakeminer.service.MonitoramentoHardwareService;
import com.fakeminer.service.MotorMineracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
public class MetricasController {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;

    public MetricasController(MonitoramentoHardwareService monitoramentoService,
                              MotorMineracaoService motorMineracao,
                              AlertaService alertaService) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
    }

    @GetMapping("/metricas")
    public ResponseEntity<SnapshotSistema> buscarSnapshot() {
        return ResponseEntity.ok(new SnapshotSistema(
                monitoramentoService.coletarMetricas(),
                motorMineracao.gerarStatusAtual()));
    }

    @GetMapping("/metricas/hardware")
    public ResponseEntity<MetricasHardware> buscarMetricasHardware() {
        return ResponseEntity.ok(monitoramentoService.coletarMetricas());
    }

    @GetMapping("/metricas/minerador")
    public ResponseEntity<StatusMineracao> buscarStatusMinerador() {
        return ResponseEntity.ok(motorMineracao.gerarStatusAtual());
    }

    @PostMapping("/minerador/alternar")
    public ResponseEntity<StatusMineracao> alternarMinerador() {
        motorMineracao.alternarEstado();
        return ResponseEntity.ok(motorMineracao.gerarStatusAtual());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaHistoricoItem>> buscarAlertas() {
        return ResponseEntity.ok(alertaService.buscarHistorico());
    }

    public record SnapshotSistema(MetricasHardware hardware, StatusMineracao mineracao) {}
}
