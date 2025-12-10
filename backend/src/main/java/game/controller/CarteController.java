package game.controller;

import game.model.Carte;
import game.service.CarteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartes")
@CrossOrigin(origins = "*")
public class CarteController {

    @Autowired
    private CarteService carteService;

    @GetMapping
    public List<Carte> listerCartes() {
        return carteService.listerCartes();
    }

    @PostMapping("/aleatoire")
    @ResponseStatus(HttpStatus.CREATED)
    public Carte genererCarteAleatoire() {
        return carteService.genererCarteAleatoire();
    }
}