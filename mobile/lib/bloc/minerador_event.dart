import 'package:equatable/equatable.dart';

abstract class MineradorEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class MineradorIniciado extends MineradorEvent {}

class MineradorAtualizado extends MineradorEvent {}

class MineradorAlternado extends MineradorEvent {}
