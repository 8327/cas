/**
 * Created by tschmidt on 2/15/17.
 */
import {Injectable} from "@angular/core";
import {DiffEntry} from "../../domain/diff-entry";
import {Service} from "../service";
import {Http} from "@angular/http";
import {AbstractRegisteredService} from "../../domain/registered-service";

@Injectable()
export class ChangesService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  currentDiff: DiffEntry;

  getChanges(branch: String): Promise<DiffEntry[]> {
    return this.get<DiffEntry[]>("changes?branch=" + branch);
  }

  getDiff(diff: DiffEntry): Promise<String> {
    let url: string = "viewDiff";
    if (diff.diff === "HISTORY") {
      url = "viewHistoryDiff.html"
    }
    return this.post<String>(url, diff);
  }

  getChange(change: String): Promise<AbstractRegisteredService> {
    return this.get<AbstractRegisteredService>("viewChange?id=" + change);
  }

  getJSON(change: String): Promise<AbstractRegisteredService> {
    return this.http.get("viewChange?id=" + change)
      .toPromise()
      .then(resp => resp.json())
      .catch(this.handleError);
  }

}
