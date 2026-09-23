package com.fakeminer.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record AlertaHistoricoItem(
        String mensagem,
        String severidade,
        LocalDateTime timestamp
) {
    public static AlertaHistoricoItem of(String mensagem, String severidade) {
        return new AlertaHistoricoItem(mensagem, severidade,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
