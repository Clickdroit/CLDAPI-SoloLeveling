

## Compilation depuis PowerShell

La toolchain utilise Java 21 et compile du bytecode Java 8. Construire d’abord CLDAPI dans le dossier voisin : le build attend `../CLDAPI/build/libs/UHC-1.0.jar`.

Depuis la racine du dépôt :

```powershell
java -version
.\gradlew.bat --no-daemon build
```

Le wrapper fourni choisit la version de Gradle du projet. Sa première
exécution peut télécharger Gradle et les dépendances. Une compilation réussie
ne vérifie pas le comportement sur un serveur réel : tester ensuite sur une
instance de développement avec sa configuration dédiée.
