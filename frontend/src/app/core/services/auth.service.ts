import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, ResetPasswordRequest, UpdateProfileRequest, UserProfileResponse } from '../models/auth.model';
import { API_ENDPOINTS, TOKEN_STORAGE_KEY } from '../constants/constants';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);

    // ### Fields ###
    readonly currentUser = signal<AuthResponse | null>(null);

    constructor() {
        this.restoreSession();
    }

    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(API_ENDPOINTS.auth.login, request)
            .pipe(tap(response => this.setSession(response)));
    }

    register(request: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(API_ENDPOINTS.auth.register, request)
            .pipe(tap(response => this.setSession(response)));
    }

    forgotPassword(email: string): Observable<void> {
        return this.http.post<void>(API_ENDPOINTS.auth.forgotPassword, { email });
    }

    resetPassword(request: ResetPasswordRequest): Observable<void> {
        return this.http.post<void>(API_ENDPOINTS.auth.resetPassword, request);
    }

    updateProfile(request: UpdateProfileRequest): Observable<UserProfileResponse> {
        return this.http.put<UserProfileResponse>(API_ENDPOINTS.users.updateProfile, request);
    }

    changePassword(request: ChangePasswordRequest): Observable<void> {
        return this.http.put<void>(API_ENDPOINTS.users.changePassword, request);
    }

    logout(): void {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        this.currentUser.set(null);
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_STORAGE_KEY);
    }

    getMyProfile(): Observable<UserProfileResponse> {
        return this.http.get<UserProfileResponse>(API_ENDPOINTS.users.me);
    }

    isLoggedIn(): boolean {
        return !!this.getToken();
    }

    private setSession(response: AuthResponse): void {
        localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
        this.currentUser.set(response);
    }

    private restoreSession(): void {
        const token = this.getToken();
        if (!token) return;

        const payload = this.decodeToken(token);

        if (!payload || this.isExpired(payload)) {
            this.logout();
            return;
        }

        this.currentUser.set({
            id: 0, // JWT doesn't bring id — irelavant in  UI, only email/role are used
            email: payload.sub,
            role: payload.role,
            token
        });
    }

    private decodeToken(token: string): any {
        try {
            const payloadPart = token.split('.')[1];
            return JSON.parse(atob(payloadPart));
        } catch {
            return null;
        }
    }

    private isExpired(payload: any): boolean {
        if (!payload.exp) return false;
        return Date.now() >= payload.exp * 1000;
    }
}