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
            String idCarte = "carte-1";
            if (!carteRepository.existsById(idCarte)) {
                Carte carte = new Carte(idCarte, "Carte Démo", 8, 8);
                List<Case> cases = new ArrayList<>();

                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        TypeCase type = TypeCase.PLAINE;
                        if (x >= 3 && x <= 4 && y >= 3 && y <= 4) {
                            type = TypeCase.EAU;
                        }
                        if ((x == 0 || x == 7) && (y == 0 || y == 7)) {
                            type = TypeCase.ARBRE;
                        }
                        cases.add(new Case(x, y, type, carte));
                    }
                }
                carte.setCases(cases);
                carteRepository.save(carte);
                System.out.println("--- CARTE PAR DEFAUT (carte-1) INITIALISEE ---");
            }
        };
    }
}