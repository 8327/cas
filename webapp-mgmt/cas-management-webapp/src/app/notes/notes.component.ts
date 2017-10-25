import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Editor } from "../editor.component";
import { Messages } from "../messages";

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.css']
})

export class NotesComponent implements OnInit {
  @Input()
  note: String;

  @Output()
  commit: EventEmitter<String> = new EventEmitter<String>();

  @ViewChild('editor')
  editor: Editor;

  @Input()
  viewOnly: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

  saveNote() {
    this.commit.emit(this.editor.getFile());
  }

}
