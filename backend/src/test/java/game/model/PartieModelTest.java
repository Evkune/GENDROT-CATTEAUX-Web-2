package game.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PartieModelTest {

    @Test
    void testConsommerPiece() {
        Partie p = new Partie();
        // On initialise manuellement la map car le constructeur par défaut ne le fait pas forcément
        p.getPiecesDisponibles().put(TypeAnimal.OURS, 1);

        // Cas 1 : Consommation réussie
        boolean result1 = p.consommerPiece(TypeAnimal.OURS);
        assertTrue(result1);
        assertEquals(0, p.getPiecesDisponibles().get(TypeAnimal.OURS));

        // Cas 2 : Pas assez de pièces
        boolean result2 = p.consommerPiece(TypeAnimal.OURS);
        assertFalse(result2);

        // Cas 3 : Animal inexistant
        boolean result3 = p.consommerPiece(TypeAnimal.RENARD);
        assertFalse(result3);
    }

    @Test
    void testAjouterPiece() {
        Partie p = new Partie();
        p.ajouterPiece(TypeAnimal.POISSON, 2);
        assertEquals(2, p.getPiecesDisponibles().get(TypeAnimal.POISSON));

        p.ajouterPiece(TypeAnimal.POISSON, 1);
        assertEquals(3, p.getPiecesDisponibles().get(TypeAnimal.POISSON));
    }
}