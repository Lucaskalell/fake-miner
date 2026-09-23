export interface MetricasHardware {
  temperaturaCpu: number;
  usoCpu: number;
  frequenciaCpuGhz: number;
  memoriaUsadaGb: number;
  memoriaTotalGb: number;
  percentualMemoriaUsada: number;
  swapUsadaGb: number;
  uptimeSegundos: number;
}

export interface StatusMineracao {
  ativo: boolean;
  hashRateAtual: number;
  hashRateMedio: number;
  consumoWatts: number;
  eficiencia: number;
  lucroEstimadoDolar: number;
  progressoMetaDiaria: number;
  inicioSessao: string;
}

export interface CapturaSistema {
  hardware: MetricasHardware;
  mineracao: StatusMineracao;
}

export interface ItemAlerta {
  mensagem: string;
  severidade: string;
  timestamp: string;
}
