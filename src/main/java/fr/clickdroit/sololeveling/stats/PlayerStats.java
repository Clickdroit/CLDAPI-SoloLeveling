package fr.clickdroit.sololeveling.stats;

import java.util.UUID;

/**
 * Statistiques d'un joueur pour une partie.
 * Stocke toutes les données de performance d'un joueur.
 */
public class PlayerStats {

    private final UUID playerUuid;
    private final String playerName;
    
    // Statistiques de combat
    private int kills;
    private int deaths;
    private int assists;
    private double damageDealt;
    private double damageTaken;
    
    // Statistiques de pouvoirs
    private int powersUsed;
    private int powersSuccessful;
    
    // Statistiques de temps
    private long gameStartTime;
    private long survivalTime;
    private boolean alive;
    
    // Statistiques diverses
    private int alliesHelped;
    private int enemiesDetected;

    public PlayerStats(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.kills = 0;
        this.deaths = 0;
        this.assists = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.powersUsed = 0;
        this.powersSuccessful = 0;
        this.gameStartTime = System.currentTimeMillis();
        this.survivalTime = 0;
        this.alive = true;
        this.alliesHelped = 0;
        this.enemiesDetected = 0;
    }

    // ===== Getters =====

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public double getDamageTaken() {
        return damageTaken;
    }

    public int getPowersUsed() {
        return powersUsed;
    }

    public int getPowersSuccessful() {
        return powersSuccessful;
    }

    public long getSurvivalTime() {
        if (alive) {
            return System.currentTimeMillis() - gameStartTime;
        }
        return survivalTime;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getAlliesHelped() {
        return alliesHelped;
    }

    public int getEnemiesDetected() {
        return enemiesDetected;
    }

    // ===== Setters / Incrementers =====

    public void addKill() {
        this.kills++;
    }

    public void addDeath() {
        this.deaths++;
        this.alive = false;
        this.survivalTime = System.currentTimeMillis() - gameStartTime;
    }

    public void addAssist() {
        this.assists++;
    }

    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
    }

    public void addDamageTaken(double damage) {
        this.damageTaken += damage;
    }

    public void addPowerUsed(boolean successful) {
        this.powersUsed++;
        if (successful) {
            this.powersSuccessful++;
        }
    }

    public void addAllyHelped() {
        this.alliesHelped++;
    }

    public void addEnemyDetected() {
        this.enemiesDetected++;
    }

    // ===== Statistiques calculées =====

    /**
     * Calcule le ratio kill/death.
     */
    public double getKDRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    /**
     * Calcule le pourcentage de succès des pouvoirs.
     */
    public double getPowerSuccessRate() {
        if (powersUsed == 0) {
            return 0;
        }
        return (double) powersSuccessful / powersUsed * 100;
    }

    /**
     * Formate le temps de survie en format lisible.
     */
    public String getFormattedSurvivalTime() {
        long seconds = getSurvivalTime() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    /**
     * Calcule un score de performance global.
     * Plus le score est élevé, meilleure est la performance.
     */
    public int calculatePerformanceScore() {
        int score = 0;
        
        // Points pour les kills (100 par kill)
        score += kills * 100;
        
        // Points pour les assists (30 par assist)
        score += assists * 30;
        
        // Points pour les dégâts infligés (1 par 10 dégâts)
        score += (int) (damageDealt / 10);
        
        // Points pour le temps de survie (1 par minute)
        score += (int) (getSurvivalTime() / 60000);
        
        // Bonus si encore en vie
        if (alive) {
            score += 50;
        }
        
        // Bonus pour l'utilisation efficace des pouvoirs
        if (powersUsed > 0) {
            score += (int) (getPowerSuccessRate() / 2);
        }
        
        // Points pour l'aide aux alliés
        score += alliesHelped * 20;
        
        // Malus pour les morts (mais plafonné)
        score -= Math.min(deaths * 50, 150);
        
        return Math.max(0, score);
    }

    /**
     * Retourne un résumé des statistiques.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("§7Kills: §f").append(kills);
        sb.append(" §8| §7Deaths: §f").append(deaths);
        sb.append(" §8| §7K/D: §f").append(String.format("%.2f", getKDRatio()));
        sb.append(" §8| §7Score: §e").append(calculatePerformanceScore());
        return sb.toString();
    }
}
