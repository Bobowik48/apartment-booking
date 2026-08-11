export type ReservationStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface CreateReservationRequest {
    apartmentId: number;
    checkInDate: string;
    checkOutDate: string;
    guestsCount?: number;
    guestName: string;
    guestEmail: string;
    guestPhone: string;
    userId?: number;
}

export interface ReservationResponse {
    id: number;
    checkInDate: string;
    checkOutDate: string;
    guestsCount: number | null;
    totalPrice: number;
    status: ReservationStatus;
    accessToken: string;
}