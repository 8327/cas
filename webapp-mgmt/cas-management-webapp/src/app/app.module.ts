import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {Messages} from "./messages";
import {AppRoutingModule} from "./app-routing.module";
import {ServicesModule} from "./services/services.module";
import {HeaderComponent} from "./header/header.component";
import {SharedModule} from "./shared/shared.module";
import {FormModule} from "./form/form.module";
import { DeleteComponent } from './delete/delete.component';
import {DomainsModule} from "./domains/domains.module";
import { SearchComponent } from './search/search.component';
import {SearchService} from "./search/SearchService";

import {MATERIAL_COMPATIBILITY_MODE} from '@angular/material';
import {ControlsComponent} from "./controls/controls.component";
import {ControlsService} from "./controls/controls.service";
import {UserService} from "./user.service";
import {CommitComponent} from "./commit/commit.component";
import {HistoryModule} from "./history/history.module";
import {RevertComponent} from "./revert/revert.component";
import {ChangesModule} from "./changes/changes.module";
import {DiffComponent} from "./diff/diff.component";
import {DiffModule} from "./diff/diff.module";
import {JSONModule} from "./json/json.module";
import {HeaderService} from "./header/header.service";
import {InitComponent} from "app/init.component";
import { LocalChangesComponent } from './local-changes/local-changes.component';
import {PullModule} from "./pull/pull.module";
import {SubmitModule} from "./submits/submits.module";
import {NotesModule} from "./notes/notes.module";
import {RejectComponent} from "./reject/reject.component";
import {AcceptComponent} from "./accept/accept.component";


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpModule,
    DomainsModule,
    ServicesModule,
    HistoryModule,
    JSONModule,
    ChangesModule,
    DiffModule,
    PullModule,
    SubmitModule,
    NotesModule,
    FormModule,
    SharedModule,
    AppRoutingModule
  ],
  declarations: [
    AppComponent,
    HeaderComponent,
    DeleteComponent,
    SearchComponent,
    RevertComponent,
    AcceptComponent,
    RejectComponent,
    InitComponent,
    LocalChangesComponent
  ],
  entryComponents: [
    DeleteComponent,
    RevertComponent,
    AcceptComponent,
    RejectComponent
  ],
  providers: [
    Messages,
    SearchService,
    UserService,
    HeaderService,
    {provide: MATERIAL_COMPATIBILITY_MODE, useValue: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
