import { PieceAnimal } from './piece-animal.model';
import { TypeCase } from './type-case.enum';

export class Case {
  constructor(
    public x: number,
    public y: number,
    public type: TypeCase,
    public occupeePar?: PieceAnimal
  ) {}

  estLibre(): boolean {
    return !this.occupeePar;
  }
}