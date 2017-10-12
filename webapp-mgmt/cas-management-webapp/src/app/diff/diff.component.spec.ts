import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Location, LocationStrategy } from "@angular/common";
import { DiffComponent } from './diff.component';
import {Container} from "../container";
import {Editor} from "../editor.component";
import {Messages} from "../messages";
import {DiffEntry} from "../../domain/diff-entry";
import {ChangesService} from "../changes/changes.service";
import {AbstractRegisteredService, RegexRegisteredService} from "../../domain/registered-service";

let changesServiceStub = {
  getChanges(branch: String): Promise<DiffEntry[]> {
    return Promise.resolve([]);
  },

  getDiff(diff: DiffEntry): Promise<String> {
    return Promise.resolve("");
  },

  getChange(change: String): Promise<AbstractRegisteredService> {
    return Promise.resolve(new RegexRegisteredService())
  }
};

let locationStub = {

};

describe('DiffComponent', () => {
  let component: DiffComponent;
  let fixture: ComponentFixture<DiffComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DiffComponent, Container, Editor],
      providers: [
        Messages,
        {provide: ChangesService, useValue: changesServiceStub},
        {provide: Location, useValue: locationStub},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiffComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
