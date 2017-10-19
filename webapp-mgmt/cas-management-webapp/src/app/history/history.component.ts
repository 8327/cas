import {Component, OnInit, ViewChild} from '@angular/core';
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {HistoryService} from "./history.service";
import {History} from "../../domain/history";
import {Location} from "@angular/common";
import {DiffEntry} from "../../domain/diff-entry";
import {ChangesService} from "../changes/changes.service";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {DataSource} from "@angular/cdk/collections";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {

  displayedColumns = ['actions','message','committer','time'];
  database = new HistoryDatabase();
  dataSource: HistoryDataSource | null;

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
    this.dataSource = new HistoryDataSource(this.database, this.paginator);
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

export class HistoryDatabase {
    dataChange: BehaviorSubject<History[]> = new BehaviorSubject<History[]>([]);
    get data(): History[] { return this.dataChange.value; }

    constructor() {
    }

    load(histories: History[]) {
        this.dataChange.next([]);
        for(let history of histories) {
            this.addService(history);
        }
    }

    addService(history: History) {
        const copiedData = this.data.slice();
        copiedData.push(history);
        this.dataChange.next(copiedData);
    }
}

export class HistoryDataSource extends DataSource<any> {

    constructor(private _changesDatabase: HistoryDatabase, private _paginator: MatPaginator) {
        super();
    }

    connect(): Observable<History[]> {
        const displayDataChanges = [
            this._changesDatabase.dataChange,
            this._paginator.page,
        ];

        return Observable.merge(...displayDataChanges).map(() => {
            const data = this._changesDatabase.data.slice();
            const startIndex = this._paginator.pageIndex * this._paginator.pageSize;
            return data.splice(startIndex, this._paginator.pageSize);
        });
    }

    disconnect() {}
}
