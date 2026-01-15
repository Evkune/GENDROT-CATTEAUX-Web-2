package game.service;

import game.model.*;
import game.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartieServiceTest {

    @Mock private PartieRepository partieRepository;
    @Mock private CarteRepository carteRepository;
    @Mock private JoueurRepository joueurRepository;

    @InjectMocks private PartieService partieService;

    private Partie partie;
    private Carte carte;

    @BeforeEach
    void setUp() {
        // Setup d'une carte 3x3 simple
        carte = new Carte("MapTest", "Test", 3, 3);
        List<Case> cases = new ArrayList<>();
        // 0,0 = PLAINE, 0,1 = EAU, le reste = PLAINE
        for(int x=0; x<3; x++) {
            for(int y=0; y<3; y++) {
                TypeCase type = (x==0 && y==1) ? TypeCase.EAU : TypeCase.PLAINE;
                cases.add(new Case(x, y, type, carte));
            }
        }
        carte.setCases(cases);

        Joueur joueur = new Joueur("JoueurTest");
        partie = new Partie("id-partie", joueur, carte);
        partie.setScoreCible(50);
        partie.setTourCourant(1);

        // Mock générique pour findById
        lenient().when(partieRepository.findById("id-partie")).thenReturn(Optional.of(partie));
        // Mock générique pour save (renvoie l'objet modifié)
        lenient().when(partieRepository.save(any(Partie.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    // --- TEST DÉMARRAGE ---
    @Test
    void testDemarrerPartie() {
        when(carteRepository.findById("id-carte")).thenReturn(Optional.of(carte));
        when(joueurRepository.findById("Joueur1")).thenReturn(Optional.empty()); // Nouveau joueur
        when(joueurRepository.save(any(Joueur.class))).thenReturn(new Joueur("Joueur1"));

        Partie p = partieService.demarrerPartie("id-carte", "Joueur1");
        
        assertNotNull(p);
        assertEquals("Joueur1", p.getJoueur().getNom());
        assertEquals(9, p.getCarte().getCases().size()); // 3x3
    }

    // --- TEST PLACEMENT & RÈGLES ---
    @Test
    void testPlacerPieceValideEtScore() {
        // Placement Renard (5pts) sur Plaine (Bonus +7 si voisin plaine, ici on simplifie sans voisin pour l'instant)
        // Case 0,0 est Plaine.
        partie.getPiecesDisponibles().put(TypeAnimal.RENARD, 1);
        
        Partie result = partieService.placerPiece("id-partie", TypeAnimal.RENARD, 0, 0);

        assertEquals(TypeAnimal.RENARD, result.getCarte().getCases().get(0).getOccupeePar());
        assertEquals(0, result.getPiecesDisponibles().get(TypeAnimal.RENARD)); // Consommé
        assertTrue(result.getScoreTotal() >= 5); // Au moins les points de base
    }

    @Test
    void testPlacerPieceInvalideRegle() {
        // Ours sur Eau (0,1) -> Interdit
        partie.getPiecesDisponibles().put(TypeAnimal.OURS, 1);

        assertThrows(IllegalArgumentException.class, () -> 
            partieService.placerPiece("id-partie", TypeAnimal.OURS, 0, 1)
        );
    }

    @Test
    void testPlacerPieceInvalidePlusDePiece() {
        partie.getPiecesDisponibles().put(TypeAnimal.OURS, 0);
        assertThrows(IllegalArgumentException.class, () -> 
            partieService.placerPiece("id-partie", TypeAnimal.OURS, 0, 0)
        );
    }

    @Test
    void testPlacerPieceInvalideCaseOccupee() {
        partie.getPiecesDisponibles().put(TypeAnimal.RENARD, 2);
        partieService.placerPiece("id-partie", TypeAnimal.RENARD, 0, 0);
        
        // On essaie de remettre au même endroit
        assertThrows(IllegalArgumentException.class, () -> 
            partieService.placerPiece("id-partie", TypeAnimal.RENARD, 0, 0)
        );
    }

    // --- TEST UNDO / REDO ---
    @Test
    void testUndoRedoSimple() {
        partie.getPiecesDisponibles().put(TypeAnimal.POISSON, 1);
        // On place sur l'eau (0,1)
        partieService.placerPiece("id-partie", TypeAnimal.POISSON, 0, 1);
        int scoreApres = partie.getScoreTotal();

        // UNDO
        Partie pUndo = partieService.undo("id-partie");
        assertEquals(0, pUndo.getScoreTotal());
        assertNull(pUndo.getCarte().getCases().stream().filter(c -> c.getX()==0 && c.getY()==1).findFirst().get().getOccupeePar());
        assertEquals(1, pUndo.getPiecesDisponibles().get(TypeAnimal.POISSON)); // Rendu

        // REDO
        Partie pRedo = partieService.redo("id-partie");
        assertEquals(scoreApres, pRedo.getScoreTotal());
        assertEquals(TypeAnimal.POISSON, pRedo.getCarte().getCases().stream().filter(c -> c.getX()==0 && c.getY()==1).findFirst().get().getOccupeePar());
        assertEquals(0, pRedo.getPiecesDisponibles().get(TypeAnimal.POISSON)); // Repris
    }

    // --- TEST RÉGRESSION NIVEAU ---
    @Test
    void testRegressionNiveauUndo() {
        // Simulation : On est au tour 2, avec un score de 60 (cible était 50 au tour 1)
        // Le coup précédent a rapporté 15 points. Si on l'annule, on tombe à 45 < 50.
        partie.setTourCourant(2);
        partie.setScoreCible(66); // 50 + (8*2) = 66 est la cible DU TOUR 2
        partie.setScoreTotal(60);
        
        // On simule qu'on a gagné des pièces bonus au passage de niveau
        partie.getPiecesDisponibles().put(TypeAnimal.OURS, 2); // 1 base + 1 bonus
        
        // On créé un faux historique pour le undo
        Mouvement mvt = new Mouvement(0, 0, TypeAnimal.OURS, 15);
        partie.getHistorique().add(mvt);

        // Action Undo
        Partie result = partieService.undo("id-partie");

        // Vérification Régression
        assertEquals(45, result.getScoreTotal()); // 60 - 15
        assertEquals(1, result.getTourCourant()); // Redescendu au tour 1
        // On devait avoir 2 Ours. On en a récupéré 1 (celui joué), mais on a PERDU le bonus du niveau (-1). 
        // Donc 2 + 1 (rendu) - 1 (bonus perdu) = 2. 
        // Attends, logique correcte : Avant undo j'ai 2. Je rends la pièce jouée -> 3. Je perds le bonus -> 2.
        // Si le coup joué était un Ours.
        
        // Vérifions les appels internes ou l'état final. 
        // Cible tour 1 (hypothèse 50) = cible tour 2 (66) - 16 = 50. Correct.
        assertEquals(50, result.getScoreCible());
    }

    // --- TEST FIN DE PARTIE (BLOCAGE) ---
    @Test
    void testFinPartieBlocage() {
        // SCÉNARIO : Le joueur récupère un POISSON via Undo, 
        // mais toutes les cases EAU sont déjà occupées par d'autres pièces.
        // Il se retrouve donc avec un Poisson et aucune case valide -> GAME OVER.

        // 1. On nettoie
        partie.getPiecesDisponibles().clear();
        partie.getHistorique().clear();
        
        // 2. On occupe manuellement la SEULE case EAU de la carte (0,1)
        Case caseEau = partie.getCarte().getCases().stream()
                .filter(c -> c.getType() == TypeCase.EAU)
                .findFirst().orElseThrow();
        caseEau.setOccupeePar(TypeAnimal.POISSON); // Case EAU bloquée !
        
        // 3. On simule un historique : le dernier coup était sur une case PLAINE (0,0)
        // (Peu importe la logique, l'important c'est que l'undo va libérer une PLAINE et rendre un POISSON)
        partie.getHistorique().add(new Mouvement(0, 0, TypeAnimal.POISSON, 0)); 
        
        // 4. Action Undo
        // - Libère la case (0,0) -> C'est une PLAINE.
        // - Rend 1 POISSON au joueur.
        // - Vérification : Puis-je poser mon Poisson ? 
        //   - Sur (0,0) Plaine ? NON.
        //   - Sur (0,1) Eau ? NON (Occupée).
        // -> BLOCAGE TOTAL.
        Partie result = partieService.undo("id-partie");
        
        assertTrue(result.isTermine(), "La partie doit être terminée (bloquée) car le POISSON récupéré n'a pas d'eau libre");
    }
}