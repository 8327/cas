import {Component, OnInit} from '@angular/core';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {Data} from '../data';
import {WsFederationClaimsReleasePolicy} from '../../../domain/attribute-release';
import {Util} from '../../util/util';
import {Database, Datasource, Row} from '../../database';


@Component({
  selector: 'app-wsfedattrrelpolicies',
  templateUrl: './wsfedattrrelpolicies.component.html',
  styleUrls: ['./wsfedattrrelpolicies.component.css']
})
export class WsfedattrrelpoliciesComponent implements OnInit {

  formData: FormData;
  wsFedOnly: boolean;

  displayedColumns = ['source', 'mapped'];
  attributeDatabase = new Database<Row>();
  dataSource: Datasource<Row> | null;


  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
    const attrPolicy: WsFederationClaimsReleasePolicy = this.data.service.attributeReleasePolicy as WsFederationClaimsReleasePolicy;
    if (Util.isEmpty(attrPolicy.allowedAttributes)) {
      attrPolicy.allowedAttributes = new Map();
    }

    this.formData.availableAttributes.forEach((k) => {
      attrPolicy.allowedAttributes[k as string] = k;
    });

    for (const key of Array.from(Object.keys(attrPolicy.allowedAttributes))) {
      this.attributeDatabase.addItem(new Row(key as string));
    };

    this.dataSource = new Datasource(this.attributeDatabase);
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length === 0;
  }

}
