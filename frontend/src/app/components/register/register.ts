import { Component, inject, signal, computed, AfterViewInit, OnDestroy, NgZone, ViewChild, ElementRef, ChangeDetectionStrategy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { NotificationService } from '../../core/services/notification.service';
import { UI_TEXT, TURNSTILE_SITE_KEY } from '../../core/constants/constants';

declare const turnstile: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Register implements AfterViewInit, OnDestroy {
  readonly text = UI_TEXT.auth.register;
  readonly turnstileSiteKey = TURNSTILE_SITE_KEY;

  private router = inject(Router);
  private zone = inject(NgZone);

  private authService = inject(AuthService);
  private errorTranslationService = inject(ErrorTranslationService);
  private notificationService = inject(NotificationService);

  readonly fullName = signal('');
  readonly email = signal('');
  readonly phone = signal('');
  readonly password = signal('');
  readonly confirmPassword = signal('');
  readonly captchaToken = signal<string | null>(null);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  @ViewChild('turnstileContainer') turnstileContainer!: ElementRef<HTMLDivElement>;
  private turnstileWidgetId: string | null = null;
  readonly passwordsMatch = computed(() => this.password() === this.confirmPassword());
  readonly canSubmit = computed(() => {
    return this.fullName().trim().length > 0
      && this.email().trim().length > 0
      && this.phone().trim().length > 0
      && this.password().trim().length > 0
      && this.confirmPassword().trim().length > 0
      && this.passwordsMatch()
      && this.captchaToken() !== null
      && !this.isSubmitting();
  });

  private readonly renderWidget = () => {
    if (this.turnstileWidgetId !== null) return;

    this.turnstileWidgetId = turnstile.render(this.turnstileContainer.nativeElement, {
      sitekey: this.turnstileSiteKey,
      callback: (token: string) => {
        this.zone.run(() => this.captchaToken.set(token));
      },
      'expired-callback': () => {
        this.zone.run(() => this.captchaToken.set(null));
      },
      'error-callback': () => {
        this.zone.run(() => this.captchaToken.set(null));
      }
    });
  };

  ngAfterViewInit(): void {
    if (typeof turnstile !== 'undefined') {
      this.renderWidget();
    } else {
      window.addEventListener('turnstile-ready', this.renderWidget, { once: true });
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('turnstile-ready', this.renderWidget);
    if (this.turnstileWidgetId !== null && typeof turnstile !== 'undefined') {
      turnstile.remove(this.turnstileWidgetId);
    }
  }

  updateFullName(value: string): void {
    this.fullName.set(value);
  }
  updateEmail(value: string): void {
    this.email.set(value);
  }

  updatePhone(value: string): void {
    this.phone.set(value);
  }

  updatePassword(value: string): void {
    this.password.set(value);
  }

  updateConfirmPassword(value: string): void {
    this.confirmPassword.set(value);
  }

  submit(): void {
    if (!this.canSubmit()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.register({
      fullName: this.fullName(),
      email: this.email(),
      phone: this.phone(),
      password: this.password(),
      captchaToken: this.captchaToken()!
    }).subscribe({
      next: () => {
        this.notificationService.success('Konto zostało utworzone!');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));
        this.captchaToken.set(null);
        if (this.turnstileWidgetId !== null && typeof turnstile !== 'undefined') {
          turnstile.reset(this.turnstileWidgetId);
        }
      }
    });
  }
}