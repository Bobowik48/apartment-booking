import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [],
  templateUrl: './spinner.html',
  styleUrl: './spinner.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Spinner {
  private loadingService = inject(LoadingService);
  readonly isLoading = this.loadingService.isLoading;
}