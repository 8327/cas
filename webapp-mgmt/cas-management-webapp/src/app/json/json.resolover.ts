/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from "@angular/core";
import {Resolve, Router, ActivatedRouteSnapshot} from "@angular/router";
import {ChangesService} from "../changes/changes.service";
import {AbstractRegisteredService} from "../../domain/registered-service";

@Injectable()
export class JSONResolver implements Resolve<AbstractRegisteredService> {

  constructor(private service: ChangesService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<AbstractRegisteredService> {
    let param: string = route.params['fileId'];

    if(!param) {
      return new Promise((resolve,reject) => resolve(null));
    } else {
      return this.service.getChange(param).then(resp => resp ? resp : null);
    }
  }
}
