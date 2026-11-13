# 🚦 Demo_Car_Trancking

Application de bureau **Java Swing** permettant de simuler la circulation routière sur plusieurs voies, de configurer les carrefours et d’observer les files d’attente ainsi que les retards des véhicules.

Le projet fonctionne avec **Java 8 ou ultérieur**, utilise le thème **Nimbus** et dispose d’une configuration **NetBeans avec Ant**.

## Origine du projet et contributions

**John Kaiser est le premier auteur de la base historique de ce simulateur.** Son nom figure dans les en-têtes des sources d’origine.

**J’ai repris et amélioré ce projet sous le nom d’Ulb-Kalema — Kalema Daniel Jonathan.** Mes améliorations portent notamment sur la correction des bugs, l’interface Nimbus, la gestion des véhicules par voie, la configuration des carrefours et les statistiques de circulation.

Ce projet conserve ainsi la reconnaissance du travail initial de **John Kaiser**, tout en présentant les évolutions apportées par **Ulb-Kalema**.

> Ce simulateur est un outil de démonstration et d’expérimentation. Il n’est pas validé pour piloter la circulation réelle ou décider seul d’un aménagement routier.

## Aperçu

<img width="1027" height="622" alt="Capture d&#39;écran 2026-11-13 130734" src="https://github.com/user-attachments/assets/1fff1560-d171-47cd-89af-995a85be0c26" />


## Fonctionnalités

- Interface graphique **Swing avec Nimbus**.
- Plusieurs voies de circulation indépendantes.
- Ajout de véhicules sur chaque ligne.
- Ajout de nouvelles voies et de nouveaux carrefours.
- Configuration du nom de chaque carrefour.
- Configuration de la position des carrefours en mètres.
- Configuration de la durée du feu rouge en secondes.
- Vitesse cible réglable de **1 à 180 km/h**.
- Démarrage, pause, reprise et réinitialisation.
- Formation de files d’attente aux feux rouges.
- Espacement minimal entre les véhicules d’une même voie.
- Affichage des positions et des vitesses.
- Calcul des retards par véhicule et par carrefour.
- Suivi des files actuelles et maximales.

## Technologies utilisées

| Technologie | Utilisation |
| --- | --- |
| Java 8+ | Logique de simulation et exécution de l’application. |
| Swing | Interface graphique de bureau. |
| Nimbus | Apparence des composants de l’interface. |
| NetBeans / Ant | Organisation et construction du projet. |

Aucune bibliothèque Java externe ni base de données n’est nécessaire.

## Prérequis

- **Java 8 ou ultérieur** pour exécuter l’application.
- **JDK 8 ou ultérieur** pour compiler les sources.
- **NetBeans avec prise en charge des projets Ant**, si vous utilisez cet environnement.
- Un environnement graphique pour l’application et les tests Swing.

## Installation

1. Télécharger ou cloner le dépôt.
2. Extraire entièrement le projet si vous utilisez une archive.
3. Placer le dossier à l’emplacement souhaité, par exemple :

   ```text
   D:\projets\Demo_Car_Trancking
   ```

4. Vérifier la présence de `build.xml`, `src` et `nbproject`.

## Lancer l’application

### Depuis le JAR

Si le fichier compilé est présent, exécuter depuis la racine du projet :

```bat
java -jar dist\Demo_Car_Trancking.jar
```

### Depuis NetBeans

1. Sélectionner **Fichier → Ouvrir un projet**.
2. Choisir le dossier contenant `build.xml` et `nbproject`.
3. Effectuer **Clean and Build**.
4. Cliquer sur **Run / Exécuter**.

Classe principale :

```text
demo_car_trancking.Demo_Car_Trancking
```

### Compiler sans NetBeans

Depuis la racine du projet, dans PowerShell :

```powershell
New-Item -ItemType Directory -Force build/classes | Out-Null

$sources = Get-ChildItem src/demo_car_trancking/*.java

javac -encoding UTF-8 -d build/classes $sources.FullName

if ($LASTEXITCODE -ne 0) {
    throw "La compilation a échoué."
}

java -cp build/classes demo_car_trancking.Demo_Car_Trancking
```

Les commandes `java` et `javac` doivent être disponibles dans le `PATH`.

## Utilisation

### Configuration initiale

Au démarrage, le simulateur contient :

- **4 voies**, avec un véhicule sur chacune.
- **3 carrefours**, placés à 1 000, 2 000 et 3 000 mètres.
- Une vitesse cible de **50 km/h**.

### Ajouter des véhicules sur une ligne

Cliquer sur le bouton correspondant :

```text
Ligne 1 : + véhicule
Ligne 2 : + véhicule
Ligne 3 : + véhicule
Ligne 4 : + véhicule
```

Un véhicule est ajouté à une position disponible sur la voie choisie.

Les véhicules d’une même voie respectent un espacement minimal de **7 mètres**. Les véhicules de voies différentes ne se bloquent pas mutuellement.

### Ajouter une voie ou un carrefour

- **Ajouter une ligne** crée une voie vide avec son bouton d’ajout de véhicules.
- **Ajouter un carrefour** prolonge la route avec un nouveau carrefour placé 1 000 mètres après le dernier.

### Configurer les carrefours

1. Cliquer sur **Arrêter / réinitialiser**, si nécessaire.
2. Cliquer sur **Configurer les carrefours**.
3. Modifier les champs souhaités.
4. Cliquer sur **Appliquer** pour chaque carrefour modifié.

| Champ | Description |
| --- | --- |
| Nom | Nom affiché dans le schéma et les statistiques. |
| Depuis départ (m) | Position du carrefour depuis le début de la route. |
| Rouge (secondes) | Durée du feu rouge, comprise entre 1 et 3 600 secondes. |

Exemple :

| Nom | Position depuis le départ | Durée du rouge |
| --- | ---: | ---: |
| Marché | 800 m | 20 s |
| École | 1 600 m | 30 s |
| Gare | 2 500 m | 15 s |

> Les positions doivent être strictement croissantes. Le champ des mètres indique la distance depuis le départ, et non depuis le carrefour précédent.

La position du dernier carrefour définit la longueur de la route en boucle.

Une longueur incompatible avec les positions initiales des véhicules est refusée.

### Commandes de simulation

| Commande | Effet |
| --- | --- |
| Démarrer | Lance les véhicules et le cycle des feux. |
| Pause | Fige les déplacements, les feux et le temps simulé. |
| Reprendre | Continue depuis l’état mis en pause. |
| Arrêter / réinitialiser | Restaure les positions initiales et remet les feux et statistiques à leur état initial. |

L’horloge affiche l’heure réelle. Le compteur de simulation indique le temps écoulé dans le modèle.

## Fonctionnement des feux

Chaque carrefour suit ce cycle :

```text
Vert → Orange → Rouge → Vert
```

| Phase | Durée |
| --- | --- |
| Vert | 10 secondes. |
| Orange | 5 secondes. |
| Rouge | Durée configurée pour le carrefour. |

Les véhicules passent au vert et à l’orange. Ils s’arrêtent au rouge et attendent derrière le véhicule précédent si la voie est occupée.

Le feu d’un carrefour s’applique à toutes les voies à sa position.

## Files d’attente

Les files se forment indépendamment sur chaque voie.

Lorsqu’un véhicule s’arrête au rouge, les véhicules suivants attendent derrière lui en conservant l’espacement minimal.

L’espacement de **7 mètres** représente :

```text
4 mètres de véhicule + 3 mètres de marge
```

Au vert, les véhicules peuvent repartir.

Le modèle ne prend pas encore en compte l’accélération progressive ni le temps de réaction des conducteurs.

## Statistiques

### Par véhicule

| Indicateur | Description |
| --- | --- |
| Véhicule | Identifiant et voie utilisée. |
| X (m) | Position sur la route. |
| Y (m) | Coordonnée fixée à zéro dans ce modèle. |
| Vitesse (km/h) | Vitesse calculée à partir du déplacement. |
| Retard (s) | Temps perdu par rapport à la vitesse cible. |
| À l’arrêt (s) | Durée cumulée des pas sans déplacement. |
| File au carrefour | Carrefour responsable du ralentissement courant. |

### Par carrefour

| Indicateur | Description |
| --- | --- |
| Carrefour | Nom et position. |
| Feu | Couleur actuelle. |
| File actuelle | Nombre de véhicules immobiles attribués au carrefour, toutes voies confondues. |
| File maximale | Plus grande file observée depuis la réinitialisation. |
| Retard cumulé | Somme des retards attribués au carrefour. |

### Calcul du retard

```text
Vitesse en m/s = vitesse en km/h ÷ 3,6

Retard = durée du pas − distance parcourue ÷ vitesse cible en m/s
```

Exemple :

```text
3 véhicules arrêtés pendant 2 secondes
= 6 véhicules-secondes de retard cumulé
```

- Le ralentissement partiel est inclus.
- Le temps passé en pause est exclu.
- Le retard moyen inclut tous les véhicules présents.
- Changer la vitesse cible ne recalcule pas les retards passés.

## Conservation des paramètres

Les paramètres des carrefours sont conservés après une réinitialisation.

Cependant, ils restent en mémoire :

- Pas de sauvegarde du scénario après fermeture.
- Pas de conservation permanente des statistiques.
- Pas d’export CSV dans la version actuelle.


## Architecture

| Fichier | Rôle |
| --- | --- |
| `Demo_Car_Trancking.java` | Point d’entrée de l’application. |
| `CMSC335Project3.java` | Interface Swing, thème Nimbus et interactions utilisateur. |
| `Simulation.java` | Voies, déplacements, contraintes et statistiques. |
| `Car.java` | État et mesures d’un véhicule. |
| `Intersection.java` | Configuration et cycle d’un feu. |
| `Time.java` | Horloge réelle. |

L’interface utilise un minuteur Swing de **50 ms**. Les déplacements sont calculés avec des sous-pas de **20 ms maximum**.

## Tests

Les tests sont des programmes Java autonomes, sans JUnit.

Depuis la racine du projet, dans PowerShell :

```powershell
New-Item -ItemType Directory -Force build/check | Out-Null

$sources = Get-ChildItem src/demo_car_trancking/*.java
$tests = Get-ChildItem test/demo_car_trancking/*.java

javac -encoding UTF-8 -d build/check $sources.FullName $tests.FullName

if ($LASTEXITCODE -ne 0) {
    throw "La compilation des tests a échoué."
}

foreach ($test in @(
    "SimulationTest",
    "QueueTest",
    "ConfigurationTest"
)) {
    java -cp build/check "demo_car_trancking.$test"

    if ($LASTEXITCODE -ne 0) {
        throw "Échec du test : $test"
    }
}
```

Chaque suite affiche `PASS` lorsqu’elle réussit.

`SimulationTest` nécessite un environnement graphique pour construire et fermer la fenêtre Swing.

### Vérifications effectuées

- Compilation avec **JDK 1.8.0_92**.
- Démarrage, pause, reprise et redémarrage.
- Construction et fermeture de l’interface.
- Activation du thème Nimbus.
- Conversion des vitesses et calcul des distances.
- Arrêt au bon feu rouge.
- Formation et libération des files.
- Espacement des véhicules, y compris à la jonction de la boucle.
- Calcul et attribution des retards.
- Indépendance des voies.
- Ajout de véhicules sur la voie choisie.
- Configuration des noms, distances et durées du rouge.
- Rejet des configurations incohérentes.

## Limites actuelles

- Route droite et bouclée.
- Pas de dépassement ni de changement de voie.
- Pas de freinage ou d’accélération progressifs.
- Pas de temps de réaction au redémarrage.
- Pas de piétons, de virages ou de flux croisés.
- Vitesse cible commune à tous les véhicules.
- Durées du vert et de l’orange fixes.
- Pas de sauvegarde permanente ou d’export des résultats.
- Pas de données cartographiques ni de calibration terrain.
- Symboles graphiques des véhicules non représentés à l’échelle.

Après un blocage de l’interface ou une mise en veille, le rattrapage est limité. Le temps simulé peut donc progresser plus lentement que le temps réel.

## Évolutions envisagées

- [ ] Sauvegarder et recharger les scénarios.
- [ ] Exporter les statistiques au format CSV.
- [ ] Configurer les durées du vert et de l’orange.
- [ ] Ajouter l’accélération et le freinage progressifs.
- [ ] Simuler le temps de réaction des conducteurs.
- [ ] Mesurer le débit de chaque carrefour.
- [ ] Comparer plusieurs plans de feux.
- [ ] Introduire des flux croisés.
- [ ] Comparer les résultats avec des observations de terrain.

## Dépannage

| Problème | Solution |
| --- | --- |
| Java introuvable | Vérifier l’installation et la variable `PATH`. |
| `javac` introuvable | Installer un JDK et ajouter son dossier `bin` au `PATH`. |
| Le JAR ne démarre pas | Le lancer dans un terminal pour consulter l’erreur. |
| L’ancienne interface apparaît | Vérifier le projet lancé et effectuer **Clean and Build**. |
| Modification d’un carrefour refusée | Arrêter et réinitialiser la simulation avant modification. |
| Distance refusée | Vérifier l’ordre des carrefours et la place nécessaire aux véhicules. |
| Ajout d’un véhicule impossible | Vérifier qu’une position libre existe sur la voie. |
| Paramètres perdus à la fermeture | La sauvegarde permanente n’est pas encore disponible. |

## Contribuer

Les propositions d’amélioration et les signalements de bugs sont les bienvenus.

Pour signaler un problème, préciser :

- La version de Java.
- Les paramètres des carrefours.
- Le nombre de voies et de véhicules.
- Les étapes de reproduction.
- Le résultat attendu et le résultat observé.

Avant de proposer une modification, exécuter les tests et documenter les changements de comportement.

## Auteurs et reconnaissance

### Auteur initial

**John Kaiser**

Premier auteur de la base historique du simulateur, mentionné dans les en-têtes des sources d’origine.

### Reprise et améliorations

**Kalema Daniel Jonathan**

Reprise du projet, corrections et développement des fonctionnalités complémentaires de cette version.

📧 [kalemadaniel2014@gmail.com](mailto:kalemadaniel2014@gmail.com)

## Licence

Aucune licence de diffusion n’est encore définie dans cette version.

Les conditions de réutilisation et de redistribution doivent être clarifiées en respectant les droits liés au code initial et aux contributions ultérieures.
