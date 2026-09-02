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

export interface ForgotPasswordRequest {
    email: string;
}

export interface ResetPasswordRequest {
    token: string;
    newPassword: string;
    confirmPassword: string;
}

export interface UserProfileResponse {
    fullName: string;
    email: string;
    phone: string;
}

export interface UpdateProfileRequest {
    fullName: string;
    phone: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}