package game.model;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Entity
public class Partie {

    @Id
    private String id;

    @ManyToOne
    private Joueur joueur;

    @OneToOne(cascade = CascadeType.ALL)
    private Carte carte;

    private int scoreTotal;
    private boolean termine;

    private int tourCourant;
    private int scoreCible;

    @ElementCollection
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "quantite")
    private Map<TypeAnimal, Integer> piecesDisponibles = new HashMap<>();
    @ElementCollection
    private List<Mouvement> historique = new ArrayList<>();
    @ElementCollection
    private List<Mouvement> redoStack = new ArrayList<>();
    public Partie() {}

    public Partie(String id, Joueur joueur, Carte carte) {
        this.id = id;
        this.joueur = joueur;
        this.carte = carte;
        this.scoreTotal = 0;
        this.termine = false;
        this.tourCourant = 1;
        
        this.scoreCible = 8;

        this.piecesDisponibles.put(TypeAnimal.OURS, 1);
        this.piecesDisponibles.put(TypeAnimal.RENARD, 0);
        this.piecesDisponibles.put(TypeAnimal.POISSON, 0);
    }

    public void ajouterPiece(TypeAnimal type, int quantite) {
        this.piecesDisponibles.merge(type, quantite, Integer::sum);
    }

    public boolean consommerPiece(TypeAnimal type) {
        int count = this.piecesDisponibles.getOrDefault(type, 0);
        if (count > 0) {
            this.piecesDisponibles.put(type, count - 1);
            return true;
        }
        return false;
    }

    public int getNombrePiecesRestantes() {
        return piecesDisponibles.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<Mouvement> getHistorique() { return historique; }
    public void setHistorique(List<Mouvement> historique) { this.historique = historique; }

    public List<Mouvement> getRedoStack() { return redoStack; }
    public void setRedoStack(List<Mouvement> redoStack) { this.redoStack = redoStack; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Joueur getJoueur() { return joueur; }
    public Carte getCarte() { return carte; }
    public int getScoreTotal() { return scoreTotal; }
    public void setScoreTotal(int score) { this.scoreTotal = score; }
    public boolean isTermine() { return termine; }
    public void setTermine(boolean termine) { this.termine = termine; }
    public int getTourCourant() { return tourCourant; }
    public void setTourCourant(int tourCourant) { this.tourCourant = tourCourant; }
    public int getScoreCible() { return scoreCible; }
    public void setScoreCible(int scoreCible) { this.scoreCible = scoreCible; }
    public Map<TypeAnimal, Integer> getPiecesDisponibles() { return piecesDisponibles; }
}