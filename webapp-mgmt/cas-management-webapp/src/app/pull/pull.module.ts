/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {PullComponent} from "./pull.component";
import {PullService} from "./pull.service";
import {SharedModule} from "../shared/shared.module";
import { PullMenuComponent } from './menu/menu.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpModule,
    SharedModule
  ],
  declarations: [
    PullComponent,
    PullMenuComponent
  ],
  providers: [
    PullService
  ]
})

export class PullModule {}
