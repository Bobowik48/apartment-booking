import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AvailabilityResponse } from '../models/availability.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
    private http = inject(HttpClient);

    readonly unavailableDates = signal<Set<string>>(new Set());
    private loadedRange: { from: string; to: string } | null = null;

    loadAvailability(apartmentId: number, from: string, to: string): Observable<AvailabilityResponse> {
        return this.http.get<AvailabilityResponse>(API_ENDPOINTS.availability, { params: { apartmentId, from, to } })
            .pipe(tap(response => {
                this.unavailableDates.set(new Set(response.unavailableDates));
                this.loadedRange = { from, to };
            }));
    }

    isDateUnavailable(date: string): boolean {
        return this.unavailableDates().has(date);
    }
}