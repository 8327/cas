/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { ActivatedRoute } from "@angular/router";

import { RouterTestingModule } from "@angular/router/testing";

import { ChangesComponent } from './changes.component';
import {AlertComponent} from "../alert/alert.component";
import {DiffEntry} from "../../domain/diff-entry";
import {Form} from "../../domain/form";
import {ChangesService} from "./changes.service";
import {Messages} from "../messages";
import {ActivatedRouteStub} from "../../testing/router-stub";


let changesServiceStub = {
  getChanges(branch: String): Promise<DiffEntry[]> {
    return Promise.resolve([]);
  },

  getDiff(diff: DiffEntry): Promise<String> {
    return Promise.resolve("");
  },

  getChange(change: String): Promise<Form> {
    return Promise.resolve(new Form());
  }
};

let activatedRoute: ActivatedRouteStub = new ActivatedRouteStub();

let expectedDiff: DiffEntry[] = [];

describe('ChangesComponent', () => {
  let component: ChangesComponent;
  let fixture: ComponentFixture<ChangesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [ ChangesComponent, AlertComponent ],
      providers: [
        Messages,
        {provide: ChangesService, useValue: changesServiceStub},
        {provide: ActivatedRoute, useValue: activatedRoute}
      ]
    })
    .compileComponents();
  }));

  beforeEach( async(() => {

  }));

  beforeEach(() => {
    activatedRoute.testData = expectedDiff;
    fixture = TestBed.createComponent(ChangesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
