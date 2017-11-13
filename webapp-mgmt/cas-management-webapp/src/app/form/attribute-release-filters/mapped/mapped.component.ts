import {Component, Input, OnInit} from '@angular/core';
import {RegisteredServiceMappedRegexAttributeFilter} from '../../../../domain/attribute-filter';
import {Data} from '../../data';
import {Messages} from '../../../messages';
import {MatTableDataSource} from '@angular/material';
import {Row} from '../../row';

@Component({
  selector: 'app-mapped',
  templateUrl: './mapped.component.html',
  styleUrls: ['./mapped.component.css']
})
export class MappedComponent implements OnInit {
    displayedColumns = ['source', 'mapped', 'delete'];
    dataSource: MatTableDataSource<Row> | null;
    formData;

    @Input('filter')
    filter: RegisteredServiceMappedRegexAttributeFilter;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
      this.dataSource = new MatTableDataSource([]);
      if (this.filter.patterns) {
          for (const p of Array.from(Object.keys(this.filter.patterns))) {
              this.dataSource.data.push(new Row(p));
          }
      }
  }

    addRow() {
        this.dataSource.data.push(new Row(''));
    }

    doChange(row: Row, val: string) {
        console.log(row.key + ' : ' + val);
        this.filter.patterns[val] = this.filter.patterns[row.key as string];
        delete this.filter.patterns[row.key as string];
        row.key = val;
    }

    delete(row: Row) {
        delete this.filter.patterns[row.key as string];
        this.dataSource.data.splice(this.dataSource.data.indexOf(row), 1);
    }

}
