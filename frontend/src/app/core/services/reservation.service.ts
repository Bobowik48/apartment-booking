
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateReservationRequest, ReservationResponse } from '../models/reservation.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class ReservationService {
    private http = inject(HttpClient);

    createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
        return this.http.post<ReservationResponse>(API_ENDPOINTS.reservations, request);
    }

    getByAccessToken(accessToken: string): Observable<ReservationResponse> {
        return this.http.get<ReservationResponse>(`${API_ENDPOINTS.reservations}/${accessToken}`);
    }
}