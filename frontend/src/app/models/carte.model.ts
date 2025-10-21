import { Case } from './case.model';

export class Carte {
  constructor(
    public id: string,
    public nom: string,
    public largeur: number,
    public hauteur: number,
    public cases: Case[][]
  ) {}

  getCase(x: number, y: number): Case | undefined {
    if (x >= 0 && x < this.largeur && y >= 0 && y < this.hauteur) {
      return this.cases[y][x];
    }
    return undefined;
  }
}