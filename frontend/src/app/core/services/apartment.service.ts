import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apartment } from '../models/apartment.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class ApartmentService {
    private http = inject(HttpClient);

    getApartment(id: number): Observable<Apartment> {
        return this.http.get<Apartment>(`${API_ENDPOINTS.apartments}/${id}`);
    }
}