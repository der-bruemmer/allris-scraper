# Überblick #

Das Projekt bietet einen Java-basierten Scraper für das Ratsinformationssystem Allris. Der Fokus liegt dabei auf einer Erweiterung des Funktionsumfangs von Allris, vor allem der weiteren Öffnung der zur Verfügung gestellten Daten, insbesondere der Drucksachen.

Gescrapet werden die Drucksachen, inklusive des enthaltenen Textes, der Verfasser und beteiligten Fraktionen, sowie Links zum relevanten Beschluss. Außerdem werden Fraktionen und Daten über die Fraktionen gescrapet, sowie Mitglieder der Fraktionen, letztere allerdings ohne persönliche Daten.

Die Scraper erstellen Java-Objekte, die anschließend von einem Writer in gewünschten Formaten (derzeit nur RDF) ausgegeben werden können. Die schiere Menge der Drucksachen erfordert, dass der Scraper diese lokal zwischenspeichert, sie werden später vom Writer wieder eingelesen.

Der Output erfolgt derzeit nur in einen RDF-Dump. Dabei werden die Drucksachen, die Fraktionen und beteiligte Personen zu eigenen Resourcen transformiert, um ein semantisches Netz zu schaffen.

# Benutzung #
**Der Scraper funktioniert momentan nur für Allris-Systeme, bei denen XML-Ausgabe aktiviert ist, wie in Berlin der Fall.**

Derzeit noch: Projekt in IDE importieren, libs in den /resources in den builpath integrieren, config anpassen, AllrisScraper.java ausführen. jar und maven-build sind geplant

Für die tatsächliche Nutzung der entstehenden Daten ist ein **Server mit Virtuoso erforderlich, den ich nicht habe**. Dann kann mit SPARQL anfragen auf die Daten zugegriffen werden, bzw andere SemanticWebTools wie OntoWiki verwendet werden. Alternativ kann ein SQLWriter implementiert werden, um die Daten in eine relationale Datenbank zu transferieren.



# Todo #
  * Das uneindeutige Encoding von ALLRIS (irgendetwas zwischen UTF-8, ISO8859 und cp1252) erfodert noch ein "wenig" Detailarbeit. Momentan sind noch viele Encoding-Fehler im Output.
  * Drucksachen sollten Datumsbasiert gescrapt werden können, zur Zeit geht nur ein komplettes Scraping, das Stunden dauert.
  * Um die Initiatoren und die Fraktionen besser zu matchen, wird ein fuzzy matching benötigt.
  * Named Entity Recognition in den Texten der Drucksachen würde eine granularere Suche ermöglichen (Ort, Personen, Fraktionen, Institutionen).
  * Ausschüsse und Abteilungen sind oft Initatoren von Drucksachen, werden aber nicht gescrapet.