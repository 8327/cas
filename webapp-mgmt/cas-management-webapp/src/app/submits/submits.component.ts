import { Component, OnInit, ViewChild } from '@angular/core';
import {SubmitService} from "./submits.service";
import { Branch } from "../../domain/branch";
import {Database, Datasource} from "../database";
import {MatDialog, MatPaginator, MatSnackBar} from "@angular/material";
import {RevertComponent} from "../revert/revert.component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-submits',
  templateUrl: './submits.component.html',
  styleUrls: ['./submits.component.css']
})

export class SubmitsComponent implements OnInit {

  displayedColumns = ['actions', 'status', 'name', 'message'];
  database: Database<Branch> = new Database<Branch>();
  dataSource: Datasource<Branch> | null;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  selectedItem: Branch;
  revertBranch: Branch;

  constructor(private service: SubmitService,
              public dialog: MatDialog,
              public snackBar: MatSnackBar,
              public router: Router) { }

  ngOnInit() {
    this.dataSource = new Datasource(this.database, this.paginator);
    this.service.getSubmits().then(resp => this.database.load(resp));
  }


  getNotes(branch: String) {
    this.router.navigate(['notes', this.selectedItem.id]);
  }


  status(branch: Branch): String {
    if (!branch) {
      return;
    }
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
    let dialogRef = this.dialog.open(RevertComponent,{
      data: this.selectedItem,
      width: '500px',
      position: {top: '100px'}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.revert();
      }
    });
    this.revertBranch = this.selectedItem;
  }

  revert() {
    this.service.revert(this.revertBranch.name as string)
      .then(resp => {
        this.snackBar.open("Branch has been reverted", "dismiss", {
          duration: 5000
        });
        this.refresh()
      });
  }

  refresh() {
    this.ngOnInit();
  }

}
