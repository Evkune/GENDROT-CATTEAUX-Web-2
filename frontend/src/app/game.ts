import { Injectable } from '@angular/core';
import { Partie } from './models/partie.model';
import { Carte } from './models/carte.model';
import { Case } from './models/case.model';
import { TypeCase } from './models/type-case.enum';
import { Joueur } from './models/joueur.model';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private partieCourante: Partie;

  constructor() {
    this.partieCourante = this.creerPartieFactice();
  }

  getPartieCourante(): Partie {
    return this.partieCourante;
  }

  private creerPartieFactice(): Partie {
    const largeur = 8;
    const hauteur = 8;
    const cases: Case[][] = [];

    for (let y = 0; y < hauteur; y++) {
      const ligne: Case[] = [];
      for (let x = 0; x < largeur; x++) {
        if ((x > 2 && x < 5) && (y > 3 && y < 6)) {
          ligne.push(new Case(x, y, TypeCase.EAU));
        } else {
          ligne.push(new Case(x, y, TypeCase.PLAINE));
        }
      }
      cases.push(ligne);
    }

    const carte = new Carte('carte-factice', 'Ma Première Carte', largeur, hauteur, cases);
    const joueur = new Joueur('joueur-factice', 'Joueur 1');

    return new Partie('partie-factice', joueur, carte);
  }
}