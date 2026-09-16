# SVG Watch OS

Un shell/launcher Android pour smartwatch, conçu pour fonctionner à partir d'Android 7 (API 24).

## Ce que contient cette V1

- Interface plein écran SVG Watch OS
- Launcher pouvant être choisi comme écran d'accueil Android
- Détection Internet
- Météo via Open-Meteo (connexion Internet requise)
- Mini assistant local SVG AI
- Liste et lancement des applications installées
- Paramètres Wi-Fi/Bluetooth/Android
- Diagnostic matériel de base
- Compatible minSdk 24 (Android 7+)

## Important

Cette application **ne remplace pas Android et ne modifie pas le firmware** de la montre.

La météo utilise Paris comme position de démonstration dans cette V1. La localisation automatique pourra être ajoutée après test des permissions GPS sur la montre.

Les fonctions SaveFamily (localisation distante, commandes de la montre, contacts familiaux, etc.) ne sont pas incluses tant qu'une API/protocole officiellement accessible n'est pas disponible.

## Construire

Le workflow GitHub Actions de `.github/workflows/build.yml` compile automatiquement l'APK.

Sur GitHub :
1. Créer un dépôt, par exemple `SVGStudio/SVG-Watch-OS`.
2. Envoyer tous les fichiers de ce projet.
3. Ouvrir **Actions**.
4. Lancer **Build SVG Watch OS**.
5. Télécharger l'APK dans les **Artifacts** du workflow.

Pour un build local avec Gradle installé :
`./gradlew assembleDebug`

L'APK se trouvera dans :
`app/build/outputs/apk/debug/app-debug.apk`
