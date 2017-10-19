import { Component, OnInit, AfterViewInit, ViewChild } from '@angular/core';
import {ChangesService} from "../changes/changes.service";
import { Location } from "@angular/common";
import {Messages} from "../messages";
import { Editor } from "../editor.component";

@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.css']
})
export class DiffComponent implements AfterViewInit, OnInit {

  file: String;

  @ViewChild('editor')
  editor: Editor;

  constructor(public messages: Messages, private service: ChangesService, private location: Location) { }

  ngAfterViewInit() {
    this.service.getDiff(this.service.currentDiff)
      .then(diff => this.file = diff);
  }

  ngOnInit(){

  }
}
