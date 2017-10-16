import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ServiceItem} from "../../domain/service-view-bean";
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceViewService} from "./service.service";
import {Location} from "@angular/common";
import {MatDialog, MatPaginator, MatSnackBar} from "@angular/material";
import {DeleteComponent} from "../delete/delete.component";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import {ControlsService} from "../controls/controls.service";
import {RevertComponent} from "../revert/revert.component";

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit,AfterViewInit {

  dataTable: ServiceItem[];
  deleteItem: ServiceItem;
  domain: String;
  selectedItem: ServiceItem;
  revertItem: ServiceItem;
  servicesDatabase = new ServicesDatabase();

  @ViewChild("paginator")
  paginator: MatPaginator;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: ServiceViewService,
              private location: Location,
              public dialog: MatDialog,
              public snackBar: MatSnackBar,
              public controlsService: ControlsService) {
    this.dataTable = [];
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: ServiceItem[]}) => {
        if (!data.resp) {
          this.snackBar.open(this.messages.management_services_status_listfail,'dismiss',{
            duration: 5000
          });
        }
        this.servicesDatabase.load(data.resp);
      });
    this.route.params.subscribe((params) => this.domain = params['domain']);
  }

  ngAfterViewInit() {
    const displayDataChanges = [
      this.servicesDatabase.dataChange,
      this.paginator.page,
    ];

    Observable.merge(...displayDataChanges).map(() => {
      const data = this.servicesDatabase.data.slice();
      const startIndex = this.paginator.pageIndex * this.paginator.pageSize;
      return data.splice(startIndex, this.paginator.pageSize);
    }).subscribe((d) => setTimeout(() => this.dataTable = d,0));
  }

  serviceEdit(item?: ServiceItem) {
    if (item) {
      this.selectedItem = item;
    }
    this.router.navigate(['/form',this.selectedItem.assignedId, {duplicate: false, change: false}]);
  }

  serviceDuplicate() {
    this.router.navigate(['/form',this.selectedItem.assignedId, {duplicate: true, change: false}]);
  }

  openModalDelete() {
    let dialogRef = this.dialog.open(DeleteComponent,{
      data: this.selectedItem,
      width: '500px',
      position: {top: '100px'}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.delete();
      }
    });
    this.deleteItem = this.selectedItem;
  };

  openModalRevert() {
    let dialogRef = this.dialog.open(RevertComponent,{
      data: this.selectedItem,
      width: '500px',
      position: {top: '100px'}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.revert();
      }
    });
    this.revertItem = this.selectedItem;
  };

  delete() {
    let myData = {id: this.deleteItem.assignedId};

    this.service.delete(Number.parseInt(this.deleteItem.assignedId as string))
      .then(resp => this.handleDelete(resp))
      .catch((e: any) => this.snackBar.open(e.message || e.text(), 'Dismiss', {
        duration: 5000
      }));
  };

  handleDelete(name: String) {
    this.snackBar.open(name+" "+this.messages.management_services_status_deleted,'Dismiss', {
      duration: 5000
    });
    this.refresh();
  }

  history() {
    let fileName: string = (this.selectedItem.name + '-' + this.selectedItem.assignedId + ".json").replace(/ /g,"");
    this.router.navigate(['/history',fileName]);
  }

  revert() {
    let fileName: string = (this.revertItem.name + '-' + this.revertItem.assignedId + ".json").replace(/ /g,"");
    if(this.controlsService.changeStyle(this.revertItem.assignedId) === 'deleted') {
      this.service.revertDelete(fileName)
          .then(resp => this.refresh());
    } else {
      this.service.revert(fileName)
          .then(resp => this.refresh());
    }
  }


  refresh() {
    this.getServices();
    this.controlsService.untracked();
  }

  getServices() {
    this.service.getServices(this.domain)
      .then(resp => this.servicesDatabase.load(resp))
      .catch((e: any) => this.snackBar.open(this.messages.management_services_status_listfail,'Dismiss', {
        duration: 5000
      }));
  }

  goBack() {
    this.location.back();
  }

  moveUp(a: ServiceItem) {
    let index: number = this.servicesDatabase.data.indexOf(a);
    if(index > 0) {
      let b: ServiceItem = this.servicesDatabase.data[index - 1];
      a.evalOrder = index-1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  moveDown(a: ServiceItem) {
    let index: number = this.servicesDatabase.data.indexOf(a);
    if(index < this.servicesDatabase.data.length -1) {
      let b: ServiceItem = this.servicesDatabase.data[index + 1];
      a.evalOrder = index+1;
      b.evalOrder = index;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  json() {

  }

}

export class ServicesDatabase {
  dataChange: BehaviorSubject<ServiceItem[]> = new BehaviorSubject<ServiceItem[]>([]);
  get data(): ServiceItem[] { return this.dataChange.value; }

  constructor() {
  }

  load(services: ServiceItem[]) {
    this.dataChange.next([]);
    for(let service of services) {
      this.addService(service);
    }
  }

  addService(service: ServiceItem) {
    const copiedData = this.data.slice();
    copiedData.push(service);
    this.dataChange.next(copiedData);
  }
}
