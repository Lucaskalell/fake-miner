import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:fake_miner_mobile/bloc/minerador_bloc.dart';
import 'package:fake_miner_mobile/screens/dashboard_screen.dart';
import 'package:fake_miner_mobile/screens/minerador_screen.dart';
import 'package:fake_miner_mobile/screens/alertas_screen.dart';
import 'package:fake_miner_mobile/widgets/legend_modal.dart';

import 'bloc/minerador_event.dart';

void main() {
  runApp(const FakeMinerApp());
}

class FakeMinerApp extends StatelessWidget {
  const FakeMinerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => MineradorBloc()..add(MineradorIniciado()),
      child: MaterialApp(
        title: 'FakeMiner',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          brightness: Brightness.dark,
          scaffoldBackgroundColor: const Color(0xFF000000),
          appBarTheme: const AppBarTheme(
            backgroundColor: Colors.transparent,
            elevation: 0,
            centerTitle: false,
            titleTextStyle: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          colorScheme: const ColorScheme.dark(
            primary: Color(0xFF50C878),
            surface: Color(0xFF1A1A1A),
          ),
          bottomNavigationBarTheme: const BottomNavigationBarThemeData(
            backgroundColor: Color(0xFF0D0D0D),
            selectedItemColor: Color(0xFF50C878),
            unselectedItemColor: Color(0xFF555555),
            type: BottomNavigationBarType.fixed,
            elevation: 10,
          ),
        ),
        home: const HomeScreen(),
      ),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;
  bool _isMergedView = false;

  final List<String> _titles = const ['Painel', 'Minerador', 'Alertas'];

  void _showLegend() {
    List<LegendSection> sections = [];
    String title = _titles[_currentIndex];

    final dashItems = [
      LegendItem(
        icon: Icons.bolt,
        label: 'Hash Rate',
        description: 'Velocidade de processamento atual.',
      ),
      LegendItem(
        icon: Icons.thermostat,
        label: 'Temperatura',
        description: 'Temperatura estimada da CPU.',
        color: Colors.orange,
      ),
      LegendItem(
        icon: Icons.memory,
        label: 'CPU',
        description: 'Uso total do processador.',
        color: Colors.blueAccent,
      ),
    ];

    final minerItems = [
      LegendItem(
        icon: Icons.power_settings_new,
        label: 'Status',
        description: 'Liga ou desliga a mineração simulada.',
      ),
      LegendItem(
        icon: Icons.attach_money,
        label: 'Lucro',
        description: 'Ganhos estimados da sessão.',
        color: Colors.greenAccent,
      ),
      LegendItem(
        icon: Icons.track_changes,
        label: 'Meta',
        description: 'Progresso da meta diária.',
        color: const Color(0xFF50C878),
      ),
    ];

    final alertItems = [
      LegendItem(
        icon: Icons.warning_amber_rounded,
        label: 'Aviso',
        description: 'Métricas em nível de atenção.',
        color: Colors.orange,
      ),
      LegendItem(
        icon: Icons.error_outline,
        label: 'Crítico',
        description: 'Métricas em nível perigoso.',
        color: Colors.redAccent,
      ),
    ];

    if (_currentIndex == 0 && _isMergedView) {
      title = 'Visão Geral';
      sections = [
        LegendSection(title: 'Painel', items: dashItems),
        LegendSection(title: 'Minerador', items: minerItems),
        LegendSection(title: 'Alertas', items: alertItems),
      ];
    } else {
      switch (_currentIndex) {
        case 0:
          sections = [LegendSection(items: dashItems)];
          break;
        case 1:
          sections = [LegendSection(items: minerItems)];
          break;
        case 2:
          sections = [LegendSection(items: alertItems)];
          break;
      }
    }

    if (sections.isNotEmpty) {
      LegendModal.show(context, title, sections);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_currentIndex]),
        actions: [
          IconButton(
            onPressed: _showLegend,
            icon: const Icon(Icons.info_outline, color: Color(0xFF50C878)),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: [
          DashboardScreen(
            onMergedViewChanged: (isMerged) {
              setState(() => _isMergedView = isMerged);
            },
          ),
          const MineradorScreen(),
          const AlertasScreen(),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.show_chart), label: 'Painel'),
          BottomNavigationBarItem(icon: Icon(Icons.memory), label: 'Minerador'),
          BottomNavigationBarItem(icon: Icon(Icons.notifications_outlined), label: 'Alertas'),
        ],
      ),
    );
  }
}
