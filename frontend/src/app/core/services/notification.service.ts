import { Injectable, signal } from '@angular/core';
import { Subscription, timer } from 'rxjs';

export type NotificationType = 'success' | 'error';

export interface AppNotification {
    message: string;
    type: NotificationType;
}

const AUTO_DISMISS_MS = 4000;

@Injectable({ providedIn: 'root' })
export class NotificationService {
    readonly notification = signal<AppNotification | null>(null);

    private dismissTimer?: Subscription;

    success(message: string): void {
        this.show(message, 'success');
    }

    error(message: string): void {
        this.show(message, 'error');
    }

    dismiss(): void {
        this.dismissTimer?.unsubscribe();
        this.notification.set(null);
    }

    private show(message: string, type: NotificationType): void {
        this.dismissTimer?.unsubscribe();
        this.notification.set({ message, type });
        this.dismissTimer = timer(AUTO_DISMISS_MS).subscribe(() => this.notification.set(null));
    }
}