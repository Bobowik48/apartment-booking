import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { convertToParamMap } from '@angular/router';

import { ReservationDetails } from './reservation-details';

describe('ReservationDetails', () => {
  let component: ReservationDetails;
  let fixture: ComponentFixture<ReservationDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationDetails],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap({ token: 'token-123' }) }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
