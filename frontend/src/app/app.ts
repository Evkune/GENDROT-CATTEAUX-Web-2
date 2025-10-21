import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GameService } from './game';
import { Partie } from './models/partie.model';
import { TypeCase } from './models/type-case.enum';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App implements OnInit {
  partie: Partie | undefined;
  typeCase = TypeCase;

  constructor(private gameService: GameService) {}

  ngOnInit() {
    this.partie = this.gameService.getPartieCourante();
  }

  getImageUrl(caseType: TypeCase): string {
    switch (caseType) {
      case TypeCase.PLAINE:
        return 'plain.svg';
      case TypeCase.EAU:
        return 'water.svg';
      case TypeCase.ARBRE:
        return 'tree.svg';
      default:
        return 'plain.svg';
    }
  }
}