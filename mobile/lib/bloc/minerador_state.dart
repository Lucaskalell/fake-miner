import 'package:equatable/equatable.dart';
import 'package:fake_miner_mobile/models/minerador_state_model.dart';

abstract class MineradorState extends Equatable {
  @override
  List<Object?> get props => [];
}

class MineradorCarregando extends MineradorState {}

class MineradorCarregado extends MineradorState {
  final MineradorStateModel dados;

  MineradorCarregado(this.dados);

  @override
  List<Object?> get props => [dados];
}

class MineradorErro extends MineradorState {
  final String mensagem;

  MineradorErro(this.mensagem);

  @override
  List<Object?> get props => [mensagem];
}
