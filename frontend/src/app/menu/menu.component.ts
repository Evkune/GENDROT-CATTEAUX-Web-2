import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService } from '../game';
import { Carte } from '../models/carte.model';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {
  pseudo: string = 'Joueur 1';
  cartes: Carte[] = [];
  chargement = false;

  constructor(private gameService: GameService, private router: Router) {}

  ngOnInit() {
    this.chargerCartes();
  }

  chargerCartes() {
    this.gameService.recupererCartes().subscribe(cartes => this.cartes = cartes);
  }

  genererCarte() {
    this.chargement = true;
    this.gameService.genererCarteAleatoire().subscribe(() => {
      this.chargerCartes();
      this.chargement = false;
    });
  }

  lancerPartie(carte: Carte) {
    if (!this.pseudo.trim()) {
      alert('Veuillez entrer un pseudo');
      return;
    }
    this.chargement = true;
    this.gameService.demarrerPartie(carte.id, this.pseudo).subscribe({
      next: () => this.router.navigate(['/game']),
      error: (err) => {
        alert('Erreur au démarrage : ' + err.message);
        this.chargement = false;
      }
    });
  }
}