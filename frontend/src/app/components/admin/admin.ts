import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ApartmentService } from '../../core/services/apartment.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { ApartmentPhoto, UpdateApartmentRequest } from '../../core/models/apartment.model';
import { DEFAULT_APARTMENT_ID, API_BASE_URL, UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Admin implements OnInit {
  // ### Constants ###
  readonly text = UI_TEXT.admin;

  // ### Services ###
  private apartmentService = inject(ApartmentService);
  private errorTranslationService = inject(ErrorTranslationService);

  // ### Fields ###
  readonly name = signal('');
  readonly description = signal('');
  readonly street = signal('');
  readonly apartmentNumber = signal('');
  readonly district = signal('');
  readonly city = signal('');
  readonly pricePerNight = signal(0);
  readonly maxGuests = signal(1);
  readonly area = signal(0);
  readonly floor = signal(0);
  readonly buildingEntranceCode = signal('');
  readonly keyBoxCode = signal('');

  readonly photos = signal<ApartmentPhoto[]>([]);
  readonly isSaving = signal(false);
  readonly isUploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.apartmentService.getApartmentForAdmin(DEFAULT_APARTMENT_ID).subscribe(apartment => {
      this.name.set(apartment.name);
      this.description.set(apartment.description);
      this.street.set(apartment.street ?? '');
      this.apartmentNumber.set(apartment.apartmentNumber ?? '');
      this.district.set(apartment.district ?? '');
      this.city.set(apartment.city ?? '');
      this.pricePerNight.set(apartment.pricePerNight);
      this.maxGuests.set(apartment.maxGuests);
      this.area.set(apartment.area ?? 0);
      this.floor.set(apartment.floor ?? 0);
      this.buildingEntranceCode.set(apartment.buildingEntranceCode ?? '');
      this.keyBoxCode.set(apartment.keyBoxCode ?? '');
    });

    this.loadPhotos();
  }

  updateName(value: string): void {
    this.name.set(value);
  }

  updateDescription(value: string): void {
    this.description.set(value);
  }

  updateStreet(value: string): void {
    this.street.set(value);
  }

  updateApartmentNumber(value: string): void {
    this.apartmentNumber.set(value);
  }

  updateDistrict(value: string): void {
    this.district.set(value);
  }

  updateCity(value: string): void {
    this.city.set(value);
  }

  updatePricePerNight(value: string): void {
    this.pricePerNight.set(Number(value));
  }

  updateMaxGuests(value: string): void {
    this.maxGuests.set(Number(value));
  }

  updateArea(value: string): void {
    this.area.set(Number(value));
  }

  updateFloor(value: string): void {
    this.floor.set(Number(value));
  }

  updateBuildingEntranceCode(value: string): void {
    this.buildingEntranceCode.set(value);
  }

  updateKeyBoxCode(value: string): void {
    this.keyBoxCode.set(value);
  }

  saveApartment(): void {
    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const request: UpdateApartmentRequest = {
      name: this.name(),
      description: this.description(),
      street: this.street(),
      apartmentNumber: this.apartmentNumber() || null,
      district: this.district(),
      city: this.city(),
      pricePerNight: this.pricePerNight(),
      maxGuests: this.maxGuests(),
      area: this.area(),
      floor: this.floor(),
      buildingEntranceCode: this.buildingEntranceCode() || null,
      keyBoxCode: this.keyBoxCode() || null
    };

    this.apartmentService.updateApartment(DEFAULT_APARTMENT_ID, request).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Zapisano zmiany.');
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.isUploading.set(true);
    this.errorMessage.set(null);

    this.apartmentService.uploadPhoto(DEFAULT_APARTMENT_ID, file).subscribe({
      next: () => {
        this.isUploading.set(false);
        this.loadPhotos();
      },
      error: (err) => {
        this.isUploading.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }

  deletePhoto(photo: ApartmentPhoto): void {
    this.apartmentService.deletePhoto(DEFAULT_APARTMENT_ID, photo.id).subscribe({
      next: () => this.loadPhotos(),
      error: (err) => {
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }

  photoUrl(photo: ApartmentPhoto): string {
    return `${API_BASE_URL}/uploads/apartments/${DEFAULT_APARTMENT_ID}/${photo.fileName}`;
  }

  private loadPhotos(): void {
    this.apartmentService.getPhotos(DEFAULT_APARTMENT_ID).subscribe(photos => this.photos.set(photos));
  }
}