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
        // 1. On détermine le nom final (on utilise une variable tampon 'nomFinal')
        final String nomFinal;
        if (nomJoueur == null || nomJoueur.trim().isEmpty()) {
            nomFinal = "Joueur-" + UUID.randomUUID().toString().substring(0, 5);
        } else {
            nomFinal = nomJoueur;
        }

        // 2. On utilise 'nomFinal' partout à la place de 'nomJoueur'
        Joueur joueur = joueurRepository.findById(nomFinal)
                .orElseGet(() -> joueurRepository.save(new Joueur(nomFinal)));

        Carte carteModele = carteRepository.findById(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));
        
        Carte copieCarte = new Carte(
            UUID.randomUUID().toString(), 
            carteModele.getNom(), 
            carteModele.getLargeur(), 
            carteModele.getHauteur()
        );
        copieCarte.setModele(false); // Important : ce n'est pas un modèle

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

        if (partie.isTermine()) throw new IllegalArgumentException("La partie est terminée !");
        if (!partie.consommerPiece(animal)) throw new IllegalArgumentException("Plus de pièce " + animal);

        Carte carte = partie.getCarte();
        Case caseCible = trouverCase(carte, x, y);

        if (caseCible.getOccupeePar() != null) throw new IllegalArgumentException("Case occupée.");

        verifierValiditePlacement(caseCible, animal);
        
        caseCible.setOccupeePar(animal);
        int pointsGagnes = calculerPoints(carte, caseCible, animal);
        
        // Historique
        Mouvement mvt = new Mouvement(x, y, animal, pointsGagnes);
        partie.getHistorique().add(mvt);
        partie.getRedoStack().clear(); // On vide le redo quand on joue un nouveau coup
        
        // Score
        partie.setScoreTotal(partie.getScoreTotal() + pointsGagnes);

        // Gestion du niveau (Level Up)
        gererPassageTour(partie);

        verifierFinPartie(partie);

        return partieRepository.save(partie);
    }

    // --- LOGIQUE UNDO / REDO ---

    @Transactional
    public Partie undo(String partieId) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        List<Mouvement> historique = partie.getHistorique();

        if (historique.isEmpty()) return partie;

        Mouvement dernierCoup = historique.remove(historique.size() - 1);

        Case c = trouverCase(partie.getCarte(), dernierCoup.getX(), dernierCoup.getY());
        c.setOccupeePar(null);

        partie.ajouterPiece(dernierCoup.getAnimal(), 1);
        partie.setScoreTotal(partie.getScoreTotal() - dernierCoup.getPointsGagnes());

        gererRegressionTour(partie);
        
        // --- CORRECTIF IMPORTANT ---
        // 1. On réouvre la partie par défaut (car on récupère une pièce)
        partie.setTermine(false);
        
        // 2. MAIS on vérifie tout de suite si on n'est pas bloqué malgré tout
        verifierFinPartie(partie);
        // ---------------------------

        partie.getRedoStack().add(dernierCoup);

        return partieRepository.save(partie);
    }

    @Transactional
    public Partie redo(String partieId) {
        Partie partie = partieRepository.findById(partieId).orElseThrow();
        List<Mouvement> redoStack = partie.getRedoStack();

        if (redoStack.isEmpty()) return partie;

        // 1. Récupérer le coup à refaire
        Mouvement coupARefaire = redoStack.remove(redoStack.size() - 1);

        // 2. Ré-appliquer les effets
        Case c = trouverCase(partie.getCarte(), coupARefaire.getX(), coupARefaire.getY());
        c.setOccupeePar(coupARefaire.getAnimal());

        // 3. Re-consommer la pièce
        // Note : On force la consommation car on sait qu'on l'avait
        partie.consommerPiece(coupARefaire.getAnimal());

        // 4. Re-ajouter le score
        partie.setScoreTotal(partie.getScoreTotal() + coupARefaire.getPointsGagnes());

        // 5. AJOUT : Re-vérifier si cela déclenche un passage de niveau
        // (Comme dans placerPiece, car refaire le coup redonne les points)
        gererPassageTour(partie);

        // 6. Remettre dans l'historique
        partie.getHistorique().add(coupARefaire);
        
        verifierFinPartie(partie);

        return partieRepository.save(partie);
    }

    // --- LOGIQUE MÉTIER ---

    private void gererPassageTour(Partie partie) {
        while (partie.getScoreTotal() >= partie.getScoreCible()) {
            partie.setTourCourant(partie.getTourCourant() + 1);
            
            // Formule : Augmentation = 8 * Nouveau Tour
            int augmentation = 8 * partie.getTourCourant();
            partie.setScoreCible(partie.getScoreCible() + augmentation);

            partie.ajouterPiece(TypeAnimal.OURS, 1);
            partie.ajouterPiece(TypeAnimal.RENARD, 1);
            partie.ajouterPiece(TypeAnimal.POISSON, 1);
        }
    }

    // NOUVELLE MÉTHODE : Inverse exact de gererPassageTour
    private void gererRegressionTour(Partie partie) {
        // Tant que le score est inférieur au seuil du niveau précédent...
        while (partie.getTourCourant() > 1) {
            
            // On recalcule quel était le seuil du niveau d'avant
            // Lors du passage, on a fait : cible = ciblePrecedente + (8 * tourActuel)
            // Donc : ciblePrecedente = cible - (8 * tourActuel)
            int augmentationDernierTour = 8 * partie.getTourCourant();
            int scoreCiblePrecedent = partie.getScoreCible() - augmentationDernierTour;

            if (partie.getScoreTotal() < scoreCiblePrecedent) {
                // On est redescendu sous le seuil !
                
                // 1. On remet les valeurs de tour et de cible
                partie.setScoreCible(scoreCiblePrecedent);
                partie.setTourCourant(partie.getTourCourant() - 1);

                // 2. On RETIRE les pièces bonus qui avaient été données
                partie.consommerPiece(TypeAnimal.OURS);
                partie.consommerPiece(TypeAnimal.RENARD);
                partie.consommerPiece(TypeAnimal.POISSON);
            } else {
                // Le score est suffisant pour maintenir le niveau actuel
                break;
            }
        }
    }

    private void verifierFinPartie(Partie partie) {
        boolean plusDePieces = partie.getNombrePiecesRestantes() == 0;
        boolean cartePleine = partie.getCarte().getCases().stream().allMatch(c -> c.getOccupeePar() != null);
        
        // NOUVEAU : On vérifie si le joueur est bloqué
        // (S'il a des pièces, mais qu'il ne peut les poser nulle part)
        boolean estBloque = !plusDePieces && !existeCoupPossible(partie);

        if ((plusDePieces && partie.getScoreTotal() < partie.getScoreCible()) || cartePleine || estBloque) {
            partie.setTermine(true);
        }
    }

    private boolean existeCoupPossible(Partie partie) {
        Carte carte = partie.getCarte();
        
        // Pour chaque type d'animal (Ours, Renard, Poisson)
        for (TypeAnimal animal : TypeAnimal.values()) {
            
            // Si le joueur possède cet animal en stock
            if (partie.getPiecesDisponibles().getOrDefault(animal, 0) > 0) {
                
                // On regarde s'il existe au moins UNE case libre compatible sur la carte
                boolean coupTrouve = carte.getCases().stream().anyMatch(c -> {
                    // Si la case est occupée, on passe
                    if (c.getOccupeePar() != null) return false;
                    
                    // On teste si le placement est valide
                    try {
                        verifierValiditePlacement(c, animal);
                        return true; // C'est valide !
                    } catch (IllegalArgumentException e) {
                        return false; // Ce n'est pas valide (ex: Poisson sur Terre)
                    }
                });
                
                // Si on a trouvé un coup possible pour cet animal, le joueur n'est pas bloqué
                if (coupTrouve) return true;
            }
        }
        
        // On a tout testé, aucun coup n'est possible
        return false;
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
                if (terrain != TypeCase.EAU) throw new IllegalArgumentException("Le POISSON doit être placé sur l'EAU.");
                break;
            case OURS:
            case RENARD:
                if (terrain == TypeCase.EAU) throw new IllegalArgumentException("L'animal " + animal + " ne peut pas être placé sur l'EAU.");
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
                if (v.getOccupeePar() == TypeAnimal.RENARD) score -= 2;
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

    public Partie recupererPartie(String id) {
        return partieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partie introuvable"));
    }
}