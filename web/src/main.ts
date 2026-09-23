import { bootstrapApplication } from '@angular/platform-browser';
import { configuracaoApp } from './app/app.config';
import { ComponenteRaiz } from './app/app';

bootstrapApplication(ComponenteRaiz, configuracaoApp)
  .catch((err) => console.error(err));
