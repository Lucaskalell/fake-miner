class MineradorStateModel {
  final double hashRate;
  final List<double> historicoHash;
  final double temperatura;
  final double cpu;
  final double lucro;
  final double meta;
  final bool ativo;

  const MineradorStateModel({
    required this.hashRate,
    required this.historicoHash,
    required this.temperatura,
    required this.cpu,
    required this.lucro,
    required this.meta,
    required this.ativo,
  });

  factory MineradorStateModel.fromJson(Map<String, dynamic> json) {
    final hardware = json['hardware'] as Map<String, dynamic>;
    final mineracao = json['mineracao'] as Map<String, dynamic>;

    return MineradorStateModel(
      hashRate: (mineracao['hashRateAtual'] as num).toDouble(),
      historicoHash: [],
      temperatura: (hardware['temperaturaCpu'] as num).toDouble(),
      cpu: (hardware['usoCpu'] as num).toDouble(),
      lucro: (mineracao['lucroEstimadoDolar'] as num).toDouble(),
      meta: (mineracao['progressoMetaDiaria'] as num).toDouble(),
      ativo: mineracao['ativo'] as bool,
    );
  }

  MineradorStateModel copyWith({
    double? hashRate,
    List<double>? historicoHash,
    double? temperatura,
    double? cpu,
    double? lucro,
    double? meta,
    bool? ativo,
  }) {
    return MineradorStateModel(
      hashRate: hashRate ?? this.hashRate,
      historicoHash: historicoHash ?? this.historicoHash,
      temperatura: temperatura ?? this.temperatura,
      cpu: cpu ?? this.cpu,
      lucro: lucro ?? this.lucro,
      meta: meta ?? this.meta,
      ativo: ativo ?? this.ativo,
    );
  }
}
