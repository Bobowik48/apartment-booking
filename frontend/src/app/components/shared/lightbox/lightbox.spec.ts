import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Lightbox } from './lightbox';

describe('Lightbox', () => {
  let component: Lightbox;
  let fixture: ComponentFixture<Lightbox>;

  const imageUrls = ['a.jpg', 'b.jpg', 'c.jpg'];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Lightbox]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Lightbox);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('imageUrls', imageUrls);
    fixture.componentRef.setInput('startIndex', 1);
    fixture.detectChanges();
  });

  it('should create and start at the given index', () => {
    expect(component).toBeTruthy();
    expect(component.currentIndex()).toBe(1);
    expect(component.currentUrl()).toBe('b.jpg');
  });

  it('next() should wrap around to the first image', () => {
    component.currentIndex.set(imageUrls.length - 1);
    component.next();
    expect(component.currentIndex()).toBe(0);
  });

  it('previous() should wrap around to the last image', () => {
    component.currentIndex.set(0);
    component.previous();
    expect(component.currentIndex()).toBe(imageUrls.length - 1);
  });

  it('close() should emit the closed output', () => {
    const spy = jasmine.createSpy('closed');
    component.closed.subscribe(spy);
    component.close();
    expect(spy).toHaveBeenCalled();
  });
});
