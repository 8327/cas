import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {Data} from '../data';
import {DataSource} from '@angular/cdk/table';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Util} from '../../util/util';
import {Database, Datasource, Row} from '../../database';

@Component({
  selector: 'app-rejectedattributes',
  templateUrl: './rejectedattributes.component.html',
  styleUrls: ['./rejectedattributes.component.css']
})
export class RejectedattributesComponent implements OnInit {

  displayedColumns = ['source', 'mapped', 'delete'];
  attributeDatabase = new Database<Row>();
  dataSource: Datasource<Row> | null;

  @Input()
  attributes: Map<String, String[]>;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    for (const p of Array.from(Object.keys(this.attributes))) {
      this.attributeDatabase.addItem(new Row(p));
    }
    this.dataSource = new Datasource(this.attributeDatabase);
  }

  addRow() {
    this.attributeDatabase.addItem(new Row(''));
  }

  doChange(row: Row, val: string) {
    this.attributes[val] = this.attributes[row.key as string];
    delete this.attributes[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
   delete this.attributes[row.key as string];
   this.attributeDatabase.removeItem(row);
  }
}


