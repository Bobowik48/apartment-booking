import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './components/shared/navbar/navbar';
import { Footer } from './components/shared/footer/footer';
import { Spinner } from './components/shared/spinner/spinner';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, Footer, Spinner],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App { }