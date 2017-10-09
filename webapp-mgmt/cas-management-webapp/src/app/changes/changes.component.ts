import { Component, OnInit, ViewChild } from '@angular/core';
import {ChangesService} from "./changes.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DiffEntry} from "../../domain/diff-entry";
import {Location} from "@angular/common";
import {Messages} from "../messages";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {DataSource} from "@angular/cdk/collections";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-changes',
  templateUrl: './changes.component.html',
  styleUrls: ['./changes.component.css']
})

export class ChangesComponent implements OnInit {
    displayedColumns = ['file','change','actions'];
    database = new ChangesDatabase();
    dataSource: ChangesDataSource | null;

    @ViewChild(MatPaginator) paginator: MatPaginator;

    constructor(public messages: Messages,
                public router: Router,
                public route: ActivatedRoute,
                public location: Location,
                private service: ChangesService,
                public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new ChangesDataSource(this.database, this.paginator);
    this.route.data
      .subscribe((data: { resp: DiffEntry[]}) => {
        this.database.load(data.resp);
      });
  }

  viewDiff(diff: DiffEntry) {
    this.service.currentDiff = diff;
    this.router.navigate(['/diff']);
  }

  viewChange(diff: DiffEntry) {
    this.router.navigate(['/view',diff.newId]);
  }

  goBack() {
    this.location.back();
  }
}

export class ChangesDatabase {
  dataChange: BehaviorSubject<DiffEntry[]> = new BehaviorSubject<DiffEntry[]>([]);
  get data(): DiffEntry[] { return this.dataChange.value; }

  constructor() {
  }

  load(changes: DiffEntry[]) {
    this.dataChange.next([]);
    for(let change of changes) {
      this.addService(change);
    }
  }

  addService(change: DiffEntry) {
    const copiedData = this.data.slice();
    copiedData.push(change);
    this.dataChange.next(copiedData);
  }
}

export class ChangesDataSource extends DataSource<any> {

  constructor(private _changesDatabase: ChangesDatabase, private _paginator: MatPaginator) {
      super();
  }

  connect(): Observable<DiffEntry[]> {
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
