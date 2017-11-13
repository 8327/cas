import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {Data} from '../data';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {MatTableDataSource} from '@angular/material';
import {Row} from '../row';

@Component({
  selector: 'app-rejectedattributes',
  templateUrl: './rejectedattributes.component.html',
  styleUrls: ['./rejectedattributes.component.css']
})
export class RejectedattributesComponent implements OnInit {

  displayedColumns = ['source', 'mapped', 'delete'];
  dataSource: MatTableDataSource<Row> | null;

  @Input()
  attributes: Map<String, String[]>;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    this.dataSource = new MatTableDataSource([]);
    for (const p of Array.from(Object.keys(this.attributes))) {
      this.dataSource.data.push(new Row(p));
    }

  }

  addRow() {
    this.dataSource.data.push(new Row(''));
  }

  doChange(row: Row, val: string) {
    this.attributes[val] = this.attributes[row.key as string];
    delete this.attributes[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
   delete this.attributes[row.key as string];
   this.dataSource.data.splice(this.dataSource.data.indexOf(row), 1);
  }
}


