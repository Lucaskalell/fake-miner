import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_event.dart';
import 'package:fake_miner_mobile/bloc/minerador_state.dart';
import 'package:fake_miner_mobile/core/api_service.dart';

class MineradorBloc extends Bloc<MineradorEvent, MineradorState> {
  final ApiService _apiService = ApiService();
  Timer? _timer;

  MineradorBloc() : super(MineradorCarregando()) {
    on<MineradorIniciado>((event, emit) async {
      emit(MineradorCarregando());
      try {
        final dados = await _apiService.buscarSnapshot();
        emit(MineradorCarregado(dados.copyWith(historicoHash: [dados.hashRate])));
      } catch (e) {
        emit(MineradorErro('Sem conexão com o servidor'));
      }
      _timer?.cancel();
      _timer = Timer.periodic(const Duration(seconds: 5), (_) {
        add(MineradorAtualizado());
      });
    });

    on<MineradorAtualizado>((event, emit) async {
      try {
        final dados = await _apiService.buscarSnapshot();
        List<double> historico = [];
        if (state is MineradorCarregado) {
          historico = (state as MineradorCarregado).dados.historicoHash;
        }

        final novoHistorico = [...historico, dados.hashRate];
        final historicoLimitado = novoHistorico.length > 20
            ? novoHistorico.sublist(novoHistorico.length - 20)
            : novoHistorico;
        emit(MineradorCarregado(dados.copyWith(historicoHash: historicoLimitado)));
      } catch (e) {
        emit(MineradorErro('Falha na atualização automática'));
      }
    });

    on<MineradorAlternado>((event, emit) async {
      try {
        await _apiService.alternarMinerador();
        add(MineradorAtualizado());
      } catch (e) {
        emit(MineradorErro('Erro ao alternar o minerador'));
      }
    });
  }

  @override
  Future<void> close() {
    _timer?.cancel();
    return super.close();
  }
}
