import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { NotificationService } from '../../core/services/notification.service';
import { UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [],
  templateUrl: './change-password.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChangePassword {
  readonly text = UI_TEXT.auth.changePassword;

  private router = inject(Router);
  private authService = inject(AuthService);
  private errorTranslationService = inject(ErrorTranslationService);
  private notificationService = inject(NotificationService);

  readonly currentPassword = signal('');
  readonly newPassword = signal('');
  readonly confirmPassword = signal('');

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly passwordsMatch = computed(() => this.newPassword() === this.confirmPassword());

  readonly canSubmit = computed(() =>
    this.currentPassword().trim().length > 0
    && this.newPassword().trim().length > 0
    && this.confirmPassword().trim().length > 0
    && this.passwordsMatch()
    && !this.isSubmitting()
  );

  updateCurrentPassword(value: string): void {
    this.currentPassword.set(value);
  }

  updateNewPassword(value: string): void {
    this.newPassword.set(value);
  }

  updateConfirmPassword(value: string): void {
    this.confirmPassword.set(value);
  }

  submit(): void {
    if (!this.canSubmit()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.changePassword({
      currentPassword: this.currentPassword(),
      newPassword: this.newPassword(),
      confirmPassword: this.confirmPassword()
    }).subscribe({
      next: () => {
        this.notificationService.success('Hasło zostało zmienione. Zaloguj się ponownie.');
        this.authService.logout();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }
}