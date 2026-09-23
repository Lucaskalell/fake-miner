import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponenteRaiz } from './app';

describe('ComponenteRaiz', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComponenteRaiz],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('deve exibir o título da aplicação', async () => {
    const fixture = TestBed.createComponent(ComponenteRaiz);
    await fixture.whenStable();
    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelector('h1')?.textContent).toContain('FakeMiner');
  });

  it('deve alternar para o modo mesclado', () => {
    const fixture = TestBed.createComponent(ComponenteRaiz);
    const componente = fixture.componentInstance;

    componente.alternarModo();

    expect(componente.modoMesclado()).toBe(true);
    expect(componente.secoesLegenda().map((secao) => secao.titulo)).toEqual(['Dashboard', 'Minerador', 'Alertas']);
  });
});
