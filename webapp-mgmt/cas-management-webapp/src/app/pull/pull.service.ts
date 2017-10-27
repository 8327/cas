/**
 * Created by tschmidt on 2/15/17.
 */
import {Injectable} from '@angular/core';
import { Branch } from '../../domain/branch';
import {Service} from '../service';
import {Http} from '@angular/http';

@Injectable()
export class PullService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getBranches(options: boolean[]): Promise<Branch[]> {
    return this.post<Branch[]>('pullRequests', options);
  }

  accept(branch: Branch, note: String): Promise<String> {
    return this.post<String>('acceptBranch', { branch: branch, note: note});
  }

  reject(branch: Branch, note: String): Promise<String> {
    return this.post<String>('rejectBranch', { branch: branch, note: note});
  }

}
