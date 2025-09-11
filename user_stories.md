1)On doit avoir un écran d'acceuil qui permet de choisir le nom(pseudo) de l'utilisateur ainsi que la map sur laquelle l'utilisateur veut jouer
    -Les maps peuvent être: soit des maps déjà créées par le développeur, soit une map qui se génère de manière aléatoire(le nom est alors aussi aléatoire) et dans ce cas, la map s'ajoute aux maps créées
    -Le choix des maps doit être un choix unique cliquable qui lance directement la partie
    -La zone où l'utilisateur tape son nom est une zone de texte(validée au lancement de la partie). Avant de cliquer, "PLayer name" est affiché et lorsque l'utilisateur clique, ce texte passe au-dessus (tout en se rétrecissant) et laisse place à la possibilité de taper le vrai pseudo
    -Le texte de cet écran est: "Select a map to play with", "Generate a random map"(cliquable) et "Play with {nom de la map}"(cliquable)

2)On doit avoir un écran principal de la partie qui contient:
    -Un plateau de jeu avec des cases au milieu de l'écran
    -Des choix de cases possibles à ajouter en bas à gauche de l'écran
    -Le score de l'utilisateur contenu dans un cercle avec le tour actif en haut à gauche, il y a également le nom du projet (Web2 Frontend)
    -Le nom de la map choisie avec le nom du joueur dans un rectangle (si aucun nom n'est précisé, un nom aléatoire est créé pour lui)
    -Une barre horizontale contenant 3 boutons cliquables (Undo/redo et End Game) en bas à droite
    -La case sélectionnée qui apparaît en bas encore plus à droite

3)Le plateau est:
    -Carré contenant 100 cases(10*10)
    -En 3d, positionné vers l'utilisateur à 45°
    -Tournable sur 360° en maintenant la souris sur la map et en la déplaçant horizontalement

4)Le plateau contient 3 types de cases:
    -Des cases vertes représentant une plaine d'herbe
    -Des cases bleues représentant une étendue d'eau
    -Des cases vertes avec des arbres dessus

5)Les cases à placer peuvent être de 3 types:
    -Un ours pouvant être placé uniquement sur la plaine et les arbres
    -Un renard pouvant être placé uniquement sur la plaine et les arbres
    -Un poisson pouvant être placé que sur les cases eau

6)Quand une case à placer est sélectionnée et que l'on hover sur les cases de la map:
    -La case hovered se grise et s'enfonce légèrement
    -Le nombre de points possibles à gagner s'affiche sur la case hovered
    -Selon le type de case (animal) sélectionné, les cases alentours ie son range (2 pour l'ours ie carré de 5*5, 1 pour le poisson et le renard ie carré de 3*3) affichent le nombre de points que l'utilisateur gagne s'il place sa case à l'endroit du curseur

7)Fonctionnement du cercle des points
    -Le cercle contient le nombre de points actuels ainsi que le score à atteindre (sous la forme points/score)
    -Le contour du cercle se remplit (se grise) en fonction du pourcentage des points du tour par rapport à la différence entre l'ancien score et le nouveau score 

8)Fonctionnement d'un tour
    -Au premier tour, l'utilisateur a à sa disposition une seule case ours à placer
    -À chaque tour suivant, il débloque une case de chaque
    -À chaque tour, l'utilisateur doit atteindre un certain score pour passer au tour suivant (ce score à atteindre est exponentiellement plus grand à chaque tour)
    -Le score à atteindre à chaque tour suit une suite quadratique, à chaque tour, l'augmentation du score à atteindre augmente de 10 points supplémentaires

9)En tant qu'utilisateur, je dois voir une animation antre l'écran d'acceuil et l'écran principal qui aggrandit les cases (déjà présentes) afin de former le plateau

10)Précision des points gagnés
    -Ours: 6 points sur la case où il est placé + 4 points par arbre dans son range + 7 points par poisson déjà placé (0 pour l'eau sans poisson) + -5 par ours dans son range
    -Renard: 5 points sur la case où il est placé + 7 sur les cases plaines dans son range + -2 par renard déjà placé dans son range
    -Poisson: 8 sur la case où il est placé + 5 points sur les cases eau dans son range + -2 par poisson déjà placé dans son range

11)Fin du jeu si l'utilisateur ne parvient pas à totaliser assez de points pour atteindre le seuil nécessaire quand il ne lui reste aucune case à placer

12)À la fin de la partie, un message de fin appraraît au milieu de l'écran, annonçant le score de la partie et contenant un bouton permettant de revenir à l'cran d'acceuil(Menu)