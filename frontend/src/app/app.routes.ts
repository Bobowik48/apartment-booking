import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin-guard';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./components/home/home').then(m => m.Home)
    },
    {
        path: 'booking',
        loadComponent: () => import('./components/booking/booking').then(m => m.Booking)
    },
    {
        path: 'login',
        loadComponent: () => import('./components/login/login').then(m => m.Login)
    },
    {
        path: 'register',
        loadComponent: () => import('./components/register/register').then(m => m.Register)
    },
    {
        path: 'forgot-password',
        loadComponent: () => import('./components/forgot-password/forgot-password').then(m => m.ForgotPassword)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./components/reset-password/reset-password').then(m => m.ResetPassword)
    },
    {
        path: 'my-reservations',
        loadComponent: () => import('./components/my-reservations/my-reservations').then(m => m.MyReservations),
        canActivate: [authGuard]
    },
    {
        path: 'reservation-details',
        loadComponent: () => import('./components/reservation-details/reservation-details').then(m => m.ReservationDetails)
    },
    {
        path: 'admin',
        loadComponent: () => import('./components/admin/admin').then(m => m.Admin),
        canActivate: [adminGuard]
    },
    { path: '**', redirectTo: '' }
];