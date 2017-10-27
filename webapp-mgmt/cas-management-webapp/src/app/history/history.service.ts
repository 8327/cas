/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from '@angular/core';
import {History} from '../../domain/history';
import {Service} from '../service';
import {Http} from '@angular/http';

@Injectable()
export class HistoryService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  history(fileName: string): Promise<History[]> {
    return this.get<History[]>('history?path=' + fileName);
  }

  checkout(id: string, path: String): Promise<String> {
    return this.get<String>('checkout?id=' + id + '&path=' + path);
  }

}
