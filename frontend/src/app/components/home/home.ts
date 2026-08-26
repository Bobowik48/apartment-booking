import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApartmentService } from '../../core/services/apartment.service';
import { Apartment, ApartmentPhoto } from '../../core/models/apartment.model';
import { DEFAULT_APARTMENT_ID, API_BASE_URL, UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
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

  readonly heroImageUrl = computed(() => {
    const firstPhoto = this.photos()[0];
    if (!firstPhoto) return null;
    return `${API_BASE_URL}/uploads/apartments/${DEFAULT_APARTMENT_ID}/${firstPhoto.fileName}`;
  });

  readonly heroAddress = computed(() => {
    const apt = this.apartment();
    if (!apt) return '';
    const numberPart = apt.apartmentNumber ? `, ${apt.apartmentNumber}` : '';
    return `ul. ${apt.street}${numberPart}\n${apt.district}, ${apt.city}`;
  });

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
}