import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvailabilityResponse } from '../models/availability.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
    private http = inject(HttpClient);

    getUnavailableDates(apartmentId: number, from: string, to: string): Observable<AvailabilityResponse> {
        const params = { apartmentId, from, to };
        return this.http.get<AvailabilityResponse>(API_ENDPOINTS.availability, { params });
    }
}