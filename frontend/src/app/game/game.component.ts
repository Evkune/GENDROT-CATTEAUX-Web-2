import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GameService } from '../game';
import { Partie } from '../models/partie.model';
import { TypeCase } from '../models/type-case.enum';
import { TypeAnimal } from '../models/type-animal.enum';
import { Case } from '../models/case.model'; // Import nécessaire pour le typage

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
  
  // Map pour stocker les scores temporaires (clé = "x,y", valeur = score)
  previewMap: Map<string, number> = new Map();

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
      next: (p) => {
        this.partie = p;
        this.effacerApercu(); // On efface l'aperçu après avoir joué
      },
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
    return Math.max(0, this.partie.scoreCible - (8 * this.partie.tourCourant));
  }

  getScoreRelatif(): number {
    if (!this.partie) return 0;
    return Math.max(0, this.partie.scoreTotal - this.getSeuilPrecedent());
  }

  getObjectifRelatif(): number {
    if (!this.partie) return 0;
    return this.partie.scoreCible - this.getSeuilPrecedent();
  }

  getScoreDashOffset(): number {
    const objectif = this.getObjectifRelatif();
    if (!this.partie || objectif === 0) return 283;
    
    const avancement = this.getScoreRelatif();
    const pourcentage = Math.min(avancement / objectif, 1);
    
    const circonference = 283; 
    return circonference - (pourcentage * circonference);
  }

  // --- LOGIQUE DE PREVISUALISATION DU SCORE ---

  getKey(x: number, y: number): string {
    return `${x},${y}`;
  }

  trouverCase(x: number, y: number): Case | undefined {
    return this.partie?.carte.cases.find(c => c.x === x && c.y === y);
  }

  effacerApercu() {
    this.previewMap.clear();
  }

  /**
   * Récupère les cases voisines dans un rayon donné (carré).
   * @param cX Coordonnée X du centre
   * @param cY Coordonnée Y du centre
   * @param radius Rayon de recherche
   */
  getCasesInRadius(cX: number, cY: number, radius: number): Case[] {
    const cases: Case[] = [];
    if (!this.partie) return cases;

    for (let x = cX - radius; x <= cX + radius; x++) {
        for (let y = cY - radius; y <= cY + radius; y++) {
            if (x === cX && y === cY) continue; // On ignore la case centrale
            
            const c = this.trouverCase(x, y);
            if (c) cases.push(c);
        }
    }
    return cases;
  }

calculerApercu(targetX: number, targetY: number) {
    this.effacerApercu();
    if (!this.partie || !this.animalSelectionne) return;

    const caseVisee = this.trouverCase(targetX, targetY);
    
    // 1. Validation du placement
    if (!caseVisee || caseVisee.occupeePar) return;
    if (this.animalSelectionne === TypeAnimal.POISSON && caseVisee.type !== TypeCase.EAU) return;
    if (this.animalSelectionne !== TypeAnimal.POISSON && caseVisee.type === TypeCase.EAU) return;

    // 2. Configuration selon l'animal
    let baseScore = 0;
    let rayon = 0;

    switch (this.animalSelectionne) {
      case TypeAnimal.OURS:
        baseScore = 6;
        rayon = 2;
        break;
      case TypeAnimal.POISSON:
        baseScore = 8;
        rayon = 1;
        break;
      case TypeAnimal.RENARD:
        baseScore = 5;
        rayon = 1;
        break;
    }

    // 3. Affichage du score de base sur la case survolée (HOVER)
    this.previewMap.set(this.getKey(targetX, targetY), baseScore);

    // 4. Calcul des bonus/malus des voisins
    for (let x = targetX - rayon; x <= targetX + rayon; x++) {
      for (let y = targetY - rayon; y <= targetY + rayon; y++) {
        
        // On ignore la case centrale (déjà gérée avec le baseScore)
        if (x === targetX && y === targetY) continue;

        const voisin = this.trouverCase(x, y);
        
        if (voisin) {
          let contribution = 0;

          switch (this.animalSelectionne) {
            case TypeAnimal.OURS:
              if (voisin.type === TypeCase.ARBRE) contribution += 4;
              if (voisin.occupeePar === TypeAnimal.POISSON) contribution += 7;
              else if (voisin.occupeePar === TypeAnimal.RENARD) contribution -= 2;
              else if (voisin.occupeePar === TypeAnimal.OURS) contribution -= 5;
              break;

            case TypeAnimal.POISSON:
              if (voisin.type === TypeCase.EAU) contribution += 5;
              if (voisin.occupeePar === TypeAnimal.POISSON) contribution -= 2;
              break;

            case TypeAnimal.RENARD:
              if (voisin.type === TypeCase.PLAINE) contribution += 7;
              if (voisin.occupeePar === TypeAnimal.RENARD) contribution -= 2;
              break;
          }

          // On n'affiche QUE si la case apporte quelque chose (positif ou négatif)
          if (contribution !== 0) {
            this.previewMap.set(this.getKey(x, y), contribution);
          }
        }
      }
    }
  }
}