import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:fake_miner_mobile/models/minerador_state_model.dart';
import 'package:fake_miner_mobile/models/alerta_item.dart';
import 'package:fake_miner_mobile/core/env.dart';

class ApiService {
  static const String _baseUrl = Env.baseUrl;

  Future<MineradorStateModel> buscarSnapshot() async {
    final response = await http
        .get(Uri.parse('$_baseUrl/metricas'))
        .timeout(const Duration(seconds: 5));

    if (response.statusCode == 200) {
      return MineradorStateModel.fromJson(jsonDecode(response.body));
    }
    throw Exception('Erro ao buscar métricas');
  }

  Future<void> alternarMinerador() async {
    await http.post(Uri.parse('$_baseUrl/minerador/alternar')).timeout(const Duration(seconds: 5));
  }

  Future<List<AlertaItem>> buscarAlertas() async {
    final response = await http
        .get(Uri.parse('$_baseUrl/alertas'))
        .timeout(const Duration(seconds: 5));

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => AlertaItem.fromJson(json)).toList();
    }
    throw Exception('Erro ao buscar alertas');
  }
}
