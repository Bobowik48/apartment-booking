import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apartment, ApartmentPhoto, UpdateApartmentRequest, UpdatePhotoRequest } from '../models/apartment.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class ApartmentService {
    private http = inject(HttpClient);

    updateApartment(id: number, request: UpdateApartmentRequest): Observable<Apartment> {
        return this.http.put<Apartment>(`${API_ENDPOINTS.apartments}/${id}`, request);
    }

    uploadPhoto(id: number, file: File, altText?: string): Observable<ApartmentPhoto> {
        const formData = new FormData();
        formData.append('file', file);
        if (altText) formData.append('altText', altText);
        return this.http.post<ApartmentPhoto>(`${API_ENDPOINTS.apartments}/${id}/photos`, formData);
    }

    updatePhoto(apartmentId: number, photoId: number, request: UpdatePhotoRequest): Observable<ApartmentPhoto> {
        return this.http.put<ApartmentPhoto>(`${API_ENDPOINTS.apartments}/${apartmentId}/photos/${photoId}`, request);
    }

    deletePhoto(apartmentId: number, photoId: number): Observable<void> {
        return this.http.delete<void>(`${API_ENDPOINTS.apartments}/${apartmentId}/photos/${photoId}`);
    }

    getPhotos(id: number): Observable<ApartmentPhoto[]> {
        return this.http.get<ApartmentPhoto[]>(`${API_ENDPOINTS.apartments}/${id}/photos`);
    }

    getApartment(id: number): Observable<Apartment> {
        return this.http.get<Apartment>(`${API_ENDPOINTS.apartments}/${id}`);
    }
}