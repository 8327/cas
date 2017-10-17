import { Input, Component, OnInit } from '@angular/core';

declare var ace: any;

@Component({
    selector: 'agg-editor',
    template: `
         <div id="editor" style="width:100%;height:100%;"></div>
    `
})

export class Editor implements OnInit {
  @Input()
  mode: String = "text";
  editor: any;

  ngOnInit() {
    this.editor = ace.edit("editor");
    this.editor.getSession().setUseWrapMode(true);
    this.editor.setPrintMarginColumn(180);
    var EditorMode = ace.require("ace/mode/"+this.mode).Mode;
    var Vim = ace.require("ace/keyboard/vim");
    this.editor.session.setMode(new EditorMode());
    this.editor.setKeyboardHandler(Vim.handler);
    this.editor.setFontSize(14);
    this.editor.$blockScrolling = Infinity;
  }

  @Input()
  set file(file: String) {
    if (this.editor) {
      this.editor.setValue(file ? file : "");
    }
    setTimeout(() => {
      this.editor.focus();
      this.editor.navigateTo(0,0);
    },100);
  }


  public getFile() {
    return this.editor.getValue();
  }

  onResize(evt:Event) {
    setTimeout(() => {
        this.editor.resize(true);
    },10);
  }

  public resize() {
    this.editor.resize(true);
  }

  destroy() {
    this.editor.destroy();
  }
}
