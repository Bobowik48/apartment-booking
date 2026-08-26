import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UI_TEXT } from '../../../core/constants/constants';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Navbar {
  // ### Constants ###
  readonly text = UI_TEXT.navbar;

  // ### Services ###
  authService = inject(AuthService);

  // ### Auth ###
  logout(): void {
    this.authService.logout();
  }
}