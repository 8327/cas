import {Component, Input, OnInit} from '@angular/core';
import {RegisteredServiceMappedRegexAttributeFilter} from '../../../../domain/attribute-filter';
import {Data} from '../../data';
import {Messages} from '../../../messages';
import {Database, Datasource, Row} from '../../../database';

@Component({
  selector: 'app-mapped',
  templateUrl: './mapped.component.html',
  styleUrls: ['./mapped.component.css']
})
export class MappedComponent implements OnInit {
    displayedColumns = ['source', 'mapped', 'delete'];
    attributeDatabase = new Database<Row>();
    dataSource: Datasource<Row> | null;
    formData;

    @Input('filter')
    filter: RegisteredServiceMappedRegexAttributeFilter;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
      this.dataSource = new Datasource(this.attributeDatabase);
      if (this.filter.patterns) {
          for (const p of Array.from(Object.keys(this.filter.patterns))) {
              this.attributeDatabase.addItem(new Row(p));
          }
      }
  }

    addRow() {
        this.attributeDatabase.addItem(new Row(''));
    }

    doChange(row: Row, val: string) {
        console.log(row.key + ' : ' + val);
        this.filter.patterns[val] = this.filter.patterns[row.key as string];
        delete this.filter.patterns[row.key as string];
        row.key = val;
    }

    delete(row: Row) {
        delete this.filter.patterns[row.key as string];
        this.attributeDatabase.removeItem(row);
    }

}
