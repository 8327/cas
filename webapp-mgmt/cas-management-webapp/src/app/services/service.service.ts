/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from '@angular/core';
import {ServiceItem} from '../../domain/service-item';
import {Service} from '../service';
import {Http} from '@angular/http';

@Injectable()
export class ServiceViewService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getServices(domain: String): Promise<ServiceItem[]> {
    return this.get<ServiceItem[]>('getServices?domain=' + domain);
  }

  getYaml(id: number): Promise<String> {
    return this.http.get('getYaml?id=' + id)
      .toPromise()
      .then(resp => resp.text());
  }

  getJson(id: number): Promise<String> {
    return this.http.get('getJson?id=' + id)
      .toPromise()
      .then(resp => resp.text());
  }

  delete(id: number): Promise<String> {
    return this.get<String>('deleteRegisteredService?id=' + id);
  }

  revert(fileName: string): Promise<String> {
    return this.get<String>('revert?path=' + fileName);
  }

  revertDelete(fileName: string): Promise<String> {
    return this.get<String>('revertDelete?path=' + fileName);
  }

  updateOrder(a: ServiceItem, b: ServiceItem): Promise<String> {
    return this.post<String>('updateOrder', [a, b]);
  }

}
