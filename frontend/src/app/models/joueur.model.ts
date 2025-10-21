export class Joueur {
  constructor(public id: string, public nom: string, public scoreCourant: number = 0) {}

  getNomAffichage(): string {
    return this.nom;
  }
}