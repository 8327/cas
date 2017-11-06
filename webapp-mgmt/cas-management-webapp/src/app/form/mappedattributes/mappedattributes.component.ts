import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {DataSource} from '@angular/cdk/table';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Data} from '../data';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {Database, Datasource, Row} from '../../database';

@Component({
  selector: 'app-mappedattributes',
  templateUrl: './mappedattributes.component.html',
  styleUrls: ['./mappedattributes.component.css']
})
export class MappedattributesComponent implements OnInit {
  formData: FormData;
  displayedColumns = ['source', 'mapped'];
  attributeDatabase = new Database<Row>();
  dataSource: Datasource<Row> | null;

  @Input()
  attributes: Map<String, String[]>;


  constructor(public messages: Messages,
              public data: Data,
              private changeDetector: ChangeDetectorRef) {
    this.formData = data.formData;
  }

  ngOnInit() {
    for (const key of Array.from(Object.keys(this.attributes))) {
      this.attributeDatabase.addItem(new Row(key as string));
    };

    this.dataSource = new Datasource(this.attributeDatabase);
    this.changeDetector.detectChanges();
  }

}

