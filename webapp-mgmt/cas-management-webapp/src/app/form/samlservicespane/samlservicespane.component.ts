import {Component, OnInit} from '@angular/core';
import {Messages} from '../../messages';
import {SamlRegisteredService} from '../../../domain/saml-service';
import {Data} from '../data';
import {Util} from '../../util/util';
import {Database, Datasource, Row} from '../../database';

@Component({
  selector: 'app-samlservicespane',
  templateUrl: './samlservicespane.component.html',
  styleUrls: ['./samlservicespane.component.css']
})
export class SamlservicespaneComponent implements OnInit {

  displayedColumns = ['source', 'mapped', 'delete'];
  attributeDatabase = new Database<Row>();
  dataSource: Datasource<Row> | null;

  type: String;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService;

    if (Util.isEmpty(service.attributeNameFormats)) {
      service.attributeNameFormats = new Map();
    }
    for (const p of Array.from(Object.keys(service.attributeNameFormats))) {
      this.attributeDatabase.addItem(new Row(p));
    }
    this.dataSource = new Datasource(this.attributeDatabase);
  }

  addRow() {
    this.attributeDatabase.addItem(new Row(''));
  }

  doChange(row: Row, val: string) {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService;
    service.attributeNameFormats[val] = service.attributeNameFormats[row.key as string];
    delete service.attributeNameFormats[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService
    delete service.attributeNameFormats[row.key as string];
    this.attributeDatabase.removeItem(row);
  }
}

