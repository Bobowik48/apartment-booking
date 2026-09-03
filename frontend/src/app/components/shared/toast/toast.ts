import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Toast {
  private notificationService = inject(NotificationService);

  readonly notification = this.notificationService.notification;

  dismiss(): void {
    this.notificationService.dismiss();
  }
}