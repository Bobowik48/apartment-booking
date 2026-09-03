import { Component, input, output, signal, computed, effect, HostListener, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-lightbox',
  standalone: true,
  imports: [],
  templateUrl: './lightbox.html',
  styleUrl: './lightbox.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Lightbox {
  readonly imageUrls = input.required<string[]>();
  readonly startIndex = input.required<number>();
  readonly closed = output<void>();

  readonly currentIndex = signal(0);

  constructor() {
    effect(() => {
      this.currentIndex.set(this.startIndex());
    });
  }

  readonly currentUrl = computed(() => this.imageUrls()[this.currentIndex()]);

  next(): void {
    const total = this.imageUrls().length;
    this.currentIndex.update(i => (i + 1) % total);
  }

  previous(): void {
    const total = this.imageUrls().length;
    this.currentIndex.update(i => (i - 1 + total) % total);
  }

  close(): void {
    this.closed.emit();
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'ArrowRight') this.next();
    else if (event.key === 'ArrowLeft') this.previous();
    else if (event.key === 'Escape') this.close();
  }
}