import {Component, EventEmitter, HostListener, OnInit, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent<T> implements OnInit {

  @Output()
  selection: EventEmitter<String> = new EventEmitter<String>();

  constructor() { }

  ngOnInit() {
  }

}
