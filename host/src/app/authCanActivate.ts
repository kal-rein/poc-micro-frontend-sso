import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { catchError, first, map, of, switchMap } from 'rxjs';

import { POC_AUTH_SERVICE_TOKEN } from '@lib/poc-auth';

export const authCanActivate: CanActivateFn = () => {
  const authService = inject(POC_AUTH_SERVICE_TOKEN);

  return authService.user$.pipe(
    first(),
    map(user => user != null),
    switchMap(canActivate => canActivate ? of(true) : authService.me().pipe(
      map(() => true),
      catchError(() => of(false)),
    )),
  );
};
