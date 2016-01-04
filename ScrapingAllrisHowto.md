# Einleitung #

Das Allris-Ratsinformationssystem weist einige Features auf, die nicht jedem bekannt sein dürften. Abgesehen vom reinen scrapen der Webseiten, besteht die Möglichkeit der XML-Ausgabe einiger Meta-Informationen in aggregierter Form. Dies ermöglicht eine etwas effektivere Herangehensweise. Die angegebenen URLs sind relative URLs, die an die URL des jeweiligen Allris angehängt werden. Das wichtigste "magic fragment" ist `?selfaction=ws&template=xyz`. Leider sind einige der Ausgaben leer oder nicht valides XML.

# Details #

XML-Ausgabe der:

## Ämter ##
`/at010.asp?selfaction=ws&template=xyz`

## Parlamente ##
`/pa010.asp?selfaction=ws&template=xyz`

## Fraktionen ##
`/fr010.asp?selfaction=ws&template=xyz`

## Sitzungen ##
  * **Aktueller Monat:**     `/si010.asp?selfaction=ws&template=xyz`
  * **Datumseinschränkung:**  `/si010.asp?selfaction=ws&template=xyz&kaldatvon=01.01.2006&kaldatbis=31.12.2012`
  * **alle**                 `/si010.asp?selfaction=ws&template=xyz&showall=true`
  * **bestimmte Sitzung (nach SILFDNR aus URL)**
    * Anwesenheit:           `/si019.asp?selfaction=ws&template=xyz&SILFDNR=___`
    * Tagesordnung:          `/to010.asp?selfaction=ws&template=xyz&SILFDNR=___`
    * Niederschrift:         `/si016.asp?selfaction=ws&template=xyz&SILFDNR=___`

## offene Vorlagen ##
`/vo010.asp?selfaction=ws&template=xyz`

## Drucksachen ##
**alle (leider ohne Texte):**   `/vo040.asp?selfaction=ws&template=xyz`