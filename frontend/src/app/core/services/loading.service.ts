import { Injectable, signal } from '@angular/core';
import { HttpContext, HttpContextToken } from '@angular/common/http';
import { Subscription, timer } from 'rxjs';

export const SKIP_LOADING = new HttpContextToken<boolean>(() => false);

export function skipLoadingContext(): HttpContext {
    return new HttpContext().set(SKIP_LOADING, true);
}

const SHOW_DELAY_MS = 100;
const MIN_VISIBLE_MS = 100;

@Injectable({ providedIn: 'root' })
export class LoadingService {
    readonly isLoading = signal(false);

    private activeRequests = 0;
    private showTimer?: Subscription;
    private hideTimer?: Subscription;
    private shownAt = 0;

    show(): void {
        this.activeRequests++;
        if (this.activeRequests > 1) return;

        this.hideTimer?.unsubscribe();
        this.showTimer = timer(SHOW_DELAY_MS).subscribe(() => {
            this.shownAt = Date.now();
            this.isLoading.set(true);
        });
    }

    hide(): void {
        this.activeRequests--;
        if (this.activeRequests > 0) return;

        if (!this.isLoading()) {
            this.showTimer?.unsubscribe();
            return;
        }

        const elapsedVisible = Date.now() - this.shownAt;
        const remaining = Math.max(0, MIN_VISIBLE_MS - elapsedVisible);

        this.hideTimer = timer(remaining).subscribe(() => this.isLoading.set(false));
    }
}