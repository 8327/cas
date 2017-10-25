import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from "@angular/router/testing";

import { SubmitsComponent } from './submits.component';
import {NotesComponent} from "../notes/notes.component";
import {Editor} from "../editor.component";
import {Messages} from "../messages";
import { Branch } from "../../domain/branch";
import {SubmitService} from "./submits.service";
import {NotesService} from "../notes/notes.service";

let submitServiceStub = {
  getSubmits(): Promise<Branch[]> {
    return Promise.resolve([]);
  }
};

let notesServiceStub = {
  getNotes(id: String): Promise<String> {
    return Promise.resolve("");
  },

  addNote(id: String, text: String): Promise<String> {
    return Promise.resolve("");
  }
};

describe('SubmitsComponent', () => {
  let component: SubmitsComponent;
  let fixture: ComponentFixture<SubmitsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ RouterTestingModule ],
      declarations: [ SubmitsComponent, NotesComponent, Editor ],
      providers: [
        Messages,
        {provide: SubmitService, useValue: submitServiceStub},
        {provide: NotesService, useValue: notesServiceStub}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubmitsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
