import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [],
  templateUrl: './account.html',
  styleUrl: './account.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Account implements OnInit {
  readonly text = UI_TEXT.account;

  private authService = inject(AuthService);
  private errorTranslationService = inject(ErrorTranslationService);

  readonly fullName = signal('');
  readonly email = signal('');
  readonly phone = signal('');
  private originalFullName = signal('');
  private originalPhone = signal('');

  readonly isSaving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly hasChanges = computed(() => {
    const nameChanged = this.fullName().trim().length > 0 && this.fullName().trim() !== this.originalFullName().trim();
    const phoneChanged = this.phone().trim().length > 0 && this.phone().trim() !== this.originalPhone().trim();
    return nameChanged || phoneChanged;
  });

  ngOnInit(): void {
    this.authService.getMyProfile().subscribe(profile => {
      this.fullName.set(profile.fullName);
      this.email.set(profile.email);
      this.phone.set(profile.phone);
      this.originalFullName.set(profile.fullName);
      this.originalPhone.set(profile.phone);
    });
  }

  updateFullName(value: string): void {
    this.fullName.set(value);
  }

  updatePhone(value: string): void {
    this.phone.set(value);
  }

  save(): void {
    if (!this.hasChanges()) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService.updateProfile({
      fullName: this.fullName(),
      phone: this.phone()
    }).subscribe({
      next: (profile) => {
        this.isSaving.set(false);
        this.fullName.set(profile.fullName);
        this.phone.set(profile.phone);
        this.originalFullName.set(profile.fullName);
        this.originalPhone.set(profile.phone);
        this.successMessage.set(this.text.saved);
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
      }
    });
  }
}