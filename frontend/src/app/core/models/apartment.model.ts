export interface Apartment {
    id: number;
    name: string;
    description: string;
    street: string;
    apartmentNumber: string | null;
    district: string;
    city: string;
    pricePerNight: number;
    maxGuests: number;
    area: number;
    floor: number;
    buildingEntranceCode: string | null;
    keyBoxCode: string | null;
}

export interface ApartmentPhoto {
    id: number;
    fileName: string;
    displayOrder: number;
    altText: string | null;
}

export interface UpdateApartmentRequest {
    name: string;
    description: string;
    street: string;
    apartmentNumber: string | null;
    district: string;
    city: string;
    pricePerNight: number;
    maxGuests: number;
    area: number;
    floor: number;
    buildingEntranceCode: string | null;
    keyBoxCode: string | null;
}

export interface UpdatePhotoRequest {
    altText: string | null;
}