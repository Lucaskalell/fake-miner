import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ComponentePainelMetricas } from './components/painel-metricas/painel-metricas.component';
import { ComponenteHistoricoAlertas } from './components/historico-alertas/historico-alertas.component';
import { ComponenteModalLegenda, SecaoLegenda } from './components/modal-legenda/modal-legenda.component';
import { filter } from 'rxjs';

@Component({
  selector: 'app-raiz',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ComponentePainelMetricas, ComponenteHistoricoAlertas, ComponenteModalLegenda],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class ComponenteRaiz {
  titulo = 'FakeMiner';
  modoMesclado = signal(false);
  exibirLegenda = signal(false);
  rotaAtual = signal('');

  private router = inject(Router);

  constructor() {
    this.router.events
      .pipe(
        filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((evento) => this.rotaAtual.set(evento.urlAfterRedirects));
  }

  alternarModo() {
    this.modoMesclado.update(m => !m);
  }

  alternarLegenda() {
    this.exibirLegenda.update(l => !l);
  }

  secoesLegenda = computed<SecaoLegenda[]>(() => {
    const itensPainel = [
      { icone: 'bolt', titulo: 'Hash Rate', descricao: 'Velocidade de mineração atual, medida em MH/s.' },
      { icone: 'thermostat', titulo: 'Temperatura', descricao: 'Temperatura estimada da CPU.', cor: '#f7a409' },
      { icone: 'memory', titulo: 'CPU', descricao: 'Uso total do processador.', cor: '#0ea9f1' },
    ];

    const itensMinerador = [
      { icone: 'power_settings_new', titulo: 'Status', descricao: 'Liga ou desliga a mineração simulada.' },
      { icone: 'attach_money', titulo: 'Lucro', descricao: 'Ganhos estimados na sessão.', cor: '#50C878FF' },
      { icone: 'track_changes', titulo: 'Meta', descricao: 'Progresso em relação à meta diária de mineração.' },
    ];

    const itensAlertas = [
      { icone: 'warning_amber', titulo: 'Aviso', descricao: 'Métricas em nível de atenção ou vigilância.', cor: '#ffa502' },
      { icone: 'error_outline', titulo: 'Crítico', descricao: 'Métricas em nível perigoso ou desligamento.', cor: '#ff4757' },
    ];

    if (this.modoMesclado()) {
      return [
        { titulo: 'Dashboard', itens: itensPainel },
        { titulo: 'Minerador', itens: itensMinerador },
        { titulo: 'Alertas', itens: itensAlertas },
      ];
    }

    if (this.rotaAtual().includes('alertas')) {
      return [{ titulo: 'Alertas', itens: itensAlertas }];
    }

    return [{ titulo: 'Métricas', itens: itensPainel }];
  });

  tituloLegenda = computed(() => this.modoMesclado() ? 'Visão Geral — Legenda' : 'Legenda');
}

