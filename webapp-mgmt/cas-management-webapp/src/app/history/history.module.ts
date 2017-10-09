/**
 * Created by tschmidt on 2/23/17.
 */
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { SharedModule } from "../shared/shared.module";
import { HistoryComponent } from "./history.component";
import { HistoryResolve } from "./history.resolover";
import { HistoryService } from "./history.service";
import { HistoryMenuComponent } from './menu/menu.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    HttpModule,
    SharedModule
  ],
  declarations: [
    HistoryComponent,
    HistoryMenuComponent
  ],
  providers: [
    HistoryResolve,
    HistoryService
  ]
})

export class HistoryModule {}

