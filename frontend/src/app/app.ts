import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  size = 8;
  rows = Array.from({ length: this.size }, (_, i) => i);
  cols = Array.from({ length: this.size }, (_, i) => i);
}