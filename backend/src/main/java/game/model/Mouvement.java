package game.model;

import javax.persistence.Embeddable;

@Embeddable
public class Mouvement {
    private int x;
    private int y;
    private TypeAnimal animal;
    private int pointsGagnes;

    public Mouvement() {}

    public Mouvement(int x, int y, TypeAnimal animal, int pointsGagnes) {
        this.x = x;
        this.y = y;
        this.animal = animal;
        this.pointsGagnes = pointsGagnes;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public TypeAnimal getAnimal() { return animal; }
    public int getPointsGagnes() { return pointsGagnes; }
}