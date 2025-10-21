openapi: 3.0.0
info:
  title: Web2 Game API
  description: API REST pour le projet de jeu Web2, conçue avec une gestion undo/redo côté client (Interacto).
  version: 1.0.0

servers:
  - url: /api

paths:
  /cartes:
    get:
      summary: Récupère la liste des cartes de jeu disponibles.
      tags:
        - Cartes
      responses:
        '200':
          description: Une liste de cartes.
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Carte'

  /cartes/aleatoire:
    post:
      summary: Génère une nouvelle carte aléatoire.
      tags:
        - Cartes
      responses:
        '201':
          description: La carte aléatoire a été créée et sauvegardée.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Carte'

  /parties:
    post:
      summary: Démarre une nouvelle partie.
      tags:
        - Parties
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [carteId, nomJoueur]
              properties:
                carteId:
                  type: string
                  description: L'ID de la carte à utiliser.
                nomJoueur:
                  type: string
                  description: Le nom du joueur.
      responses:
        '201':
          description: La partie a été créée.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Partie'

  /parties/{id}:
    get:
      summary: Récupère l'état actuel d'une partie.
      tags:
        - Parties
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: L'ID de la partie.
      responses:
        '200':
          description: L'état de la partie.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Partie'
        '404':
          description: Partie non trouvée.

  /parties/{id}/placer:
    post:
      summary: Place une pièce sur le plateau.
      tags:
        - Parties
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [typeAnimal, x, y]
              properties:
                typeAnimal:
                  type: string
                  enum: [OURS, RENARD, POISSON]
                x:
                  type: integer
                  description: Coordonnée X de la case cible.
                y:
                  type: integer
                  description: Coordonnée Y de la case cible.
      responses:
        '200':
          description: Pièce placée. Retourne l'état mis à jour de la partie.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Partie'
        '400':
          description: Mouvement invalide (case occupée, mauvais type de terrain...).

components:
  schemas:
    Case:
      type: object
      properties:
        x:
          type: integer
        y:
          type: integer
        type:
          type: string
          enum: [PLAINE, EAU, ARBRE]
        occupeePar:
          type: object
          nullable: true

    Carte:
      type: object
      properties:
        id:
          type: string
        nom:
          type: string
        largeur:
          type: integer
        hauteur:
          type: integer
        cases:
          type: array
          items:
            type: array
            items:
              $ref: '#/components/schemas/Case'

    Joueur:
      type: object
      properties:
        id:
          type: string
        nom:
          type: string
        scoreCourant:
          type: integer

    Partie:
      type: object
      properties:
        id:
          type: string
        joueur:
          $ref: '#/components/schemas/Joueur'
        map:
          $ref: '#/components/schemas/Carte'
        scoreTotal:
          type: integer
        termine:
          type: boolean
