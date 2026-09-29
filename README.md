# 🗡️ CLDAPI — Solo Leveling UHC Scenario

Plugin de scénario de jeu **UHC (Ultra Hardcore) Minecraft 1.8.8+**, inspiré de l'univers de **Solo Leveling** et développé comme extension modulaire du framework **`CLDAPI`**.

---

## 📖 Présentation

Ce mode de jeu plonge les joueurs dans un UHC stratégique où chaque participant se voit attribuer un rôle et des pouvoirs inspirés de *Solo Leveling*. Les joueurs sont répartis en camps rivaux et doivent user de leurs compétences uniques pour mener leur faction à la victoire.

---

## ✨ Fonctionnalités & Mécaniques

### 👑 Système de Rôles et de Camps
- **Factions rivales :** Chasseurs, Monarques, Dirigeants, Soldats de l'Ombre, etc.
- **Attribution dynamique :** Distribution automatique ou manuelle des rôles au moment de l'épisode configuré.
- **Chat de faction :** Communication privée entre membres du même camp via `/cchat`.

### ⚡ Pouvoirs Actifs & Passifs
- Système modulaire basé sur la classe `AbstractPower`.
- Capacités avec temps de recharge (cooldowns), effets de potions, alertes sonores et particules customisées.
- Évolution et montée en puissance au fil des éliminations et des phases de jeu.

### 🖥️ Menus de Configuration In-Game (GUIs)
- **`SoloLevelingConfigGUI`** : Panneau d'administration global pour les hôtes de partie.
- **`RoleConfigMainGUI` & `RoleQuantityConfigGUI`** : Paramétrage visuel du nombre et de l'activation de chaque rôle.
- **`RoleDetailGUI`** : Consultation détaillée des caractéristiques d'un rôle.

### ⚔️ Suivi de Combat & Télémétrie
- `CombatStatsListener` : Enregistrement des dégâts, des éliminations et calcul des statistiques de combat.
- Intégration complète avec les mécaniques de réanimation ou d'extraction d'ombres.

---

## ⌨️ Commandes

| Commande | Description | Permission |
| :--- | :--- | :---: |
| `/sololeveling` (ou `/sl`) | Ouvre le menu de configuration principal | Op / Host |
| `/role` | Affiche les détails et la description de son rôle | Joueur |
| `/powers` | Liste les pouvoirs disponibles et leur statut de cooldown | Joueur |
| `/camp` | Affiche la liste des membres connus de son camp | Joueur |
| `/cchat <message>` | Envoie un message sécurisé au chat de son camp | Joueur |

---

## 🛠️ Stack & Dépendances

- **Minecraft Version :** Spigot / Paper 1.8.8+
- **Framework :** [**`CLDAPI`**](https://github.com/Clickdroit/CLDAPI) (API UHC)
- **Build Tool :** Gradle

---

## 📦 Compilation

```bash
# Compiler le plugin
./gradlew build
```

Le fichier JAR se génère dans `build/libs/`. Déposez-le dans le dossier `plugins/` d'un serveur hébergeant également **`CLDAPI`**.
