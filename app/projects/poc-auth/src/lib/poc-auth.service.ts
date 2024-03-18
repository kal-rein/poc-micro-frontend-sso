import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

import { PocUser } from './poc-user';

export const POC_AUTH_SERVICE_TOKEN = new InjectionToken<PocAuthService>('POC_AUTH_SERVICE_TOKEN');

export interface PocAuthService {
  readonly user$: Observable<PocUser | undefined>;
  me(): Observable<PocUser>;
  logout(): void;
}
