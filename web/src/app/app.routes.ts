import { Routes } from '@angular/router';
import { ComponentePainelMetricas } from './components/painel-metricas/painel-metricas.component';
import { ComponenteHistoricoAlertas } from './components/historico-alertas/historico-alertas.component';

export const rotas: Routes = [
  { path: '', redirectTo: 'painel', pathMatch: 'full' },
  { path: 'painel', component: ComponentePainelMetricas },
  { path: 'alertas', component: ComponenteHistoricoAlertas },
];
