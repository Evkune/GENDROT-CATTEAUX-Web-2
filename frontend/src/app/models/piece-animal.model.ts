import { TypeAnimal } from './type-animal.enum';
import { Joueur } from './joueur.model';
import { Case } from './case.model';
import { Carte } from './carte.model';

export abstract class PieceAnimal {
  constructor(
    public id: string,
    public type: TypeAnimal,
    public proprietaire: Joueur,
    public pointsBase: number,
    public rayon: number
  ) {}

  abstract peutEtrePlaceeSur(caseCible: Case): boolean;
  abstract calculerPoints(map: Carte, cible: Case): number;
}