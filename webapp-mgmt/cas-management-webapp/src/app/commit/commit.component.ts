import {Component, OnInit, Input, Inject} from '@angular/core';
import {Messages} from "../messages";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {DiffEntry} from "../../domain/diff-entry";

@Component({
  selector: 'app-commit',
  templateUrl: './commit.component.html',
  styleUrls: ['./commit.component.css']
})
export class CommitComponent implements OnInit {

  @Input()
  isAdmin: boolean;

  commitMessage: String;

  constructor(public dialogRef: MatDialogRef<CommitComponent>,
              @Inject(MAT_DIALOG_DATA) public changes: DiffEntry[],
              public messages: Messages) { }

  ngOnInit() {
  }

}
