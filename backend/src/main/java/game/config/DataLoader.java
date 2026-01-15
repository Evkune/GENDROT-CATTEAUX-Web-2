package game.config;

import game.model.Carte;
import game.model.Case;
import game.model.TypeCase;
import game.repository.CarteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initData(CarteRepository carteRepository) {
        return args -> {
            
            // --- CARTE 1 : L'ARCHIPEL (8x8) ---
            // Thème : Eau dominante avec des îlots. Difficile pour les Ours, bon pour les Poissons.
            if (!carteRepository.existsById("map-1")) {
                String schemaArchipel = """
                    P P E E E E A P
                    P P E E E P P A
                    E E E P P E E E
                    E P P A P E E E
                    E P E E E P P E
                    P A E P E E A P
                    P P E E P P P P
                    A P P E E E A A
                    """;
                creerCarteDepuisSchema(carteRepository, "map-1", "L'Archipel", 8, 8, schemaArchipel);
            }

            // --- CARTE 2 : LA VALLÉE (8x8) ---
            // Thème : Une rivière centrale qui serpente, bordée de forêts. Équilibrée.
            if (!carteRepository.existsById("map-2")) {
                String schemaVallee = """
                    A A P P E P P A
                    A A P P E P P A
                    A P P P E P P P
                    P P P E E E P P
                    P P P E E E P P
                    A P P P E P P P
                    A A P P E P P A
                    A A P P E P P A
                    """;
                creerCarteDepuisSchema(carteRepository, "map-2", "La Vallée", 8, 8, schemaVallee);
            }
        };
    }

    /**
     * Méthode utilitaire pour générer une carte à partir d'un "dessin" en texte.
     * Lettres acceptées : 'E' (Eau), 'A' (Arbre), tout le reste devient 'P' (Plaine).
     */
    private void creerCarteDepuisSchema(CarteRepository repo, String id, String nom, int largeur, int hauteur, String schema) {
        Carte carte = new Carte(id, nom, largeur, hauteur);
        carte.setModele(true); // Marque cette carte comme modèle visible dans le menu
        List<Case> cases = new ArrayList<>();

        // Nettoyage du schéma
        String cleanSchema = schema.replace("\n", "").replace(" ", "").replace("\r", "");

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                // Protection index
                if ((y * largeur + x) >= cleanSchema.length()) break;
                
                char code = cleanSchema.charAt(y * largeur + x);
                TypeCase type = switch(code) {
                    case 'E' -> TypeCase.EAU;
                    case 'A' -> TypeCase.ARBRE;
                    default -> TypeCase.PLAINE;
                };
                cases.add(new Case(x, y, type, carte));
            }
        }
        carte.setCases(cases);
        repo.save(carte);
    }
}