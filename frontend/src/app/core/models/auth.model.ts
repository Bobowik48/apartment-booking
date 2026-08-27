export type UserRole = 'USER' | 'ADMIN';

export interface RegisterRequest {
    fullName: string;
    email: string;
    phone: string;
    password: string;
    captchaToken: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    id: number;
    email: string;
    role: UserRole;
    token: string;
}