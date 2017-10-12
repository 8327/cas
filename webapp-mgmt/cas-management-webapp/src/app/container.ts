/**
 * Created by tschmidt on 7/13/16.
 */
import {Component, AfterViewInit, ViewChild, ElementRef, HostListener} from '@angular/core';

@Component({
    selector: 'agg-container',
    template: `
       <div #container [style.height.px]="height">
          <ng-content></ng-content>
       </div>
    `
})

export class Container implements AfterViewInit {

  height:Number;
  @ViewChild('container')
  container:ElementRef;

  ngAfterViewInit() {
      this.resize();
  }

  @HostListener('window:resize') resize() {
      this.height = (window.innerHeight - this.container.nativeElement.offsetTop -180);
  }
}
