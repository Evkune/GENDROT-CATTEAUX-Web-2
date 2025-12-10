import { TypeAnimal } from './type-animal.enum';
import { TypeCase } from './type-case.enum';

export class Case {
  constructor(
    public x: number,
    public y: number,
    public type: TypeCase,
    public occupeePar?: TypeAnimal
  ) {}

  estLibre(): boolean {
    return !this.occupeePar;
  }
}