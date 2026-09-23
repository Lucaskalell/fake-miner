import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { retry, switchMap, timer } from 'rxjs';
import { ServicoApi } from '../../services/servico-api.service';
import { ItemAlerta } from '../../models/minerador.modelos';

@Component({
  selector: 'app-historico-alertas',
  imports: [DatePipe],
  template: `
    <div class="envelopamento-alertas">
      @if (exibirTitulo()) {
        <div class="linha-cabecalho">
          <h2>Histórico de Alertas</h2>
          <span class="emblema">{{ listaAlertas().length }} Eventos</span>
        </div>
      }

      <div class="lista-alertas">
        @for (alerta of alertasExibidos(); track alerta.timestamp + alerta.mensagem; let i = $index) {
          <div class="cartao-alerta" 
               [class]="alerta.severidade.toLowerCase()" 
               [class.expandido]="indiceExpandido() === i"
               (click)="alternarExpansao(i)">
            <div class="indicador-alerta"></div>
            <div class="conteudo-alerta">
              <div class="linha-superior">
                <p class="mensagem" [class.truncada]="indiceExpandido() !== i">{{ alerta.mensagem }}</p>
                <span class="material-icons icone-seta">
                  {{ indiceExpandido() === i ? 'keyboard_arrow_up' : 'keyboard_arrow_down' }}
                </span>
              </div>
              
              @if (indiceExpandido() === i) {
                <div class="detalhes-alerta">
                  <span class="etiqueta-severidade">{{ alerta.severidade }}</span>
                  <span class="horario">
                    {{ alerta.timestamp | date:'dd/MM' }} {{ alerta.timestamp | date:'HH:mm' }}
                  </span>
                </div>
              } @else {
                <span class="etiqueta-severidade-mini">{{ alerta.severidade }}</span>
              }
            </div>
          </div>
        } @empty {
          <div class="estado-vazio">
            <span class="material-icons">notifications_none</span>
            <p>Nenhum alerta crítico detectado.</p>
          </div>
        }
      </div>
    </div>
  `,
  styles: `
    .envelopamento-alertas {
      animation: aparecer 0.4s ease-out;
      width: 100%;
    }

    @keyframes aparecer {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .linha-cabecalho {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    h2 { margin: 0; font-weight: 700; color: #fff; }

    .emblema {
      background: #1c1c21;
      padding: 6px 14px;
      border-radius: 8px;
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--cor-primaria);
      border: 1px solid rgba(255,255,255,0.05);
    }

    .lista-alertas {
      display: flex;
      flex-direction: column;
      gap: 0.8rem;
    }

    .cartao-alerta {
      background: #1a1a1a;
      border: 1px solid rgba(255,255,255,0.05);
      border-radius: 16px;
      display: flex;
      cursor: pointer;
      transition: all 0.2s;
      overflow: hidden;
    }

    .cartao-alerta:hover {
      border-color: rgba(255,255,255,0.1);
    }

    .indicador-alerta {
      width: 4px;
      margin: 12px 0 12px 12px;
      border-radius: 2px;
      background: #333;
    }

    .cartao-alerta.normal .indicador-alerta,
    .cartao-alerta.info .indicador-alerta { background: var(--cor-info); }
    .cartao-alerta.normal .etiqueta-severidade, 
    .cartao-alerta.normal .etiqueta-severidade-mini,
    .cartao-alerta.info .etiqueta-severidade,
    .cartao-alerta.info .etiqueta-severidade-mini { color: var(--cor-info); }

    .cartao-alerta.alerta_1 .indicador-alerta, 
    .cartao-alerta.vigilancia .indicador-alerta,
    .cartao-alerta.aviso .indicador-alerta { background: var(--cor-aviso); }
    .cartao-alerta.alerta_1 .etiqueta-severidade,
    .cartao-alerta.vigilancia .etiqueta-severidade,
    .cartao-alerta.aviso .etiqueta-severidade,
    .cartao-alerta.alerta_1 .etiqueta-severidade-mini,
    .cartao-alerta.vigilancia .etiqueta-severidade-mini,
    .cartao-alerta.aviso .etiqueta-severidade-mini { color: var(--cor-aviso); }

    .cartao-alerta.alerta_2 .indicador-alerta,
    .cartao-alerta.alerta_3 .indicador-alerta,
    .cartao-alerta.critico .indicador-alerta { background: var(--cor-perigo); }
    .cartao-alerta.alerta_2 .etiqueta-severidade,
    .cartao-alerta.alerta_3 .etiqueta-severidade,
    .cartao-alerta.critico .etiqueta-severidade,
    .cartao-alerta.alerta_2 .etiqueta-severidade-mini,
    .cartao-alerta.alerta_3 .etiqueta-severidade-mini,
    .cartao-alerta.critico .etiqueta-severidade-mini { color: var(--cor-perigo); }

    .cartao-alerta.expandido.normal, .cartao-alerta.expandido.info { border-color: rgba(0, 168, 255, 0.4); box-shadow: 0 0 15px rgba(0, 168, 255, 0.1); }
    .cartao-alerta.expandido.alerta_1, .cartao-alerta.expandido.vigilancia, .cartao-alerta.expandido.aviso { border-color: rgba(255, 165, 2, 0.4); box-shadow: 0 0 15px rgba(255, 165, 2, 0.1); }
    .cartao-alerta.expandido.alerta_2, .cartao-alerta.expandido.alerta_3, .cartao-alerta.expandido.critico { border-color: rgba(255, 71, 87, 0.4); box-shadow: 0 0 15px rgba(255, 71, 87, 0.1); }

    .conteudo-alerta {
      padding: 12px;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .linha-superior {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
    }

    .mensagem {
      margin: 0;
      color: #fff;
      font-size: 0.85rem;
      line-height: 1.4;
    }

    .mensagem.truncada {
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .icone-seta {
      font-size: 1.2rem;
      color: #555;
    }

    .detalhes-alerta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 8px;
    }

    .etiqueta-severidade {
      font-size: 0.65rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .etiqueta-severidade-mini {
      font-size: 0.65rem;
      font-weight: 800;
      text-transform: uppercase;
    }

    .horario {
      font-size: 0.65rem;
      color: #555;
    }

    .estado-vazio {
      padding: 2rem;
      text-align: center;
      background: #1a1a1a;
      border-radius: 24px;
      border: 1px dashed rgba(255,71,87,0.1);
      color: #555;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }

    .estado-vazio .material-icons { font-size: 1.5rem; }
    .estado-vazio p { margin: 0; font-size: 0.75rem; }
  `
})
export class ComponenteHistoricoAlertas {
  private static readonly INTERVALO_ATUALIZACAO_MS = 5000;

  private api = inject(ServicoApi);
  private destroyRef = inject(DestroyRef);

  listaAlertas = signal<ItemAlerta[]>([]);
  indiceExpandido = signal<number | null>(null);

  limite = input<number>(0);
  exibirTitulo = input<boolean>(true);

  alertasExibidos = computed(() => {
    const alertas = [...this.listaAlertas()].sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime(),
    );
    return this.limite() > 0 ? alertas.slice(0, this.limite()) : alertas;
  });

  constructor() {
    timer(0, ComponenteHistoricoAlertas.INTERVALO_ATUALIZACAO_MS)
      .pipe(
        switchMap(() => this.api.obterAlertas()),
        retry({ delay: ComponenteHistoricoAlertas.INTERVALO_ATUALIZACAO_MS }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((alertas) => this.listaAlertas.set(alertas));
  }

  alternarExpansao(indice: number) {
    this.indiceExpandido.update((atual) => (atual === indice ? null : indice));
  }
}
