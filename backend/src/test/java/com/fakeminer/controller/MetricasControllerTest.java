package com.fakeminer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "telegram.bot.habilitado=false")
@AutoConfigureMockMvc
class MetricasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarSnapshotComHardwareEMineracao() throws Exception {
        mockMvc.perform(get("/api/v1/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hardware.memoriaTotalGb").isNumber())
                .andExpect(jsonPath("$.mineracao.ativo").isBoolean());
    }

    @Test
    void alternarDeveRetornarNovoStatusDoMinerador() throws Exception {
        mockMvc.perform(post("/api/v1/minerador/alternar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").isBoolean());
    }

    @Test
    void deveRetornarHistoricoDeAlertas() throws Exception {
        mockMvc.perform(get("/api/v1/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
