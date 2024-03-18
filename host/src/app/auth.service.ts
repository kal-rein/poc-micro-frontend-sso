import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { environment } from '@app/environment';
import { PocAuthService, PocUser } from '@lib/poc-auth';

@Injectable()
export class AuthService implements PocAuthService {

  private readonly http = inject(HttpClient);

  private readonly user = new BehaviorSubject<PocUser | undefined>(undefined);

  get user$(): Observable<PocUser | undefined> {
    return this.user.asObservable();
  }

  me(): Observable<PocUser> {
    return this.http.get<PocUser>(`${environment.apiUrl}/v1/user/me`).pipe(
      tap(user => this.user.next(user)),
    );
  }

  logout(): void {
    window.open(environment.logoutUrl, '_self');
  }
}
