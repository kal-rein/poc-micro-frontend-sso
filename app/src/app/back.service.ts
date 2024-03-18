import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '@app/environment';

import { User } from './user';

@Injectable({ providedIn: 'root' })
export class BackService {

  private readonly http = inject(HttpClient);

  me(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/v1/user/me`);
  }
}
