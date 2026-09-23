import { Component, input, output } from '@angular/core';

export interface ItemLegenda {
  icone: string;
  titulo: string;
  descricao: string;
  cor?: string;
}

export interface SecaoLegenda {
  titulo?: string;
  itens: ItemLegenda[];
}

@Component({
  selector: 'app-modal-legenda',
  template: `
    <div class="fundo-modal" (click)="fechar.emit()">
      <div class="container-modal" (click)="$event.stopPropagation()">
        <header class="cabecalho-modal">
          <div class="alca-modal"></div>
          <div class="linha-titulo">
            <h2>{{ titulo() }}</h2>
            <button class="botao-fechar" (click)="fechar.emit()">
              <span class="material-icons">close</span>
            </button>
          </div>
        </header>

        <div class="corpo-modal">
          @for (secao of secoes(); track secao.titulo) {
            <div class="secao-legenda">
              @if (secao.titulo) {
                <h3 class="titulo-secao">{{ secao.titulo }}</h3>
              }
              <div class="lista-itens">
                @for (item of secao.itens; track item.titulo) {
                  <div class="item-legenda">
                    <div class="icone-container" [style.color]="item.cor || 'var(--cor-primaria)'">
                      <span class="material-icons">{{ item.icone }}</span>
                    </div>
                    <div class="texto-item">
                      <span class="rotulo-item">{{ item.titulo }}</span>
                      <p class="descricao-item">{{ item.descricao }}</p>
                    </div>
                  </div>
                }
              </div>
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: `
    .fundo-modal {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: flex-end;
      justify-content: center;
      z-index: 1000;
      animation: fadeIn 0.3s ease;
    }

    .container-modal {
      width: 100%;
      max-width: 600px;
      background: #1a1a1a;
      border-radius: 24px 24px 0 0;
      padding: 1.5rem 2rem 2.5rem 2rem;
      animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      max-height: 90vh;
      display: flex;
      flex-direction: column;
    }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }

    .cabecalho-modal {
      margin-bottom: 1.5rem;
    }

    .alca-modal {
      width: 40px;
      height: 4px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 2px;
      margin: 0 auto 1.5rem auto;
    }

    .linha-titulo {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .linha-titulo h2 {
      margin: 0;
      font-size: 1.5rem;
      color: #fff;
    }

    .botao-fechar {
      background: transparent;
      border: none;
      color: #555;
      cursor: pointer;
      padding: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.2s;
    }

    .botao-fechar:hover { color: #fff; }

    .corpo-modal {
      overflow-y: auto;
      padding-right: 8px;
    }

    .corpo-modal::-webkit-scrollbar { width: 4px; }
    .corpo-modal::-webkit-scrollbar-thumb { background: #333; border-radius: 2px; }

    .titulo-secao {
      color: var(--cor-primaria);
      font-size: 0.75rem;
      font-weight: 800;
      letter-spacing: 1.2px;
      text-transform: uppercase;
      margin: 1.5rem 0 1rem 0;
    }

    .lista-itens {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .item-legenda {
      display: flex;
      gap: 1.2rem;
      align-items: flex-start;
    }

    .icone-container {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 24px;
    }

    .rotulo-item {
      display: block;
      color: #fff;
      font-weight: 700;
      font-size: 1rem;
      margin-bottom: 4px;
    }

    .descricao-item {
      margin: 0;
      color: #888;
      font-size: 0.9rem;
      line-height: 1.4;
    }
  `
})
export class ComponenteModalLegenda {
  titulo = input<string>('Legenda');
  secoes = input<SecaoLegenda[]>([]);
  fechar = output<void>();
}
