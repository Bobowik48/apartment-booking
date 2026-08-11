import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { API_ENDPOINTS } from '../constants/constants';

const TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);

    // ### Fields ###
    readonly currentUser = signal<AuthResponse | null>(null);

    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(API_ENDPOINTS.auth.login, request)
            .pipe(tap(response => this.setSession(response)));
    }

    register(request: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(API_ENDPOINTS.auth.register, request)
            .pipe(tap(response => this.setSession(response)));
    }

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        this.currentUser.set(null);
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_KEY);
    }

    isLoggedIn(): boolean {
        return !!this.getToken();
    }

    private setSession(response: AuthResponse): void {
        localStorage.setItem(TOKEN_KEY, response.token);
        this.currentUser.set(response);
    }
}