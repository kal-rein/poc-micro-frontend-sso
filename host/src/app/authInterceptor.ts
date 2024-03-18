import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, NEVER, throwError } from 'rxjs';

import { environment } from '@app/environment';

const rootDomainApiRegex = new RegExp(`^https?:\/\/.*\.?${environment.rootDomain}\/api\/?.*$`);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  let newReq = req;
  if (rootDomainApiRegex.test(newReq.url)) {
    newReq = req.clone({ withCredentials: true });
  }

  return next(newReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const currentUri = encodeURIComponent(window.location.href);
        const redirectUri = environment.loginUrl.replace('{redirectUri}', currentUri);
        window.open(redirectUri, '_self');

        return NEVER;
      }

      return throwError(() => error);
    }),
  );
};
