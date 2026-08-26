import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Register {
  // ### Constants ###
  readonly text = UI_TEXT.auth.register;

  // ### Dependencies ###
  private router = inject(Router);

  // ### Services ###
  private authService = inject(AuthService);
  private errorTranslationService = inject(ErrorTranslationService);

  // ### Fields ###
  readonly email = signal('');
  readonly password = signal('');
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly canSubmit = computed(() => {
    return this.email().trim().length > 0
      && this.password().trim().length > 0
      && !this.isSubmitting();
  });

  updateEmail(value: string): void {
    this.email.set(value);
  }

  updatePassword(value: string): void {
    this.password.set(value);
  }

  submit(): void {
    if (!this.canSubmit()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.register({
      email: this.email(),
      password: this.password()
    }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }
}