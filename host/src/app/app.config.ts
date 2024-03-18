import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { POC_AUTH_SERVICE_TOKEN } from '@lib/poc-auth';

import { routes } from './app.routes';
import { AuthService } from './auth.service';
import { authInterceptor } from './authInterceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),

    provideRouter(routes),

    { provide: POC_AUTH_SERVICE_TOKEN, useClass: AuthService },
  ],
};
