/**
 * Created by tsschmi on 3/8/17.
 */
import {Injectable} from '@angular/core'
import {Note} from '../../domain/note';
import {Service} from '../service';
import {Http} from '@angular/http';

@Injectable()
export class NotesService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getNotes(id: String): Promise<String> {
    return this.get<String>('notes?id=' + id);
  }

  addNote(id: String, text: String): Promise<String> {
    return this.post<String>('addNote', new Note(id, text));
  }

}
