import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoryMenuComponent } from './menu.component';

describe('HistoryMenuComponent', () => {
  let component: HistoryMenuComponent;
  let fixture: ComponentFixture<HistoryMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HistoryMenuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HistoryMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
