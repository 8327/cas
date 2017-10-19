/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core'
import { RouterModule } from '@angular/router';
import {ServicesComponent} from "./services/services.component";
import {ServicesResolve} from "./services/services.resolover";
import {DomainsComponent} from "./domains/domains.component";
import {SearchComponent} from "./search/search.component";
import {InitComponent} from "./init.component";
import {HistoryComponent} from "./history/history.component";
import {HistoryResolve} from "./history/history.resolover";
import {DiffComponent} from "./diff/diff.component";
import {JSONComponent} from "./json/json.component";
import {JSONResolver} from "./json/json.resolover";

@NgModule({
  imports: [
    RouterModule.forRoot( [
      {
        path: 'domains',
        component: DomainsComponent,
      },
      {
        path: 'services/:domain',
        component: ServicesComponent,
        resolve: {
          resp: ServicesResolve
        }
      },
      {
        path: 'search/:query',
        component: SearchComponent
      },
      {
        path: 'history/:fileName',
        component: HistoryComponent,
        resolve: {
            resp: HistoryResolve
        }
      },
      {
        path: 'diff',
        component: DiffComponent
      },
      {
        path: 'json/:fileId',
        component: JSONComponent,
        resolve: {
          resp: JSONResolver
        }
      },
      {
        path: 'manage.html',
        component: InitComponent
      },
    ]),
  ],
  exports: [ RouterModule ]
})

export class AppRoutingModule {}
