# FakeMiner · mobile

App Flutter com gerenciamento de estado em BLoC. Tem três abas (painel, minerador e alertas) e consome a mesma API do painel web. Visão geral do projeto no [README principal](../README.md).

## Rodando

```bash
flutter pub get
flutter run --dart-define=API_URL=http://<ip-do-pc>:8080/api/v1
```

Sem `API_URL`, o app usa `http://10.0.2.2:8080/api/v1`, endereço do host dentro do emulador Android. Celular e computador precisam estar na mesma rede.

## Estrutura

```
lib/
├── bloc/      eventos, estados e MineradorBloc (atualização a cada 5 s)
├── core/      cliente HTTP e configuração da URL
├── models/    conversão das respostas da API
├── screens/   painel, minerador e alertas
└── widgets/   gráfico de hash rate, cartões e modal de legenda
```

## Verificação

```bash
flutter analyze
flutter test
```
