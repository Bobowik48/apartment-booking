import { TestBed } from '@angular/core/testing';
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { of, Observable } from 'rxjs';

import { jwtInterceptor } from './jwt-interceptor';
import { AuthService } from '../services/auth.service';

describe('jwtInterceptor', () => {
  const runInterceptor = (req: HttpRequest<unknown>, next: HttpHandlerFn) =>
    TestBed.runInInjectionContext(() => jwtInterceptor(req, next));

  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken']);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authServiceSpy }]
    });
  });

  it('should add an Authorization header when a token is present', (done) => {
    authServiceSpy.getToken.and.returnValue('my-jwt-token');
    const req = new HttpRequest('GET', '/api/apartments/1');

    const next: HttpHandlerFn = (modifiedReq): Observable<HttpEvent<unknown>> => {
      expect(modifiedReq.headers.get('Authorization')).toBe('Bearer my-jwt-token');
      done();
      return of();
    };

    runInterceptor(req, next);
  });

  it('should not add an Authorization header when no token is present', (done) => {
    authServiceSpy.getToken.and.returnValue(null);
    const req = new HttpRequest('GET', '/api/apartments/1');

    const next: HttpHandlerFn = (modifiedReq): Observable<HttpEvent<unknown>> => {
      expect(modifiedReq.headers.has('Authorization')).toBeFalse();
      done();
      return of();
    };

    runInterceptor(req, next);
  });
});
