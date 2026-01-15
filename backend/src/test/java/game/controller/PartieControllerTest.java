package game.controller;

import game.model.Partie;
import game.service.PartieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartieController.class)
public class PartieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartieService partieService;

    @Test
    void testDemarrerPartie() throws Exception {
        Partie p = new Partie();
        p.setId("123");
        when(partieService.demarrerPartie(anyString(), anyString())).thenReturn(p);

        mockMvc.perform(post("/api/parties")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carteId\":\"c1\", \"nomJoueur\":\"Evan\"}"))
                // CORRECTION ICI : on attend created() (201) et pas ok() (200)
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void testPlacerPiece() throws Exception {
        Partie p = new Partie();
        p.setScoreTotal(10);
        when(partieService.placerPiece(anyString(), any(), anyInt(), anyInt())).thenReturn(p);

        mockMvc.perform(post("/api/parties/123/placer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"typeAnimal\":\"OURS\", \"x\":0, \"y\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scoreTotal").value(10));
    }

    @Test
    void testUndo() throws Exception {
        when(partieService.undo("123")).thenReturn(new Partie());

        mockMvc.perform(post("/api/parties/123/undo"))
                .andExpect(status().isOk());
    }

    @Test
    void testRecupererPartie() throws Exception {
        Partie p = new Partie();
        p.setId("123");
        when(partieService.recupererPartie("123")).thenReturn(p);

        mockMvc.perform(get("/api/parties/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }
}