export const API_BASE_URL = 'http://localhost:8080';

export const API_ENDPOINTS = {
    apartments: `${API_BASE_URL}/api/apartments`,
    availability: `${API_BASE_URL}/api/availability`,
    reservations: `${API_BASE_URL}/api/reservations`,
    auth: {
        register: `${API_BASE_URL}/api/auth/register`,
        login: `${API_BASE_URL}/api/auth/login`,
    },
} as const;

export const DEFAULT_APARTMENT_ID = 1;