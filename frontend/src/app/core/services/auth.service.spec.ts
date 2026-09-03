import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { API_ENDPOINTS, TOKEN_STORAGE_KEY } from '../constants/constants';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

function buildJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  return `${header}.${body}.fake-signature`;
}

describe('AuthService', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  });

  function getService(): AuthService {
    const service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    return service;
  }

  it('should be created with no session when localStorage is empty', () => {
    const service = getService();
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.currentUser()).toBeNull();
  });

  it('should restore a valid, non-expired session from localStorage on construction', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600;
    const token = buildJwt({ sub: 'jan@example.com', role: 'USER', exp: futureExp });
    localStorage.setItem(TOKEN_STORAGE_KEY, token);

    const service = getService();

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.currentUser()?.email).toBe('jan@example.com');
    expect(service.currentUser()?.role).toBe('USER');
  });

  it('should log out and clear the stored token when the restored token is expired', () => {
    const pastExp = Math.floor(Date.now() / 1000) - 3600;
    const token = buildJwt({ sub: 'jan@example.com', role: 'USER', exp: pastExp });
    localStorage.setItem(TOKEN_STORAGE_KEY, token);

    const service = getService();

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('should log out when the stored token is malformed', () => {
    localStorage.setItem(TOKEN_STORAGE_KEY, 'not-a-valid-jwt');

    const service = getService();

    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('login() should store the token and update currentUser on success', () => {
    const service = getService();
    const request: LoginRequest = { email: 'jan@example.com', password: 'Secret123!' };
    const response: AuthResponse = { id: 1, email: 'jan@example.com', role: 'USER', token: 'abc.def.ghi' };

    service.login(request).subscribe(res => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne(API_ENDPOINTS.auth.login);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(response);

    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBe('abc.def.ghi');
    expect(service.currentUser()).toEqual(response);
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('register() should store the token and update currentUser on success', () => {
    const service = getService();
    const request: RegisterRequest = {
      fullName: 'Jan Kowalski',
      email: 'jan@example.com',
      phone: '+48600000000',
      password: 'Secret123!',
      captchaToken: 'token'
    };
    const response: AuthResponse = { id: 2, email: 'jan@example.com', role: 'USER', token: 'xyz.123.456' };

    service.register(request).subscribe();

    const req = httpMock.expectOne(API_ENDPOINTS.auth.register);
    expect(req.request.method).toBe('POST');
    req.flush(response);

    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBe('xyz.123.456');
    expect(service.currentUser()).toEqual(response);
  });

  it('logout() should clear the token and currentUser', () => {
    const service = getService();
    const response: AuthResponse = { id: 1, email: 'jan@example.com', role: 'USER', token: 'abc.def.ghi' };

    service.login({ email: 'jan@example.com', password: 'Secret123!' }).subscribe();
    httpMock.expectOne(API_ENDPOINTS.auth.login).flush(response);

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('forgotPassword() should POST the email to the correct endpoint', () => {
    const service = getService();

    service.forgotPassword('jan@example.com').subscribe();

    const req = httpMock.expectOne(API_ENDPOINTS.auth.forgotPassword);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'jan@example.com' });
    req.flush(null);
  });
});
