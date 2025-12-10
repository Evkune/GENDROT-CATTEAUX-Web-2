package game.model;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cases_jeu")
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;

    @Enumerated(EnumType.STRING)
    private TypeCase type;

    @ManyToOne
    @JoinColumn(name = "carte_id")
    @JsonIgnore
    private Carte carte;

    @Enumerated(EnumType.STRING)
    private TypeAnimal occupeePar;

    public Case() {}

    public Case(int x, int y, TypeCase type, Carte carte) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.carte = carte;
    }

    public Long getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public TypeCase getType() { return type; }
    public Carte getCarte() { return carte; }
    public TypeAnimal getOccupeePar() { return occupeePar; }
    public void setOccupeePar(TypeAnimal animal) { this.occupeePar = animal; }
}