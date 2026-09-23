import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_state.dart';
import 'package:fake_miner_mobile/widgets/hash_chart.dart';
import 'package:fake_miner_mobile/widgets/metric_card.dart';
import 'package:fake_miner_mobile/bloc/minerador_event.dart';
import 'package:fake_miner_mobile/core/api_service.dart';
import 'package:fake_miner_mobile/models/alerta_item.dart';

class DashboardScreen extends StatefulWidget {
  final Function(bool)? onMergedViewChanged;

  const DashboardScreen({super.key, this.onMergedViewChanged});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  bool _isMergedView = false;
  final ApiService _apiService = ApiService();
  List<AlertaItem> _recentAlerts = [];
  Timer? _refreshTimer;
  int? _expandedIndex;

  @override
  void initState() {
    super.initState();
    _loadRecentAlerts();
    _refreshTimer = Timer.periodic(const Duration(seconds: 30), (_) => _loadRecentAlerts());
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadRecentAlerts() async {
    try {
      final alerts = await _apiService.buscarAlertas();
      if (mounted) {
        setState(() {
          alerts.sort((a, b) => b.timestamp.compareTo(a.timestamp));
          _recentAlerts = alerts.take(3).toList();
        });
      }
    } catch (_) {}
  }

  void _toggleMergedView(bool value) {
    setState(() => _isMergedView = value);
    if(value){
      _loadRecentAlerts();
    }
    if (widget.onMergedViewChanged != null) {
      widget.onMergedViewChanged!(value);
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MineradorBloc, MineradorState>(
      builder: (context, state) {
        if (state is MineradorCarregando) {
          return const Center(child: CircularProgressIndicator(color: Color(0xFF50C878)));
        }
        if (state is MineradorErro) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(state.mensagem, style: const TextStyle(color: Colors.redAccent)),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: () => context.read<MineradorBloc>().add(MineradorIniciado()),
                  child: const Text('Tentar Novamente'),
                ),
              ],
            ),
          );
        }
        final dados = (state as MineradorCarregado).dados;

        return SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _isMergedView ? 'Visão Geral' : 'Painel',
                        style: const TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _isMergedView ? 'Tudo em um só lugar' : 'Monitoramento em tempo real',
                        style: const TextStyle(fontSize: 13, color: Color(0xFF555555)),
                      ),
                    ],
                  ),
                  Column(
                    children: [
                      const Text(
                        'MESCLAR',
                        style: TextStyle(fontSize: 9, color: Color(0xFF555555)),
                      ),
                      Switch(
                        value: _isMergedView,
                        onChanged: _toggleMergedView,
                        activeThumbColor: const Color(0xFF50C878),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 20),

              _buildDashboardSection(dados),

              if (_isMergedView) ...[
                const SizedBox(height: 24),
                const Divider(color: Colors.white10),
                const SizedBox(height: 16),
                const Text(
                  'Minerador',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
                ),
                const SizedBox(height: 16),
                _buildMineradorSection(dados, context),
                const SizedBox(height: 24),
                const Divider(color: Colors.white10),
                const SizedBox(height: 16),
                const Text(
                  'Alertas Recentes',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
                ),
                const SizedBox(height: 16),
                _buildAlertasSection(),
              ],
            ],
          ),
        );
      },
    );
  }

  Widget _buildDashboardSection(dados) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: const Color(0xFF1A1A1A),
            borderRadius: BorderRadius.circular(24),
            border: _isMergedView
                ? Border.all(color: const Color(0xFF50C878).withValues(alpha: 0.3))
                : null,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'HASH RATE',
                        style: TextStyle(
                          fontSize: 11,
                          color: Color(0xFF555555),
                          letterSpacing: 1.2,
                        ),
                      ),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text(
                            dados.hashRate.toStringAsFixed(2),
                            style: const TextStyle(
                              fontSize: 32,
                              fontWeight: FontWeight.w600,
                              color: Colors.white,
                            ),
                          ),
                          const SizedBox(width: 4),
                          const Padding(
                            padding: EdgeInsets.only(bottom: 6),
                            child: Text(
                              'MH/s',
                              style: TextStyle(fontSize: 14, color: Color(0xFF555555)),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: const Color(0xFF50C878).withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.bolt, color: Color(0xFF50C878), size: 22),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              HashChart(data: dados.historicoHash),
            ],
          ),
        ),
        const SizedBox(height: 16),
        Row(
          children: [
            Expanded(
              child: MetricCard(
                icon: Icons.thermostat,
                label: 'TEMPERATURA',
                value: '${dados.temperatura.toStringAsFixed(0)}',
                unit: '°C',
                hint: dados.temperatura > 78 ? 'Acima do ideal' : 'Operacional',
                accentColor: dados.temperatura > 78 ? Colors.orange : const Color(0xFF50C878),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: MetricCard(
                icon: Icons.memory,
                label: 'CPU',
                value: '${dados.cpu.toStringAsFixed(0)}',
                unit: '%',
                hint: 'Uso médio',
                accentColor: Colors.blueAccent,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildMineradorSection(dados, context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF1A1A1A),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.blueAccent.withValues(alpha: 0.2)),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: const Color(0xFF50C878).withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(
                  Icons.power_settings_new,
                  color: dados.ativo ? const Color(0xFF50C878) : Colors.grey,
                  size: 22,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'STATUS',
                      style: TextStyle(fontSize: 11, color: Color(0xFF555555), letterSpacing: 1.2),
                    ),
                    Text(
                      dados.ativo ? 'Ativa' : 'Pausada',
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
              ),
              Switch(
                value: dados.ativo,
                onChanged: (_) => context.read<MineradorBloc>().add(MineradorAlternado()),
                activeThumbColor: const Color(0xFF50C878),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('LUCRO', style: TextStyle(fontSize: 10, color: Color(0xFF555555))),
                    Text(
                      '\$${dados.lucro.toStringAsFixed(4)}',
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('META', style: TextStyle(fontSize: 10, color: Color(0xFF555555))),
                    Text(
                      '${dados.meta.toStringAsFixed(1)}%',
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildAlertasSection() {
    if (_recentAlerts.isEmpty) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: const Color(0xFF1A1A1A),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: Colors.redAccent.withValues(alpha: 0.1)),
        ),
        child: const Column(
          children: [
            Icon(Icons.notifications_none, color: Color(0xFF555555)),
            SizedBox(height: 8),
            Text(
              'Nenhum alerta crítico detectado.',
              style: TextStyle(color: Color(0xFF555555), fontSize: 12),
            ),
          ],
        ),
      );
    }

    return Column(
      children: List.generate(_recentAlerts.length, (index) {
        final alert = _recentAlerts[index];
        Color color;
        switch (alert.severidade.toUpperCase()) {
          case 'CRITICO':
            color = Colors.red;
            break;
          case 'AVISO':
            color = Colors.orange;
            break;
          default:
            color = Colors.blue;
        }

        final isExpanded = _expandedIndex == index;

        return GestureDetector(
          onTap: () => setState(() => _expandedIndex = isExpanded ? null : index),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            margin: const EdgeInsets.only(bottom: 8),
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: const Color(0xFF1A1A1A),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isExpanded ? color.withValues(alpha: 0.5) : color.withValues(alpha: 0.2),
              ),
            ),
            child: Row(
              crossAxisAlignment: isExpanded ? CrossAxisAlignment.start : CrossAxisAlignment.center,
              children: [
                Container(
                  width: 4,
                  height: isExpanded ? 40 : 32,
                  decoration: BoxDecoration(
                    color: color,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        alert.mensagem,
                        maxLines: isExpanded ? 5 : 1,
                        overflow: isExpanded ? TextOverflow.visible : TextOverflow.ellipsis,
                        style: const TextStyle(color: Colors.white, fontSize: 13),
                      ),
                      if (isExpanded) ...[
                        const SizedBox(height: 8),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              alert.severidade,
                              style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.bold),
                            ),
                            Text(
                              '${alert.timestamp.day.toString().padLeft(2, '0')}/${alert.timestamp.month.toString().padLeft(2, '0')} ${alert.timestamp.hour.toString().padLeft(2, '0')}:${alert.timestamp.minute.toString().padLeft(2, '0')}',
                              style: const TextStyle(color: Color(0xFF555555), fontSize: 10),
                            ),
                          ],
                        ),
                      ] else
                        Text(
                          alert.severidade,
                          style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.bold),
                        ),
                    ],
                  ),
                ),
                Icon(
                  isExpanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
                  color: const Color(0xFF555555),
                  size: 16,
                ),
              ],
            ),
          ),
        );
      }),
    );
  }
}
