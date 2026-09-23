import 'package:fake_miner_mobile/models/alerta_item.dart';
import 'package:fake_miner_mobile/models/minerador_state_model.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('MineradorStateModel.fromJson', () {
    test('converte o snapshot da API', () {
      final modelo = MineradorStateModel.fromJson({
        'hardware': {'temperaturaCpu': 58.4, 'usoCpu': 37.3},
        'mineracao': {
          'ativo': true,
          'hashRateAtual': 48.5,
          'lucroEstimadoDolar': 0.07,
          'progressoMetaDiaria': 1.4,
        },
      });

      expect(modelo.ativo, isTrue);
      expect(modelo.hashRate, 48.5);
      expect(modelo.temperatura, 58.4);
      expect(modelo.cpu, 37.3);
    });
  });

  group('AlertaItem.fromJson', () {
    test('converte um alerta da API', () {
      final alerta = AlertaItem.fromJson({
        'mensagem': 'ALERTA: MÉTRICA CRÍTICA DETECTADA',
        'severidade': 'CRITICO',
        'timestamp': '2026-09-22T21:30:00',
      });

      expect(alerta.severidade, 'CRITICO');
      expect(alerta.timestamp, DateTime(2026, 9, 22, 21, 30));
    });
  });
}
