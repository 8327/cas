import { Component, OnInit } from '@angular/core';
import {SurrogateRegisteredServiceAccessStrategy} from '../../../../domain/access-strategy';
import {Messages} from '../../../messages';
import {Data} from '../../data';
import {Util} from '../../../util/util';
import {Database, Datasource, Row} from '../../../database';

@Component({
  selector: 'app-surrogate',
  templateUrl: './surrogate.component.html',
  styleUrls: ['./surrogate.component.css']
})
export class SurrogateComponent implements OnInit {

  accessStrategy: SurrogateRegisteredServiceAccessStrategy;

  displayedColumns = ['source', 'mapped', 'delete'];
  attributeDatabase = new Database<Row>();

  dataSource: Datasource<Row> | null;

  constructor(public messages: Messages,
              private data: Data) {
    this.accessStrategy = data.service.accessStrategy as SurrogateRegisteredServiceAccessStrategy;
  }

  ngOnInit() {
    if (Util.isEmpty(this.accessStrategy.surrogateRequiredAttributes)) {
      this.accessStrategy.surrogateRequiredAttributes = new Map();
    }
    for (const p of Array.from(Object.keys(this.accessStrategy.surrogateRequiredAttributes))) {
      this.attributeDatabase.addItem(new Row(p));
    }
    this.dataSource = new Datasource(this.attributeDatabase);

  }

  addRow() {
    this.attributeDatabase.addItem(new Row(''));
  }

  doChange(row: Row, val: string) {
    this.accessStrategy.surrogateRequiredAttributes[val] = this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    this.attributeDatabase.removeItem(row);
  }
}
