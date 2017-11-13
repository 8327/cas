import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Data} from '../data';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {MatTableDataSource} from '@angular/material';
import {Row} from '../row';

@Component({
  selector: 'app-mappedattributes',
  templateUrl: './mappedattributes.component.html',
  styleUrls: ['./mappedattributes.component.css']
})
export class MappedattributesComponent implements OnInit {
  formData: FormData;
  displayedColumns = ['source', 'mapped'];
  dataSource: MatTableDataSource<Row> | null;

  @Input()
  attributes: Map<String, String[]>;


  constructor(public messages: Messages,
              public data: Data,
              private changeDetector: ChangeDetectorRef) {
    this.formData = data.formData;
  }

  ngOnInit() {
    this.dataSource = new MatTableDataSource([]);
    for (const key of Array.from(Object.keys(this.attributes))) {
      this.dataSource.data.push(new Row(key as string));
    };
    this.changeDetector.detectChanges();
  }

}

