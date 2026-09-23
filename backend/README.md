# FakeMiner · backend

API em Spring Boot responsável pela coleta de métricas, simulação do minerador, máquina de estados dos alertas e bot do Telegram. Visão geral, arquitetura e endpoints estão no [README principal](../README.md).

## Configuração

| Variável | Padrão | Uso |
|---|---|---|
| `TELEGRAM_BOT_HABILITADO` | `true` | `false` desliga o bot e as notificações |
| `TELEGRAM_BOT_TOKEN` | | Token gerado pelo BotFather |
| `TELEGRAM_BOT_USERNAME` | | Username do bot |
| `TELEGRAM_CHAT_ID` | | Único chat autorizado a receber alertas e enviar comandos |
| `SERVER_PORT` | `8080` | Porta HTTP |

As variáveis podem ficar em um arquivo `.env` na pasta `backend/` (veja `.env.example`). Os intervalos de coleta e de resumo ficam em `application.properties`.

## Pacotes

```
com.fakeminer
├── bot/          comandos recebidos pelo Telegram
├── config/       beans do OSHI e registro do bot
├── controller/   endpoints REST
├── model/        métricas, status do minerador e estados de alerta
├── scheduler/    coleta periódica e resumo por hora
└── service/      monitoramento, simulação, alertas e envio ao Telegram
```

## Comandos

```bash
./mvnw spring-boot:run
./mvnw verify
```
