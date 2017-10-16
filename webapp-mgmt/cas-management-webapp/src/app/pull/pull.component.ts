import {Component, OnInit, ViewChild } from '@angular/core';
import {Messages} from "../messages";
import {Router} from "@angular/router";
import { Branch } from "../../domain/branch";
import {PullService} from "./pull.service";
import { Location } from "@angular/common";
import {NotesService} from "../notes/notes.service";
import {ChangesService} from "../changes/changes.service";
import {DiffEntry} from "../../domain/diff-entry";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {DataSource} from "@angular/cdk/collections";
import {Observable} from "rxjs/Observable";
import {MatPaginator, MatSnackBar} from "@angular/material";

@Component({

  selector: 'app-pull',
  templateUrl: './pull.component.html',
  styleUrls: ['./pull.component.css']
})

export class PullComponent implements OnInit {
  displayedColumns = ['branch','message','status','actions'];
  database = new BranchDatabase();
  dataSource: BranchDataSource | null;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  note: String;
  showNote: boolean;
  noteBranch: String;
  showReject: boolean;
  rejectBranch: Branch;
  acceptBranch: Branch;
  showAccept: boolean;
  changes: DiffEntry[];

  selectedItem: Branch;
  showPending: boolean = true;
  showAccepted: boolean;
  showRejected: boolean;

  constructor(public messages: Messages,
              private router: Router,
              private service: PullService,
              private location: Location,
              private notesService: NotesService,
              private changeService: ChangesService,
              public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new BranchDataSource(this.database, this.paginator);
    this.service.getBranches([this.showPending,this.showAccepted, this.showRejected])
        .then(resp => this.database.load(resp));
  }

  viewChanges() {
    this.router.navigate(['/changes',this.selectedItem.name]);
  }

  acceptBranchModal() {
    this.acceptBranch = this.selectedItem;
    this.changeService.getChanges(this.acceptBranch.id)
      .then(resp => this.handleChanges(resp));
  }

  handleChanges(resp: DiffEntry[]) {
    this.changes = resp;
    this.showAccept = true;
  }

  accept(note: String) {
    this.showAccept = false;
    if (note != 'CANCEL') {
      this.service.accept(this.acceptBranch, note)
        .then(resp => this.snackBar.open("Branch has been merged", "dismiss", {
            duration: 5000
        }));
    }
  }

  rejectBranchModal() {
    this.showReject = true;
    this.rejectBranch = this.selectedItem;
  }

  reject(note: String) {
    this.showReject = false;
    if (note != 'CANCEL') {
      this.service.reject(this.rejectBranch, note)
        .then(resp => this.snackBar.open("Branch has beem marked as rejected","dismiss", {
            duration: 5000
        }));
    }
  }

  goBack() {
    this.location.back();
  }

  refresh() {
    this.ngOnInit();
  }

  addNote() {
    this.noteBranch = this.selectedItem.id;
    this.note = "";
    this.showNote = true;
  }

  cancel() {
    this.note = "";
    this.showNote = false;
  }

  getNotes() {
    this.notesService.getNotes(this.selectedItem.id)
      .then(resp => this.handleNote(resp));
  }

  handleNote(note: String) {
    this.note = note;
    this.showNote = true;
  }

  saveNote(note: String) {
    if(note === 'CANCEL') {
      this.cancel();
    } else {
      this.notesService.addNote(this.noteBranch, note)
        .then(resp => this.handleSaveNote(resp));
    }
  }

  handleSaveNote(msg: String) {
    this.note = null;
    this.showNote = false;
    this.snackBar.open(msg as string,'dismiss',{
        duration: 5000
    });
  }

  status(branch: Branch): String {
    if(branch.accepted) {
      return "Accepted";
    } else if (branch.rejected) {
      return "Rejected";
    } else {
      return "Pending";
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
