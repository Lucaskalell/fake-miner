import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ItemAlerta, StatusMineracao, CapturaSistema } from '../models/minerador.modelos';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ServicoApi {
  private clienteHttp = inject(HttpClient);
  private urlBase = environment.urlApi;

  obterCaptura(): Observable<CapturaSistema> {
    return this.clienteHttp.get<CapturaSistema>(`${this.urlBase}/metricas`);
  }

  obterAlertas(): Observable<ItemAlerta[]> {
    return this.clienteHttp.get<ItemAlerta[]>(`${this.urlBase}/alertas`);
  }

  alternarMinerador(): Observable<StatusMineracao> {
    return this.clienteHttp.post<StatusMineracao>(`${this.urlBase}/minerador/alternar`, {});
  }
}
