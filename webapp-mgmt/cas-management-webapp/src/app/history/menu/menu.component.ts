import {Component} from '@angular/core';
import {History} from "../../../domain/history";
import {MenuComponent} from "../../menu/menu.component";

@Component({
  selector: 'app-history-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class HistoryMenuComponent extends MenuComponent<History>{}
