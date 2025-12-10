package game.service;

import game.model.Carte;
import game.model.Case;
import game.model.TypeCase;
import game.repository.CarteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class CarteService {

    @Autowired
    private CarteRepository carteRepository;

    private final Random random = new Random();

    public List<Carte> listerCartes() {
        return carteRepository.findAll();
    }

    @Transactional
    public Carte genererCarteAleatoire() {
        String id = UUID.randomUUID().toString();
        String nom = "Carte Aléatoire " + id.substring(0, 4);
        int taille = 8;

        Carte carte = new Carte(id, nom, taille, taille);
        List<Case> cases = new ArrayList<>();

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                TypeCase type = determinerTypeAleatoire();
                cases.add(new Case(x, y, type, carte));
            }
        }
        carte.setCases(cases);
        return carteRepository.save(carte);
    }

    private TypeCase determinerTypeAleatoire() {
        int rand = random.nextInt(100);
        if (rand < 60) return TypeCase.PLAINE;
        if (rand < 80) return TypeCase.ARBRE;
        return TypeCase.EAU;
    }
}