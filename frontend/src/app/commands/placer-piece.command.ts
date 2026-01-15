import { UndoableCommand } from 'interacto';
import { GameService } from '../game';
import { TypeAnimal } from '../models/type-animal.enum';

export class PlacerPieceCommand extends UndoableCommand {
  
  constructor(
    private gameService: GameService,
    private typeAnimal: TypeAnimal,
    private x: number,
    private y: number
  ) {
    super();
  }

  // Appelé automatiquement par Interacto
  protected execution(): void {
    // On subscribe pour lancer l'appel HTTP
    this.gameService.placerPiece(this.typeAnimal, this.x, this.y).subscribe();
  }

  public undo(): void {
    this.gameService.undo().subscribe();
  }

  public redo(): void {
    this.gameService.redo().subscribe();
  }

  // Interacto a besoin de ça. On retourne un objet vide ou null car
  // notre état est géré côté serveur, mais la méthode doit exister.
  public getMemento(): any {
    return {}; 
  }
}