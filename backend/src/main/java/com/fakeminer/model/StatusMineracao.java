package com.fakeminer.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StatusMineracao {
    private boolean ativo;
    private double hashRateAtual;
    private double hashRateMedio;
    private double consumoWatts;
    private double eficiencia;
    private double lucroEstimadoDolar;
    private double progressoMetaDiaria;
    private LocalDateTime inicioSessao;
}
