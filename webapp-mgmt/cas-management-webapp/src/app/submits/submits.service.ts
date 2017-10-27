/**
 * Created by tschmidt on 2/15/17.
 */
import {Injectable} from '@angular/core';
import { Branch } from '../../domain/branch';
import {Service} from '../service';
import {Http} from '@angular/http';

@Injectable()
export class SubmitService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getSubmits(): Promise<Branch[]> {
    return this.get<Branch[]>('submitRequests');
  }

  revert(name: string): Promise<String> {
    return this.get<String>('revertSubmit?branchName=' + name);
  }

}
