import {Component, EventEmitter, HostListener, OnInit, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent<T> implements OnInit {
  @ViewChild("menuEl")
  menuEl: HTMLDivElement;
  menuTop: String;
  menuLeft: String;
  clickedAction: Element;
  selectedItem: T;

  @Output()
  selection: EventEmitter<String> = new EventEmitter<String>();

  constructor() { }

  ngOnInit() {
  }

  show(event: Event , item: T) {
    if (event.srcElement.tagName != "BUTTON") {
      this.clickedAction = event.srcElement.parentElement;
    } else {
      this.clickedAction = event.srcElement;
    }
    this.menuTop = this.clickedAction.getClientRects().item(0).top+"px";
    this.menuLeft = this.clickedAction.getClientRects().item(0).left+"px";
    this.selectedItem = item;
  }

  @HostListener('document:click',['$event.srcElement'])
  hide(src) {
    if(this.clickedAction && !this.clickedAction.contains(src))
      this.selectedItem = null;
  }

}
