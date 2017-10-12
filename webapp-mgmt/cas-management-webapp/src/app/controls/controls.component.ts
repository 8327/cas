import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { Router } from "@angular/router";
import {Messages} from "../messages";
import {ControlsService} from "./controls.service";
import {UserService} from "../user.service";
import {PublishComponent} from "../publish/publish.component";
import {Commit} from "../../domain/commit";
import {MatSnackBar} from "@angular/material";

@Component({
  selector: 'app-controls',
  templateUrl: './controls.component.html',
  styleUrls: ['./controls.component.css']
})

export class ControlsComponent implements OnInit {

  isAdmin: boolean = false;
  showCommit: boolean = false;
  publishDirty: boolean = false;
  userAhead: boolean = false;

  @ViewChild('publishModal')
  submitComp: PublishComponent;

  constructor(public messages: Messages,
              public service: ControlsService,
              private router: Router,
              private userService: UserService,
              public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.userService.getUser().then(resp => this.isAdmin = resp.admin);
    this.service.untracked();
    this.unpublished();
  }

  newService() {
    this.router.navigate(["form",-1]);
  }

  openModalCommit() {
    this.showCommit = true;
  }

  closeModalCommit() {
    this.showCommit = false;
  }

  commit(msg: String) {
    if(msg === 'CANCEL') {
      this.closeModalCommit();
    } else if (!this.isAdmin) {
      this.submit(msg);
    } else {
      if (msg != null && msg != "") {
        this.service.commit(msg)
          .then(resp => this.handleCommit(resp))
          .catch(e => this.handleNotCommitted(e));
      }
    }
  }

  handleCommit(resp: String) {
    this.closeModalCommit();
    this.publishDirty = true;
    this.userAhead = true;
    this.service.untracked().then();
    this.snackBar.open(this.messages.management_services_status_committed,'dismiss',{
        duration: 5000
    });
  }

  handleNotCommitted(e: any) {
    this.snackBar.open(this.messages.management_services_status_notcommitted,'dismiss',{
        duration: 5000
    });
  }

  openModalPublish() {

  }

  publish(commits: Commit[]) {
    if (commits.length > 0 ) {
      this.service.publish()
        .then(resp => this.handlePublish())
        .catch(e => this.handleNotPublished(e));
    }
  }

  handlePublish() {
    this.publishDirty = false;
    this.snackBar.open(this.messages.management_services_status_published,'dismiss',{
        duration: 5000
    });
  }

  handleNotPublished(e: any) {
    this.snackBar.open(this.messages.management_services_status_notpublished,'dismiss',{
        duration: 5000
    });
  }

  callSubmit() {
    this.openModalCommit();
  }

  submit(msg: String) {
    this.service.submit(msg)
      .then(resp => this.handleSubmit())
      .catch(e => this.handleNotSubmitted(e));
  }

  handleSubmit() {
    this.closeModalCommit();
    this.publishDirty = true;
    this.userAhead = true;
    this.service.untracked().then();
    this.snackBar.open('Your commit has been submitted for review', 'dismiss',{
        duration: 5000
    });
  }

  handleNotSubmitted(e: any) {
    this.snackBar.open('Something went wrong and your commit was not able to be submitted', 'dismiss',{
        duration: 5000
    });
  }

  unpublished () {
    this.service.unpublished()
      .then(behind => this.publishDirty = behind > 0);
  }

}
