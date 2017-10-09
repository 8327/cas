import { Component, OnInit, ViewChild } from '@angular/core';
import {SubmitService} from "./submits.service";
import { Branch } from "../../domain/branch";
import {Location} from "@angular/common";
import {NotesService} from "../notes/notes.service";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {DataSource} from "@angular/cdk/collections";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-submits',
  templateUrl: './submits.component.html',
  styleUrls: ['./submits.component.css']
})

export class SubmitsComponent implements OnInit {
  displayedColumns = ['branch','message','status','actions'];
  database = new BranchDatabase();
  dataSource: BranchDataSource | null;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  note: String;
  showNote: boolean;
  revertItem: Branch;

  constructor(private service: SubmitService,
              private location: Location,
              private notesService: NotesService) { }

  ngOnInit() {
    this.dataSource = new BranchDataSource(this.database, this.paginator);
    this.service.getSubmits().then(resp => this.database.load(resp));
  }

  goBack() {
    this.location.back();
  }

  cancel(note: String) {
    this.note = "";
    this.showNote = false;
  }

  getNotes(branch: String) {
    this.notesService.getNotes(branch)
      .then(resp => this.handleNotes(resp));
  }

  handleNotes(resp: String) {
    this.note = resp;
    this.showNote = true;
  }

  status(branch: Branch): String {
    if (branch.accepted) {
      return "Accepted";
    } else if (branch.reverted) {
      return "Reverted";
    } else if (branch.rejected) {
      return "Rejected";
    } else {
      return "Pending";
    }
  }

  openModalRevert(branch: Branch) {
    this.revertItem = branch;
  }

  closeModalRevert() {
    this.revertItem = null;
  }

  revert(branch: Branch) {
    this.service.revert(branch.name as string).then(resp => this.refresh());
    this.closeModalRevert();
  }

  refresh() {
    this.ngOnInit();
  }

  handleRevert(msg: String) {
    if (msg == 'cancel') {
      this.closeModalRevert();
    } else {
      this.revert(this.revertItem);
    }
  }

}

export class BranchDatabase {
    dataChange: BehaviorSubject<Branch[]> = new BehaviorSubject<Branch[]>([]);
    get data(): Branch[] { return this.dataChange.value; }

    constructor() {
    }

    load(branches: Branch[]) {
        this.dataChange.next([]);
        for(let branch of branches) {
            this.addService(branch);
        }
    }

    addService(branch: Branch) {
        const copiedData = this.data.slice();
        copiedData.push(branch);
        this.dataChange.next(copiedData);
    }
}

export class BranchDataSource extends DataSource<any> {

    constructor(private _changesDatabase: BranchDatabase, private _paginator: MatPaginator) {
        super();
    }

    connect(): Observable<Branch[]> {
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
