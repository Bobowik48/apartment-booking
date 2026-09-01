import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentInitResponse } from '../models/payment.model';
import { API_ENDPOINTS } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private http = inject(HttpClient);

    initPayment(reservationId: number): Observable<PaymentInitResponse> {
        return this.http.post<PaymentInitResponse>(`${API_ENDPOINTS.payments}/${reservationId}`, {});
    }
}