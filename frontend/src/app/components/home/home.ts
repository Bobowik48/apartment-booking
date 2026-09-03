import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApartmentService } from '../../core/services/apartment.service';
import { Apartment, ApartmentPhoto } from '../../core/models/apartment.model';
import { DEFAULT_APARTMENT_ID, API_BASE_URL, UI_TEXT } from '../../core/constants/constants';
import { Lightbox } from '../shared/lightbox/lightbox';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, Lightbox],
  templateUrl: './home.html',
  styleUrl: './home.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Home implements OnInit {
  // ### Constants ###
  readonly text = UI_TEXT.home;

  // ### Services ###
  private apartmentService = inject(ApartmentService);

  // ### Fields ###
  readonly apartment = signal<Apartment | null>(null);
  readonly photos = signal<ApartmentPhoto[]>([]);
  readonly isLoading = signal(true);
  readonly lightboxIndex = signal<number | null>(null);

  readonly heroImageUrl = computed(() => {
    const firstPhoto = this.photos()[0];
    if (!firstPhoto) return null;
    return this.photoUrl(firstPhoto);
  });

  readonly heroAddress = computed(() => {
    const apt = this.apartment();
    if (!apt) return '';
    const numberPart = apt.apartmentNumber ? `, ${apt.apartmentNumber}` : '';
    return `ul. ${apt.street}${numberPart}\n${apt.district}, ${apt.city}`;
  });

  readonly heroPhoto = computed(() => this.photos()[0] ?? null);

  readonly smallSlots = computed(() => {
    const photos = this.photos();
    if (photos.length === 5) return photos.slice(1, 5);
    return photos.slice(1, 4);
  });

  readonly overlayPhoto = computed(() => {
    const photos = this.photos();
    if (photos.length <= 5) return null;
    return photos[4];
  });

  readonly extraPhotosCount = computed(() => Math.max(0, this.photos().length - 4));

  readonly galleryPhotoUrls = computed(() => this.photos().map(p => this.photoUrl(p)));

  ngOnInit(): void {
    this.apartmentService.getApartment(DEFAULT_APARTMENT_ID)
      .subscribe({
        next: apartment => {
          this.apartment.set(apartment);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        }
      });

    this.apartmentService.getPhotos(DEFAULT_APARTMENT_ID)
      .subscribe(photos => this.photos.set(photos));
  }

  photoUrl(photo: ApartmentPhoto): string {
    return `${API_BASE_URL}/uploads/apartments/${DEFAULT_APARTMENT_ID}/${photo.fileName}`;
  }

  openLightbox(index: number): void {
    this.lightboxIndex.set(index);
  }

  closeLightbox(): void {
    this.lightboxIndex.set(null);
  }
}