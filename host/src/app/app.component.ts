import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterOutlet } from '@angular/router';

import { environment } from '@app/environment';
import { POC_AUTH_SERVICE_TOKEN } from '@lib/poc-auth';

@Component({
  selector: 'host-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  private readonly authService = inject(POC_AUTH_SERVICE_TOKEN);

  protected readonly isProduction = environment.production;
  protected readonly user = toSignal(this.authService.user$);

  protected logout(): void {
    this.authService.logout();
  }
}
