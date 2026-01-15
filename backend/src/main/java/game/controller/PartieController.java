package game.controller;

import game.dto.CoupDto;
import game.dto.CreationPartieDto;
import game.model.Partie;
import game.service.PartieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = "*")
public class PartieController {

    @Autowired
    private PartieService partieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Partie demarrerPartie(@RequestBody CreationPartieDto dto) {
        try {
            return partieService.demarrerPartie(dto.getCarteId(), dto.getNomJoueur());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/placer")
    public Partie placerPiece(@PathVariable String id, @RequestBody CoupDto coup) {
        try {
            return partieService.placerPiece(id, coup.getTypeAnimal(), coup.getX(), coup.getY());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie introuvable");
        }
    }
    
    @GetMapping("/{id}")
    public Partie getPartie(@PathVariable String id) {
        try {
            return partieService.recupererPartie(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie introuvable");
        }
    }

    @PostMapping("/{id}/undo")
    public Partie undo(@PathVariable String id) {
        try {
            return partieService.undo(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/redo")
    public Partie redo(@PathVariable String id) {
        try {
            return partieService.redo(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}