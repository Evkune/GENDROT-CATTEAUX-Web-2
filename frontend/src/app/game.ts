import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { Partie } from './models/partie.model';
import { Carte } from './models/carte.model';
import { TypeAnimal } from './models/type-animal.enum';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private apiUrl = '/api';
  
  private partieCourante: Partie | undefined;
  
  // LE SIGNAL : Notifie les composants quand la partie change
  public partieSubject = new BehaviorSubject<Partie | undefined>(undefined);

  constructor(private http: HttpClient) {}

  getPartieCourante(): Partie | undefined {
    return this.partieCourante;
  }

  recupererCartes(): Observable<Carte[]> {
    return this.http.get<Carte[]>(`${this.apiUrl}/cartes`);
  }

  genererCarteAleatoire(): Observable<Carte> {
    return this.http.post<Carte>(`${this.apiUrl}/cartes/aleatoire`, {});
  }

  demarrerPartie(carteId: string, nomJoueur: string): Observable<Partie> {
    return this.http.post<Partie>(`${this.apiUrl}/parties`, { carteId, nomJoueur }).pipe(
      tap(partie => {
        this.partieCourante = partie;
        // IMPORTANT : On prévient l'écran que la partie a changé
        this.partieSubject.next(partie);
      })
    );
  }

  placerPiece(typeAnimal: TypeAnimal, x: number, y: number): Observable<Partie> {
    if (!this.partieCourante) throw new Error('Aucune partie en cours');
    const url = `${this.apiUrl}/parties/${this.partieCourante.id}/placer`;
    return this.http.post<Partie>(url, { typeAnimal, x, y }).pipe(
      tap(partie => {
        this.partieCourante = partie;
        // IMPORTANT : On prévient l'écran
        this.partieSubject.next(partie);
      })
    );
  }

  undo(): Observable<Partie> {
    if (!this.partieCourante) throw new Error("Pas de partie");
    return this.http.post<Partie>(`${this.apiUrl}/parties/${this.partieCourante.id}/undo`, {}).pipe(
      tap(partie => {
        this.partieCourante = partie;
        // IMPORTANT : On prévient l'écran
        this.partieSubject.next(partie);
      })
    );
  }

  redo(): Observable<Partie> {
    if (!this.partieCourante) throw new Error("Pas de partie");
    return this.http.post<Partie>(`${this.apiUrl}/parties/${this.partieCourante.id}/redo`, {}).pipe(
      tap(partie => {
        this.partieCourante = partie;
        // IMPORTANT : On prévient l'écran
        this.partieSubject.next(partie);
      })
    );
  }
}