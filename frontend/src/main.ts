import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Ponto de partida do Angular
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
