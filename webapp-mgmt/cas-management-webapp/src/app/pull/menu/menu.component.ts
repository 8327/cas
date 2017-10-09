import { Component } from '@angular/core';
import {Branch} from "../../../domain/branch";
import {MenuComponent} from "../../menu/menu.component";

@Component({
  selector: 'app-pull-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class PullMenuComponent extends MenuComponent<Branch> {}

