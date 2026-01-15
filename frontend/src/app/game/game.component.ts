import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { GameService } from '../game';
import { Partie } from '../models/partie.model';
import { TypeCase } from '../models/type-case.enum';
import { TypeAnimal } from '../models/type-animal.enum';
import { Case } from '../models/case.model';

// 1. IMPORTS INTERACTO OBLIGATOIRES
import { InteractoModule } from 'interacto-angular';
import { PlacerPieceCommand } from '../commands/placer-piece.command';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-game',
  standalone: true,
  // 2. IMPORT DU MODULE INTERACTO ICI
  imports: [CommonModule, InteractoModule], 
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.css']
})
export class GameComponent implements OnInit {
  partie: Partie | undefined;
  typeCase = TypeCase;
  typeAnimal = TypeAnimal;
  animalSelectionne: TypeAnimal = TypeAnimal.OURS;
  previewMap: Map<string, number> = new Map();
  private partieSub: Subscription | undefined;

  constructor(
    private gameService: GameService, 
    private router: Router
    // 3. ON RETIRE undoHistory DU CONSTRUCTEUR (Géré par les directives)
  ) {}

  ngOnInit() {
    if (!this.gameService.getPartieCourante()) {
      this.router.navigate(['/']);
      return;
    }
    this.partie = this.gameService.getPartieCourante();
    this.partieSub = this.gameService.partieSubject.subscribe(p => {
      if (p) this.partie = p;
    });
  }

  ngOnDestroy() {
    if (this.partieSub) this.partieSub.unsubscribe();
  }

  selectionnerAnimal(animal: TypeAnimal) {
    this.animalSelectionne = animal;
  }

  // 4. NOUVELLE MÉTHODE (BINDER) REMPLAÇANT onCaseClick
  // C'est celle demandée par le prof pour configurer le clic
  // CORRECTION : Utilisez 'any' pour le binder
  public configurerClic(binder: any, x: number, y: number): void {
    binder
      .toProduce(() => new PlacerPieceCommand(this.gameService, this.animalSelectionne, x, y))
      // AJOUT : La commande ne se déclenche QUE si on a des pièces > 0
      .when(() => (this.partie?.piecesDisponibles[this.animalSelectionne] || 0) > 0)
      .bind();
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

  getKey(x: number, y: number): string {
    return `${x},${y}`;
  }

  trouverCase(x: number, y: number): Case | undefined {
    return this.partie?.carte.cases.find(c => c.x === x && c.y === y);
  }

  // Cette fonction permet à Angular d'identifier chaque case de manière unique
  trackByCase(c: any): string {
    return `${c.x}-${c.y}`;
  }

  effacerApercu() {
    this.previewMap.clear();
  }

  calculerApercu(targetX: number, targetY: number) {
    this.effacerApercu();
    if (!this.partie || !this.animalSelectionne) return;

    const nbRestant = this.partie.piecesDisponibles[this.animalSelectionne] || 0;
    if (nbRestant <= 0) {
        return;
    }

    const caseVisee = this.trouverCase(targetX, targetY);
    if (!caseVisee || caseVisee.occupeePar) return;
    if (this.animalSelectionne === TypeAnimal.POISSON && caseVisee.type !== TypeCase.EAU) return;
    if (this.animalSelectionne !== TypeAnimal.POISSON && caseVisee.type === TypeCase.EAU) return;

    let baseScore = 0;
    let rayon = 0;

    switch (this.animalSelectionne) {
      case TypeAnimal.OURS: baseScore = 6; rayon = 2; break;
      case TypeAnimal.POISSON: baseScore = 8; rayon = 1; break;
      case TypeAnimal.RENARD: baseScore = 5; rayon = 1; break;
    }

    this.previewMap.set(this.getKey(targetX, targetY), baseScore);

    for (let x = targetX - rayon; x <= targetX + rayon; x++) {
      for (let y = targetY - rayon; y <= targetY + rayon; y++) {
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
          if (contribution !== 0) {
            this.previewMap.set(this.getKey(x, y), contribution);
          }
        }
      }
    }
  }
}