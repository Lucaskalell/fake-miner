package com.fakeminer.service;

import com.fakeminer.bot.FakeMinerBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramNotificacaoService {

    private final ObjectProvider<FakeMinerBot> bot;
    private final boolean habilitado;
    private final String chatId;

    public TelegramNotificacaoService(ObjectProvider<FakeMinerBot> bot,
                                      @Value("${telegram.bot.habilitado}") boolean habilitado,
                                      @Value("${telegram.bot.chat-id}") String chatId) {
        this.bot = bot;
        this.habilitado = habilitado;
        this.chatId = chatId;
    }

    public void enviarMensagem(String texto) {
        if (!habilitado) {
            log.info("Telegram desabilitado. Mensagem não enviada: {}", texto.lines().findFirst().orElse(""));
            return;
        }
        try {
            bot.getObject().execute(new SendMessage(chatId, texto));
        } catch (TelegramApiException e) {
            log.error("Falha ao enviar mensagem no Telegram: {}", e.getMessage());
        }
    }
}
