import { Case } from './case.model';

export class Carte {
  constructor(
    public id: string,
    public nom: string,
    public largeur: number,
    public hauteur: number,
    public cases: Case[]
  ) {}
}