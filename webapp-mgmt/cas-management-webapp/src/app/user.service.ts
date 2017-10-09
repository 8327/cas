import {Injectable} from "@angular/core";
import {Service} from "./service";
import {Http} from "@angular/http";
import {UserProfile} from "../domain/user-profile";

@Injectable()
export class UserService extends Service {

    constructor(protected http: Http) {
        super(http);
    }

    getUser(): Promise<UserProfile> {
        return this.get<UserProfile>("user");
    }
}