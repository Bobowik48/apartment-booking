import { Component, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResetPassword {
  // ### Constants ###
  readonly text = UI_TEXT.auth.resetPassword;

  // ### Dependencies ###
  private route = inject(ActivatedRoute);

  // ### Services ###
  private authService = inject(AuthService);
  private errorTranslationService = inject(ErrorTranslationService);

  // ### Fields ###
  private readonly token = this.route.snapshot.queryParamMap.get('token');
  readonly hasToken = signal(this.token !== null);
  readonly newPassword = signal('');
  readonly confirmPassword = signal('');
  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly passwordsMatch = computed(() => this.newPassword() === this.confirmPassword());

  readonly canSubmit = computed(() => {
    return this.newPassword().trim().length > 0
      && this.confirmPassword().trim().length > 0
      && this.passwordsMatch()
      && !this.isSubmitting();
  });

  updateNewPassword(value: string): void {
    this.newPassword.set(value);
  }

  updateConfirmPassword(value: string): void {
    this.confirmPassword.set(value);
  }

  submit(): void {
    if (!this.canSubmit() || !this.token) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.resetPassword({
      token: this.token,
      newPassword: this.newPassword(),
      confirmPassword: this.confirmPassword()
    }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.isSubmitted.set(true);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }
}