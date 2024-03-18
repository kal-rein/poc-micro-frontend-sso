import { JsonPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { environment } from '@app/environment';
import { POC_AUTH_SERVICE_TOKEN } from '@lib/poc-auth';

import { BackService } from './back.service';
import { User } from './user';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [JsonPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {

  private readonly authService = inject(POC_AUTH_SERVICE_TOKEN);
  private readonly backService = inject(BackService);

  protected readonly appName = environment.appName;
  protected readonly hostUser = toSignal(this.authService.user$);
  protected readonly user = signal<User | undefined>(undefined);

  ngOnInit(): void {
    this.backService.me().subscribe(user => this.user.set(user));
  }
}
