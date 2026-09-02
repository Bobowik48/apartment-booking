import { Component, inject, signal, ChangeDetectionStrategy, HostListener, ElementRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
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

  // ### Dependencies ###
  private router = inject(Router);
  private elementRef = inject(ElementRef);

  // ### Services ###
  authService = inject(AuthService);

  // ### Fields ###
  readonly isMenuOpen = signal(false);
  readonly isAccountMenuOpen = signal(false);

  toggleMenu(): void {
    this.isMenuOpen.update(open => !open);
  }

  closeMenu(): void {
    this.isMenuOpen.set(false);
    this.isAccountMenuOpen.set(false);
  }

  toggleAccountMenu(): void {
    this.isAccountMenuOpen.update(open => !open);
  }

  logout(): void {
    this.authService.logout();
    this.closeMenu();
    this.router.navigate(['/']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isAccountMenuOpen()) return;
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isAccountMenuOpen.set(false);
    }
  }
}