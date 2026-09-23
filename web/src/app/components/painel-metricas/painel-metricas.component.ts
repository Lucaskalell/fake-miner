import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { retry, switchMap, timer } from 'rxjs';
import { ServicoApi } from '../../services/servico-api.service';
import { CapturaSistema } from '../../models/minerador.modelos';

@Component({
  selector: 'app-painel-metricas',
  imports: [DecimalPipe],
  template: `
    <div class="envelopamento-painel">
      @if (dadosAtualizados(); as dados) {
        
        @if (!exibirApenasControle()) {
          <div class="grade-metricas">
            <div class="cartao-reflexo cartao-metrica destaque-hash">
              <div class="cabecalho-cartao">
                <span class="rotulo">Hash Rate</span>
                <div class="icone-circulo">
                  <span class="material-icons">bolt</span>
                </div>
              </div>
              <div class="corpo-cartao">
                <div class="container-valor">
                  <span class="valor gradiente-texto">{{ dados.mineracao.hashRateAtual | number: '1.2-2' }}</span>
                  <span class="unidade">MH/s</span>
                </div>
                
                <div class="container-grafico">
                  <svg viewBox="0 0 400 120" preserveAspectRatio="none" class="grafico-svg">
                    <defs>
                      <linearGradient id="gradienteArea" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" style="stop-color: var(--cor-primaria); stop-opacity: 0.3" />
                        <stop offset="100%" style="stop-color: var(--cor-primaria); stop-opacity: 0" />
                      </linearGradient>
                    </defs>
                    
                    <path [attr.d]="caminhoArea()" fill="url(#gradienteArea)" />
                    
                    <path [attr.d]="caminhoLinha()" fill="none" stroke="var(--cor-primaria)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  
                  <div class="eixo-x">
                    <span>0s</span>
                    <span>10s</span>
                    <span>20s</span>
                    <span>30s</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="grade-secundaria">
              <div class="cartao-reflexo cartao-metrica pequeno" [class.alerta-amarelo]="dados.hardware.usoCpu > 80">
                <div class="cabecalho-cartao">
                  <div class="bloco-info">
                    <span class="rotulo">Uso de CPU</span>
                    <div class="container-valor-pequeno">
                      <span class="valor-pequeno">{{ dados.hardware.usoCpu | number: '1.0-0' }}</span>
                      <span class="unidade-pequena">%</span>
                    </div>
                  </div>
                  <span class="material-icons icone-discreto">memory</span>
                </div>
                <div class="rodape-cartao">
                  <span class="legenda">Uso médio</span>
                </div>
              </div>

              <div class="cartao-reflexo cartao-metrica pequeno" [class.alerta-perigo]="dados.hardware.temperaturaCpu > 78">
                <div class="cabecalho-cartao">
                  <div class="bloco-info">
                    <span class="rotulo">Temperatura</span>
                    <div class="container-valor-pequeno">
                      <span class="valor-pequeno">{{ dados.hardware.temperaturaCpu | number: '1.0-0' }}</span>
                      <span class="unidade-pequena">°C</span>
                    </div>
                  </div>
                  <span class="material-icons icone-discreto" [style.color]="dados.hardware.temperaturaCpu > 78 ? 'var(--cor-perigo)' : 'var(--cor-primaria)'">thermostat</span>
                </div>
                <div class="rodape-cartao">
                  <span class="legenda">{{ dados.hardware.temperaturaCpu > 78 ? 'Acima do ideal' : 'Operacional' }}</span>
                </div>
              </div>
            </div>
          </div>
        }

        @if (!exibirApenasDashboard()) {
          <div class="secao-controles">
            <div class="cartao-reflexo painel-controle" [class.ativo]="dados.mineracao.ativo">
              <div class="topo-controle">
                <div class="info-principal">
                  <div class="icone-status" [class.ligado]="dados.mineracao.ativo">
                    <span class="material-icons">power_settings_new</span>
                  </div>
                  <div class="texto-status">
                    <span class="rotulo">Status</span>
                    <h3>{{ dados.mineracao.ativo ? 'Ativa' : 'Pausada' }}</h3>
                  </div>
                </div>
                <div class="interruptor-energia">
                  <label class="switch-grande">
                    <input type="checkbox" [checked]="dados.mineracao.ativo" (change)="alternarEstadoMinerador()">
                    <span class="slider-grande round"></span>
                  </label>
                </div>
              </div>

              <div class="grid-detalhes">
                <div class="item-detalhe">
                  <span class="rotulo-mini">LUCRO</span>
                  <span class="valor-detalhe">$ {{ dados.mineracao.lucroEstimadoDolar | number: '1.4-4' }}</span>
                </div>
                <div class="item-detalhe">
                  <span class="rotulo-mini">META</span>
                  <span class="valor-detalhe">{{ dados.mineracao.progressoMetaDiaria | number: '1.1-1' }}%</span>
                </div>
                @if (dados.mineracao.ativo) {
                  <div class="item-detalhe">
                    <span class="rotulo-mini">CONSUMO</span>
                    <span class="valor-detalhe">{{ dados.mineracao.consumoWatts | number: '1.0-0' }} W</span>
                  </div>
                }
              </div>
            </div>
          </div>
        }

      } @else {
        <div class="tela-carregamento">
          <div class="carregador"></div>
          <p>Sincronizando dados...</p>
        </div>
      }
    </div>
  `,
  styles: `
    .envelopamento-painel {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .grade-metricas {
      display: flex;
      flex-direction: column;
      gap: 1.2rem;
    }

    .grade-secundaria {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.2rem;
    }

    .cartao-reflexo {
      background: #1a1a1a;
      border: 1px solid rgba(255,255,255,0.05);
      border-radius: 24px;
      padding: 1.5rem;
      transition: all 0.3s;
    }

    .destaque-hash {
      background: #111;
      border: 1px solid rgba(80, 200, 120, 0.1);
    }

    .cabecalho-cartao {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
    }

    .rotulo {
      color: #555;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 1.2px;
      text-transform: uppercase;
    }

    .icone-circulo {
      width: 44px;
      height: 44px;
      background: rgba(80, 200, 120, 0.1);
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--cor-primaria);
    }

    .valor {
      font-size: 2.5rem;
      font-weight: 700;
    }

    .unidade {
      color: #555;
      margin-left: 8px;
      font-size: 1rem;
    }

    .container-grafico {
      margin-top: 1.5rem;
      height: 140px;
      display: flex;
      flex-direction: column;
    }

    .grafico-svg {
      width: 100%;
      height: 110px;
      filter: drop-shadow(0 0 10px rgba(80, 200, 120, 0.15));
    }

    .eixo-x {
      display: flex;
      justify-content: space-between;
      padding-top: 8px;
      color: #333;
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.5px;
    }

    .cartao-metrica.pequeno {
      padding: 1.2rem;
    }

    .container-valor-pequeno {
      display: flex;
      align-items: flex-end;
      gap: 4px;
      margin-top: 4px;
    }

    .valor-pequeno {
      font-size: 1.8rem;
      font-weight: 700;
      color: #fff;
    }

    .unidade-pequena {
      color: #555;
      font-size: 0.9rem;
      margin-bottom: 4px;
    }

    .icone-discreto {
      color: #333;
      font-size: 1.5rem;
    }

    .rodape-cartao {
      margin-top: 1rem;
    }

    .legenda {
      font-size: 0.75rem;
      color: #555;
    }

    .painel-controle {
      border: 1px solid rgba(80, 200, 120, 0.15);
    }

    .topo-controle {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .info-principal {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .icone-status {
      width: 44px;
      height: 44px;
      background: rgba(255,255,255,0.05);
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #555;
      transition: all 0.3s;
    }

    .icone-status.ligado {
      background: rgba(80, 200, 120, 0.1);
      color: var(--cor-primaria);
    }

    .texto-status h3 {
      margin: 0;
      font-size: 1.2rem;
    }

    .grid-detalhes {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
      gap: 1.5rem;
    }

    .item-detalhe {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .rotulo-mini {
      font-size: 0.65rem;
      color: #555;
      font-weight: 700;
      letter-spacing: 1px;
    }

    .valor-detalhe {
      font-size: 1rem;
      font-weight: 700;
      color: #fff;
    }

    .switch-grande {
      position: relative;
      display: inline-block;
      width: 50px;
      height: 26px;
    }

    .switch-grande input { opacity: 0; width: 0; height: 0; }

    .slider-grande {
      position: absolute;
      cursor: pointer;
      top: 0; left: 0; right: 0; bottom: 0;
      background-color: #333;
      transition: .4s;
    }

    .slider-grande:before {
      position: absolute;
      content: "";
      height: 18px; width: 18px;
      left: 4px; bottom: 4px;
      background-color: white;
      transition: .4s;
    }

    input:checked + .slider-grande { background-color: var(--cor-primaria); }
    input:checked + .slider-grande:before { transform: translateX(24px); }
    .slider-grande.round { border-radius: 26px; }
    .slider-grande.round:before { border-radius: 50%; }

    .tela-carregamento {
      height: 200px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #555;
    }

    .carregador {
      width: 32px;
      height: 32px;
      border: 2px solid #222;
      border-top-color: var(--cor-primaria);
      border-radius: 50%;
      animation: girar 1s linear infinite;
      margin-bottom: 1rem;
    }

    @keyframes girar { to { transform: rotate(360deg); } }
  `
})
export class ComponentePainelMetricas {
  private static readonly INTERVALO_ATUALIZACAO_MS = 2000;
  private static readonly PONTOS_NO_GRAFICO = 20;
  private static readonly LARGURA_GRAFICO = 400;
  private static readonly ALTURA_GRAFICO = 110;
  private static readonly MARGEM_GRAFICO = 10;

  private api = inject(ServicoApi);
  private destroyRef = inject(DestroyRef);

  dadosAtualizados = signal<CapturaSistema | null>(null);
  historicoHash = signal<number[]>([]);

  exibirApenasDashboard = input<boolean>(false);
  exibirApenasControle = input<boolean>(false);

  caminhoLinha = computed(() => {
    const valores = this.historicoHash();
    if (valores.length < 2) return '';

    const { LARGURA_GRAFICO: largura, ALTURA_GRAFICO: altura, MARGEM_GRAFICO: margem } = ComponentePainelMetricas;
    const minimo = Math.min(...valores) - 2;
    const amplitude = Math.max(...valores) + 2 - minimo || 1;

    const pontos = valores.map((valor, i) => ({
      x: (i / (valores.length - 1)) * largura,
      y: altura - margem - ((valor - minimo) / amplitude) * (altura - 2 * margem),
    }));

    return pontos.slice(1).reduce((caminho, atual, i) => {
      const anterior = pontos[i];
      const controleX = anterior.x + (atual.x - anterior.x) / 2;
      return `${caminho} C ${controleX} ${anterior.y}, ${controleX} ${atual.y}, ${atual.x} ${atual.y}`;
    }, `M ${pontos[0].x} ${pontos[0].y}`);
  });

  caminhoArea = computed(() => {
    const linha = this.caminhoLinha();
    if (!linha) return '';
    const { LARGURA_GRAFICO: largura, ALTURA_GRAFICO: altura } = ComponentePainelMetricas;
    return `${linha} L ${largura} ${altura} L 0 ${altura} Z`;
  });

  constructor() {
    timer(0, ComponentePainelMetricas.INTERVALO_ATUALIZACAO_MS)
      .pipe(
        switchMap(() => this.api.obterCaptura()),
        retry({ delay: ComponentePainelMetricas.INTERVALO_ATUALIZACAO_MS }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((dados) => this.registrarCaptura(dados));
  }

  alternarEstadoMinerador() {
    this.api
      .alternarMinerador()
      .pipe(switchMap(() => this.api.obterCaptura()))
      .subscribe({
        next: (dados) => this.registrarCaptura(dados),
        error: (erro) => console.error('Falha ao alternar o minerador', erro),
      });
  }

  private registrarCaptura(dados: CapturaSistema) {
    this.dadosAtualizados.set(dados);
    this.historicoHash.update((historico) =>
      [...historico, dados.mineracao.hashRateAtual].slice(-ComponentePainelMetricas.PONTOS_NO_GRAFICO),
    );
  }
}
