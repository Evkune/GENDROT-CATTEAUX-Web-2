import { Carte } from './carte.model';
import { Joueur } from './joueur.model';

export class Partie {
  constructor(
    public id: string,
    public joueur: Joueur,
    public carte: Carte,
    public scoreTotal: number = 0,
    public termine: boolean = false,
    public tourCourant: number = 1,
    public scoreCible: number = 0,
    public piecesDisponibles: { [key: string]: number } = {} 
  ) {}
}