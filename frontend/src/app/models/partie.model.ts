import { Carte } from './carte.model';
import { Joueur } from './joueur.model';

export class Partie {
  constructor(
    public id: string,
    public joueur: Joueur,
    public map: Carte,
    public scoreTotal: number = 0,
    public termine: boolean = false
  ) {}
}