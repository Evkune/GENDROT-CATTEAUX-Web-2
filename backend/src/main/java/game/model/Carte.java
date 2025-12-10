package game.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Carte {

    @Id
    private String id;

    private String nom;
    private int largeur;
    private int hauteur;

    @OneToMany(mappedBy = "carte", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Case> cases = new ArrayList<>();

    public Carte() {}

    public Carte(String id, String nom, int largeur, int hauteur) {
        this.id = id;
        this.nom = nom;
        this.largeur = largeur;
        this.hauteur = hauteur;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public int getLargeur() { return largeur; }
    public int getHauteur() { return hauteur; }
    public List<Case> getCases() { return cases; }
    public void setCases(List<Case> cases) { this.cases = cases; }
}