import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FraktionScraper {

	private String baseUri;
	private List<Fraktion> fraktionen;
	
	public FraktionScraper(String baseUri) {
		setBaseUri(baseUri);
		this.fraktionen = new ArrayList<Fraktion>();
		
	}
	
	public void setBaseUri(String baseUri) {
		if(baseUri.endsWith("/")) {
			this.baseUri = baseUri.substring(0, baseUri.length()-1);
		} else {
			this.baseUri = baseUri;
		}
	}
	
	public List<Fraktion> parseFraktionen(String fraktionsUri, int timeout) {
		
		List<Fraktion> fraktionen = new ArrayList<Fraktion>();
		
		Connection.Response response = null;
		
		try {
			response = Jsoup.connect(fraktionsUri)
							.userAgent("Uni-Leipzig: ALLRIS Scraper")
							.timeout(timeout)
							.execute();
			
			Document doc = response.parse();
			
			Elements fraktionRows = doc.select("table.tl1 tbody tr.zl12, table.tl1 tbody tr.zl11");
			Iterator<Element> fraktionRowIt = fraktionRows.iterator();
			
			//iterating over fraktion rows
			while(fraktionRowIt.hasNext()) {
				Element fraktionRow = fraktionRowIt.next();
				if(fraktionRow.childNodes().size()==1) {
					//this one is empty...
					continue;
				}
				Fraktion fraktionObject = new Fraktion();
				String idString = fraktionRow.select("td form input[name=FRLFDNR]").attr("value");
				if(idString!=null) {
					fraktionObject.setId(Integer.valueOf(idString));
				}
				
				fraktionObject.setUri(this.baseUri+fraktionRow.select("td.text1 a").attr("href"));
				fraktionObject.setName(this.setFraktionName(fraktionRow.select("td.text1 a").first()));
				String members = fraktionRow.select("td.text5").text();
				if(members.contains("bis")) {
					fraktionObject.setAktuell(false);
				} else {
					fraktionObject.setAktuell(true);
				}
				fraktionen.add(this.parseSingleFraktion(fraktionObject, timeout));
				//System.out.println(fraktionRow.html());
			}
			
			
		} catch(HttpStatusException httpe) {
			if(httpe.getStatusCode()==500) {
				System.out.println("Document " + fraktionsUri + " does not exist");
			}

		} catch(SocketTimeoutException ste) {
			System.out.println(fraktionsUri + " timed out.");

		} catch(IOException ioe) {
			System.out.println(fraktionsUri + " could not be reached.");
	
		} 	
		return fraktionen;	
	}
	
	private Fraktion parseSingleFraktion(Fraktion fraktion, int timeout) {
		
		Connection.Response response = null;
		
		try {
			response = Jsoup.connect(fraktion.getUri())
							.userAgent("Uni-Leipzig: ALLRIS Scraper")
							.timeout(timeout)
							.execute();
			
			Document doc = response.parse();
			
			Elements anschriftElements = doc.select("table.tk1 tbody tr td table tbody tr td");
			fraktion = this.setFraktionAnschrift(fraktion, anschriftElements);
			
			Elements mitgliederElements = doc.select("table.tl1 tbody tr");
			fraktion = this.setFraktionMitglieder(fraktion, mitgliederElements);
			
		} catch(HttpStatusException httpe) {
			if(httpe.getStatusCode()==500) {
				System.out.println("Document " + fraktion.getUri() + " does not exist");
			}

		} catch(SocketTimeoutException ste) {
			System.out.println(fraktion.getUri() + " timed out.");

		} catch(IOException ioe) {
			System.out.println(fraktion.getUri() + " could not be reached.");
	
		}
		
		return fraktion;
	}
	
	private String setFraktionName(Element nameElement) {
		String name = null;
		
		name = nameElement.text();
		name = name.replace("Fraktion", "");
		name = name.replace("der", "");
		name = name.replace("Gruppe", "");
		name = name.trim();

		return name;
	}
	
	private Fraktion setFraktionAnschrift(Fraktion fraktion, Elements anschriftElements) {
		
		Iterator<Element> anschriftIt = anschriftElements.iterator();
		while(anschriftIt.hasNext()) {
			Element anschriftEl = anschriftIt.next();
			String anschrift = "";
			if(!anschriftEl.text().trim().isEmpty()) {
				
				if(!anschriftEl.select("a").isEmpty()) {
					//setting email
					if(anschriftEl.select("a").first().text().contains("@")) {
						fraktion.setEmail(anschriftEl.select("a").first().text().trim());
						
					//setting hp
					} else if(anschriftEl.select("a").first().text().contains("http")) {
						fraktion.setHomepage(anschriftEl.select("a").first().text().trim());
					}
				}  else {
					anschrift=anschriftEl.text().trim();
					if(anschrift.toLowerCase().contains("sprechzeiten")) {
						fraktion.setSprechzeiten(anschrift.replaceFirst("(S|s)prechzeiten(:)*", "").trim());
					} else if(anschrift.trim().matches("[0-9]{5}\\s*[A-Z]{1}.*")) {
						fraktion.appendAdresse(anschrift.trim());
					} else {
						if(anschrift.toLowerCase().contains("straße") || anschrift.toLowerCase().contains("str.") ||
							anschrift.toLowerCase().contains("weg")	|| anschrift.toLowerCase().contains("platz") ||
							anschrift.toLowerCase().contains("allee") || anschrift.toLowerCase().contains("zimmer") ||
							anschrift.toLowerCase().contains("raum")) {
							
							fraktion.appendAdresse(anschrift.trim());
						}
					}
				}
			}
		}
		return fraktion;
	}
	
	public Fraktion setFraktionMitglieder(Fraktion fraktion, Elements mitgliederElements) {
		
		Iterator<Element> mitgliederIt = mitgliederElements.iterator();
		List<Politiker> mitglieder = new ArrayList<Politiker>();
		while(mitgliederIt.hasNext()) {
			Element mitgliedRow = mitgliederIt.next();
			Elements nameEl = mitgliedRow.select("td[nowrap]");
			if(!nameEl.isEmpty()) {
				Politiker mitglied = new Politiker();
				mitglied.setName(nameEl.select("a").text());
				mitglied.setUri(this.baseUri + nameEl.select("a").attr("href"));
				Elements artEl = mitgliedRow.select("td.text1");
				if(!artEl.isEmpty()) {
					mitglied.setArt(artEl.text());
				} else {
					mitglied.setArt("");
				}
				mitglieder.add(mitglied);
			} 
		}
		fraktion.setMitglieder(mitglieder);
		return fraktion;
	}
	
	public static void wait (int k){
		long time0, time1;
		time0 = System.currentTimeMillis();
		do{
		time1 = System.currentTimeMillis();
		}
		while ((time1 - time0) < k);
	}
	
	public static void main(String[] args) {
		FraktionScraper fruck = new FraktionScraper("http://www.berlin.de");
		
		List<Fraktion> fraktionen = fruck.parseFraktionen("http://www.berlin.de/ba-friedrichshain-kreuzberg/bvv-online/fr010.asp", 20000);
		for(Fraktion f : fraktionen) {
			System.out.println(f.getName());
			System.out.println(f.getAdresse());
			System.out.println(f.getSprechzeiten());
			System.out.println(f.getEmail());
			System.out.println(f.getHomepage());
			System.out.println("Mitglieder:");
			for(Politiker p : f.getMitglieder()) {
				System.out.println(p.getName() + " " + p.getArt());
			}
		}
	}
	
}
