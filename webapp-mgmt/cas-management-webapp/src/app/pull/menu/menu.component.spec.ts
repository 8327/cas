import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PullMenuComponent } from './menu.component';

describe('MenuComponent', () => {
  let component: PullMenuComponent;
  let fixture: ComponentFixture<PullMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PullMenuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PullMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
