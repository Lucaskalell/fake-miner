class AlertaItem {
  final String mensagem;
  final String severidade;
  final DateTime timestamp;

  const AlertaItem({
    required this.mensagem,
    required this.severidade,
    required this.timestamp,
  });

  factory AlertaItem.fromJson(Map<String, dynamic> json) {
    return AlertaItem(
      mensagem: json['mensagem'] as String,
      severidade: json['severidade'] as String,
      timestamp: DateTime.parse(json['timestamp'] as String),
    );
  }
}
