package game.dto;

import game.model.TypeAnimal;

public class CoupDto {
    private TypeAnimal typeAnimal;
    private int x;
    private int y;

    public TypeAnimal getTypeAnimal() { return typeAnimal; }
    public void setTypeAnimal(TypeAnimal typeAnimal) { this.typeAnimal = typeAnimal; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
}