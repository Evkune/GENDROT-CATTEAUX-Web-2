package game.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Joueur {

    @Id
    private String id;
    
    private String nom;

    public Joueur() {
    }

    public Joueur(String nom) {
        this.id = nom;
        this.nom = nom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}