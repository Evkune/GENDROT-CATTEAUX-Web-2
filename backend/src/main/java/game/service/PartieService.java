package game.service;

import game.model.*;
import game.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PartieService {

    @Autowired
    private PartieRepository partieRepository;
    @Autowired
    private CarteRepository carteRepository;
    @Autowired
    private JoueurRepository joueurRepository;

    @Transactional
    public Partie demarrerPartie(String carteId, String nomJoueur) {
        Joueur joueur = joueurRepository.findById(nomJoueur)
                .orElseGet(() -> joueurRepository.save(new Joueur(nomJoueur)));
        Carte carteModele = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));
        Carte copieCarte = new Carte(
            UUID.randomUUID().toString(), 
            carteModele.getNom(), 
            carteModele.getLargeur(), 
            carteModele.getHauteur()
        );
        List<Case> nouvellesCases = new ArrayList<>();
        for (Case caseModele : carteModele.getCases()) {
            nouvellesCases.add(new Case(
                caseModele.getX(), 
                caseModele.getY(), 
                caseModele.getType(), 
                copieCarte
            ));
        }
        copieCarte.setCases(nouvellesCases);
        Partie partie = new Partie(UUID.randomUUID().toString(), joueur, copieCarte);
        
        return partieRepository.save(partie);
    }

    @Transactional
    public Partie placerPiece(String partieId, TypeAnimal animal, int x, int y) {
        Partie partie = partieRepository.findById(partieId)
                .orElseThrow(() -> new RuntimeException("Partie non trouvée"));

        if (partie.isTermine()) {
            throw new IllegalArgumentException("La partie est terminée !");
        }

        if (!partie.consommerPiece(animal)) {
            throw new IllegalArgumentException("Vous n'avez plus de pièce de type " + animal);
        }

        Carte carte = partie.getCarte();
        Case caseCible = trouverCase(carte, x, y);

        if (caseCible.getOccupeePar() != null) {
            throw new IllegalArgumentException("La case (" + x + "," + y + ") est déjà occupée.");
        }

        verifierValiditePlacement(caseCible, animal);

        caseCible.setOccupeePar(animal);
        int pointsGagnes = calculerPoints(carte, caseCible, animal);
        partie.setScoreTotal(partie.getScoreTotal() + pointsGagnes);

        gererPassageTour(partie);

        verifierFinPartie(partie);

        return partieRepository.save(partie);
    }

    private void gererPassageTour(Partie partie) {
        while (partie.getScoreTotal() >= partie.getScoreCible()) {
            partie.setTourCourant(partie.getTourCourant() + 1);
            
            int augmentation = 8 * partie.getTourCourant();
            partie.setScoreCible(partie.getScoreCible() + augmentation);

            partie.ajouterPiece(TypeAnimal.OURS, 1);
            partie.ajouterPiece(TypeAnimal.RENARD, 1);
            partie.ajouterPiece(TypeAnimal.POISSON, 1);
        }
    }

    private void verifierFinPartie(Partie partie) {
        boolean plusDePieces = partie.getNombrePiecesRestantes() == 0;
        boolean cartePleine = partie.getCarte().getCases().stream().allMatch(c -> c.getOccupeePar() != null);

        if ((plusDePieces && partie.getScoreTotal() < partie.getScoreCible()) || cartePleine) {
            partie.setTermine(true);
        }
    }

    private Case trouverCase(Carte carte, int x, int y) {
        return carte.getCases().stream()
                .filter(c -> c.getX() == x && c.getY() == y)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Case hors limite : " + x + "," + y));
    }

    private void verifierValiditePlacement(Case c, TypeAnimal animal) {
        TypeCase terrain = c.getType();
        switch (animal) {
            case POISSON:
                if (terrain != TypeCase.EAU) {
                    throw new IllegalArgumentException("Le POISSON doit être placé sur l'EAU.");
                }
                break;
            case OURS:
            case RENARD:
                if (terrain == TypeCase.EAU) {
                    throw new IllegalArgumentException("L'animal " + animal + " ne peut pas être placé sur l'EAU.");
                }
                break;
        }
    }

    private int calculerPoints(Carte carte, Case caseCible, TypeAnimal animal) {
        int score = 0;
        int rayon = (animal == TypeAnimal.OURS) ? 2 : 1;

        if (animal == TypeAnimal.OURS) score += 6;
        else if (animal == TypeAnimal.RENARD) score += 5;
        else if (animal == TypeAnimal.POISSON) score += 8;

        List<Case> voisins = recupererVoisins(carte, caseCible, rayon);

        for (Case v : voisins) {
            if (animal == TypeAnimal.OURS) {
                if (v.getType() == TypeCase.ARBRE) score += 4;
                if (v.getOccupeePar() == TypeAnimal.POISSON) score += 7;
                if (v.getOccupeePar() == TypeAnimal.OURS) score -= 5;
            } else if (animal == TypeAnimal.RENARD) {
                if (v.getType() == TypeCase.PLAINE) score += 7;
                if (v.getOccupeePar() == TypeAnimal.RENARD) score -= 2;
            } else if (animal == TypeAnimal.POISSON) {
                if (v.getType() == TypeCase.EAU) score += 5;
                if (v.getOccupeePar() == TypeAnimal.POISSON) score -= 2;
            }
        }
        return score;
    }

    private List<Case> recupererVoisins(Carte carte, Case centre, int rayon) {
        int xMin = centre.getX() - rayon;
        int xMax = centre.getX() + rayon;
        int yMin = centre.getY() - rayon;
        int yMax = centre.getY() + rayon;

        return carte.getCases().stream()
                .filter(c -> c.getX() >= xMin && c.getX() <= xMax
                          && c.getY() >= yMin && c.getY() <= yMax
                          && !(c.getX() == centre.getX() && c.getY() == centre.getY()))
                .toList();
    }
}