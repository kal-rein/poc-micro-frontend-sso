import { loadRemoteModule } from '@angular-architects/native-federation';
import { Routes } from '@angular/router';

import { environment } from '@app/environment';

import { authCanActivate } from './authCanActivate';

let productionRoutes: Routes = [];
if (environment.production) {
  productionRoutes = [
    {
      path: 'app-2',
      loadComponent: () => loadRemoteModule('app-2', './Component').then((m) => m.AppComponent),
    },
    {
      path: 'app-3',
      loadComponent: () => loadRemoteModule('app-3', './Component').then((m) => m.AppComponent),
    },
  ];
}

export const routes: Routes = [
  {
    path: '',
    canActivate: [authCanActivate],
    children: [
      {
        path: 'app-1',
        loadComponent: () => loadRemoteModule('app-1', './Component').then((m) => m.AppComponent),
      },
      ...productionRoutes,
    ],
  },
];
