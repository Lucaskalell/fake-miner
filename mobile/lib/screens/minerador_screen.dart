import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_event.dart';
import 'package:fake_miner_mobile/bloc/minerador_state.dart';

class MineradorScreen extends StatelessWidget {
  const MineradorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MineradorBloc, MineradorState>(
      builder: (context, state) {
        if (state is MineradorCarregando) {
          return const Center(child: CircularProgressIndicator(color: Color(0xFF50C878)));
        }
        if (state is MineradorErro) {
          return Center(
            child: Text(state.mensagem, style: const TextStyle(color: Colors.redAccent)),
          );
        }
        final dados = (state as MineradorCarregado).dados;
        return SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Minerador',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.white),
              ),
              const SizedBox(height: 4),
              const Text(
                'Controle e desempenho do worker',
                style: TextStyle(fontSize: 13, color: Color(0xFF555555)),
              ),
              const SizedBox(height: 20),
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: const Color(0xFF1A1A1A),
                  borderRadius: BorderRadius.circular(24),
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
                                style: TextStyle(
                                  fontSize: 11,
                                  color: Color(0xFF555555),
                                  letterSpacing: 1.2,
                                ),
                              ),
                              Text(
                                dados.ativo ? 'Mineração Ativa' : 'Mineração Pausada',
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
                    const SizedBox(height: 20),
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text(
                                'LUCRO',
                                style: TextStyle(
                                  fontSize: 11,
                                  color: Color(0xFF555555),
                                  letterSpacing: 1.2,
                                ),
                              ),
                              Text(
                                '\$${dados.lucro.toStringAsFixed(6)}',
                                style: const TextStyle(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.white,
                                ),
                              ),
                            ],
                          ),
                        ),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text(
                                'META',
                                style: TextStyle(
                                  fontSize: 11,
                                  color: Color(0xFF555555),
                                  letterSpacing: 1.2,
                                ),
                              ),
                              Text(
                                '${dados.meta.toStringAsFixed(2)}%',
                                style: const TextStyle(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.white,
                                ),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Progresso da meta',
                              style: TextStyle(fontSize: 12, color: Color(0xFF555555)),
                            ),
                            Text(
                              '${dados.meta.toStringAsFixed(2)}%',
                              style: const TextStyle(fontSize: 12, color: Color(0xFF50C878)),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: LinearProgressIndicator(
                            value: (dados.meta / 100).clamp(0.0, 1.0),
                            backgroundColor: const Color(0xFF2A2A2A),
                            valueColor: const AlwaysStoppedAnimation<Color>(Color(0xFF50C878)),
                            minHeight: 6,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
