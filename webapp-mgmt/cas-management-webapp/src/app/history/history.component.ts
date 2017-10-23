import {Component, OnInit, ViewChild} from '@angular/core';
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {HistoryService} from "./history.service";
import {History} from "../../domain/history";
import {Location} from "@angular/common";
import {DiffEntry} from "../../domain/diff-entry";
import {ChangesService} from "../changes/changes.service";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {Database, Datasource} from "../database";

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {

  displayedColumns = ['actions','message','committer','time'];
  database: Database<History> = new Database<History>();
  dataSource: Datasource<History> | null;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  fileName: String;

  selectedItem: History;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: HistoryService,
              private changeService: ChangesService,
              private location: Location,
              public  snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.dataSource = new Datasource(this.database, this.paginator);
    this.route.data
      .subscribe((data: { resp: History[]}) => {
        this.database.load(data.resp);
        if (!data.resp) {
          this.snackBar.open(this.messages.management_services_status_listfail,'dismiss', {
              duration: 5000
          });
        }
      });
    this.route.params.subscribe((params) => this.fileName = params['fileName']);
  }

  viewChange() {
    this.router.navigate(['/view',this.selectedItem.id]);
  }

  checkout() {
    this.service.checkout(this.selectedItem.commit as string,this.selectedItem.path)
      .then(resp => this.snackBar.open('Service successfully restored from history.', 'dismiss', {
        duration: 5000
      }));
  }

  viewDiff() {
    let diff: DiffEntry = new DiffEntry();
    diff.newId = this.selectedItem.id;
    diff.oldId = this.database.data[0].id;
    diff.path = this.selectedItem.path;
    diff.diff = "HISTORY";
    this.changeService.currentDiff = diff;
    this.router.navigate(['/diff']);
  }

  viewJSON() {
    this.router.navigate(['/json',this.selectedItem.id]);
  }
}


