import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ApartmentService } from './core/services/apartment.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private apartmentService = inject(ApartmentService);

  constructor() {
    this.apartmentService.getApartment(1).subscribe({
      next: apartment => console.log('OK:', apartment),
      error: err => console.error('BŁĄD:', err)
    });
  }
}