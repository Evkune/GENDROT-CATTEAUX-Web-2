import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GameService } from '../game';
import { Partie } from '../models/partie.model';
import { TypeCase } from '../models/type-case.enum';
import { TypeAnimal } from '../models/type-animal.enum';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit {
  partie: Partie | undefined;
  typeCase = TypeCase;
  typeAnimal = TypeAnimal;
  animalSelectionne: TypeAnimal = TypeAnimal.OURS;

  constructor(private gameService: GameService, private router: Router) {}

  ngOnInit() {
    this.partie = this.gameService.getPartieCourante();
    if (!this.partie) {
      this.router.navigate(['/']);
    }
  }

  selectionnerAnimal(animal: TypeAnimal) {
    this.animalSelectionne = animal;
  }

  onCaseClick(x: number, y: number) {
    if (!this.partie) return;
    this.gameService.placerPiece(this.animalSelectionne, x, y).subscribe({
      next: (p) => this.partie = p,
      error: (err) => alert(`Placement impossible : ${err.error?.message || err.message}`)
    });
  }

  getImageUrl(caseType: TypeCase): string {
    switch (caseType) {
      case TypeCase.PLAINE: return 'plain.svg';
      case TypeCase.EAU: return 'water.svg';
      case TypeCase.ARBRE: return 'tree.svg';
      default: return 'plain.svg';
    }
  }
  
  getAnimalImage(animal: TypeAnimal | undefined): string {
    if (!animal) return '';
    switch (animal) {
      case TypeAnimal.OURS: return 'bear.svg';
      case TypeAnimal.RENARD: return 'fox.svg';
      case TypeAnimal.POISSON: return 'fish.svg';
      default: return '';
    }
  }
  
  quitterPartie() {
    this.router.navigate(['/']);
  }

  getSeuilPrecedent(): number {
    if (!this.partie) return 0;
    // Formule inverse du Backend : SeuilActuel - (8 * TourActuel)
    return Math.max(0, this.partie.scoreCible - (8 * this.partie.tourCourant));
  }

  // Score acquis DURANT ce tour (0 au début du tour)
  getScoreRelatif(): number {
    if (!this.partie) return 0;
    return Math.max(0, this.partie.scoreTotal - this.getSeuilPrecedent());
  }

  // Nombre de points à faire TOTAL pour ce tour
  getObjectifRelatif(): number {
    if (!this.partie) return 0;
    return this.partie.scoreCible - this.getSeuilPrecedent();
  }

  getScoreDashOffset(): number {
    const objectif = this.getObjectifRelatif(); // L'écart à combler (ex: 25 points)
    if (!this.partie || objectif === 0) return 283;
    
    const avancement = this.getScoreRelatif(); // Ce qu'on a fait dans ce tour (ex: 10 points)
    const pourcentage = Math.min(avancement / objectif, 1);
    
    const circonference = 283; 
    return circonference - (pourcentage * circonference);
  }

  calculerApercu(x: number, y: number) {
    this.effacerApercu(); // On nettoie les anciens affichages
    
    // Vérifs de base (si pas de partie ou pas d'animal sélectionné, on arrête)
    if (!this.partie || !this.animalSelectionne) return;

    // Si la case visée est déjà occupée, on ne peut rien poser, donc pas de preview
    const caseVisee = this.trouverCase(x, y);
    if (!caseVisee || caseVisee.occupeePar) return;

    // --- LOGIQUE OURS (Exemple : Range 1) ---
    if (this.animalSelectionne === this.typeAnimal.OURS) {
        // On récupère les voisins directs (Range 1)
        const voisins = this.getCasesInRadius(x, y, 1);
        
        voisins.forEach(c => {
            // RÈGLE : Si le voisin est un Ours, ça rapporte des points (ex: +5)
            if (c.occupeePar === this.typeAnimal.OURS) {
                // On affiche "+5" directement SUR LA CASE DU VOISIN
                this.previewMap.set(this.getKey(c.x, c.y), 5);
            }
        });
    }

    // --- LOGIQUE RENARD (Exemple : Range 2) ---
    else if (this.animalSelectionne === this.typeAnimal.RENARD) {
        // Le renard voit plus loin (Range 2)
        const voisins = this.getCasesInRadius(x, y, 2);

        voisins.forEach(c => {
            // RÈGLE : +3 par Lapin (si vous avez des lapins), ou -2 si un autre Renard...
            // Adaptez ici selon vos vraies règles Java
            if (c.occupeePar === this.typeAnimal.OURS) {
                this.previewMap.set(this.getKey(c.x, c.y), -2); // Exemple de malus
            }
        });
    }
    
    // Ajoutez le POISSON ici...
  }

  // --- NOUVELLE MÉTHODE UTILITAIRE ---
  // Récupère toutes les cases dans un rayon donné (carré ou diamant selon votre logique)
  // Ici : Logique "Carré" (Chebyshev distance) qui est souvent utilisée en grille 2D simple
  getCasesInRadius(cX: number, cY: number, radius: number): any[] {
    const cases = [];
    for (let x = cX - radius; x <= cX + radius; x++) {
        for (let y = cY - radius; y <= cY + radius; y++) {
            if (x === cX && y === cY) continue;
            
            const c = this.trouverCase(x, y);
            if (c) cases.push(c);
        }
    }
    return cases;
  }
}