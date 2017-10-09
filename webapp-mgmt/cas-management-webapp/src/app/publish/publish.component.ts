import {Component, OnInit, Inject} from '@angular/core';
import {Messages} from "../messages";
import {Commit} from "../../domain/commit";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-publish',
  templateUrl: './publish.component.html',
  styleUrls: ['./publish.component.css']
})

export class PublishComponent implements OnInit {

    constructor(public dialogRef: MatDialogRef<PublishComponent>,
                @Inject(MAT_DIALOG_DATA) public changes: Commit[],
                public messages: Messages) { }

    ngOnInit() {
    }
}
