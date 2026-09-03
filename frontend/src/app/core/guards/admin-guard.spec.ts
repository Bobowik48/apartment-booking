import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';

import { adminGuard } from './admin-guard';
import { AuthService } from '../services/auth.service';
import { AuthResponse } from '../models/auth.model';
import { signal } from '@angular/core';

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
      TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  let routerSpy: jasmine.SpyObj<Router>;

  function configureWithUser(user: AuthResponse | null) {
    const authServiceStub = { currentUser: signal(user) };
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceStub },
        { provide: Router, useValue: routerSpy }
      ]
    });
  }

  it('should allow navigation when the current user has the ADMIN role', () => {
    configureWithUser({ id: 1, email: 'admin@example.com', role: 'ADMIN', token: 't' });

    const result = executeGuard({} as any, {} as any);

    expect(result).toBeTrue();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should redirect to / and block navigation when the current user is a regular USER', () => {
    configureWithUser({ id: 1, email: 'jan@example.com', role: 'USER', token: 't' });

    const result = executeGuard({} as any, {} as any);

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to / when there is no logged-in user', () => {
    configureWithUser(null);

    const result = executeGuard({} as any, {} as any);

    expect(result).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });
});
