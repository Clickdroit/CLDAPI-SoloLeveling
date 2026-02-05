package fr.clickdroit.sololeveling.role;

import fr.clickdroit.sololeveling.camp.Camp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour définir les métadonnées d'un rôle.
 * Permet de configurer un rôle de manière déclarative.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RoleInfo {

    /**
     * Le nom du rôle.
     */
    String name();

    /**
     * La description courte du rôle.
     */
    String description();

    /**
     * Le camp auquel appartient le rôle.
     */
    Camp camp();

    /**
     * La rareté du rôle.
     */
    RoleRarity rarity() default RoleRarity.COMMON;

    /**
     * Les lignes de lore pour la description détaillée.
     */
    String[] lore() default {};

    /**
     * Si le rôle est activé par défaut.
     */
    boolean enabled() default true;

    /**
     * Le nombre maximum de ce rôle dans une partie.
     */
    int maxPerGame() default 1;
}
