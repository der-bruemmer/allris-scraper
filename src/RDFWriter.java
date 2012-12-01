import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class RDFWriter {
	
	private List<Drucksache> drucksachen;
	private List<Fraktion> fraktionen;
	private String urlBase;
	
	public RDFWriter(List<Drucksache> drucksachen, List<Fraktion> fraktionen, String urlBase) {
		this.drucksachen = drucksachen;
		this.fraktionen = fraktionen;
		this.urlBase = urlBase.trim()+"/";
	}
	
	public void print(String format, String folder, Model jenaModel) {
		try {
			OutputStream out = new FileOutputStream(folder.trim()+"output.rdf");
			jenaModel.write(out);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Model getRdfModel() {
		
		
		Model allrisModel = ModelFactory.createDefaultModel();
		allrisModel.setNsPrefix( "dcterms", "http://purl.org/dc/terms/");
		allrisModel.setNsPrefix( "allris", this.urlBase );
		Property subject = allrisModel.createProperty("http://purl.org/dc/terms/subject");
		
		Property aktenzeichen = allrisModel.createProperty(this.urlBase+"aktenzeichen");
		aktenzeichen.addProperty(RDFS.label, allrisModel.createLiteral("Aktenzeichen","de"));
		aktenzeichen.addProperty(RDFS.comment, allrisModel.createLiteral("Das Aktenzeichen einer Drucksache.","de"));
		
		Property druckClass = allrisModel.createProperty(this.urlBase+"Drucksache");
		
		Property betreff = allrisModel.createProperty(this.urlBase+"betreff");
		betreff.addProperty(RDFS.label, allrisModel.createLiteral("Betreff","de"));
		betreff.addProperty(RDFS.comment, allrisModel.createLiteral("Der Betreff der Drucksache.","de"));
		
		Property status = allrisModel.createProperty(this.urlBase+"status");
		status.addProperty(RDFS.label, allrisModel.createLiteral("Status","de"));
		status.addProperty(RDFS.comment, allrisModel.createLiteral("Status der Drucksache, öffentlich, abgeschlossen, etc.","de"));
		
		Property druckArt = allrisModel.createProperty(this.urlBase+"artDerDrucksache");
		druckArt.addProperty(RDFS.label, allrisModel.createLiteral("Art der Drucksache","de"));
		druckArt.addProperty(RDFS.comment, allrisModel.createLiteral("Die Art der Drucksache kann eine mündliche oder schriftliche Anfrage sein, ein Antrag, eine Resolution oder ähnliches.","de"));
		
		Property initiator = allrisModel.createProperty(this.urlBase+"initiator");
		initiator.addProperty(RDFS.label, allrisModel.createLiteral("Initiator der Drucksache","de"));
		initiator.addProperty(RDFS.comment, allrisModel.createLiteral("Der Initiator der Drucksache ist meist eine Fraktion, manchmal auch eine Abteilung oder ähnliches.","de"));
		
		Property text = allrisModel.createProperty(this.urlBase+"text");
		text.addProperty(RDFS.label, allrisModel.createLiteral("Text der Drucksache","de"));
		text.addProperty(RDFS.comment, allrisModel.createLiteral("Der Volltext der Drucksache.","de"));
		
		Property verfasser = allrisModel.createProperty(this.urlBase+"verfasser");
		verfasser.addProperty(RDFS.label, allrisModel.createLiteral("VerfasserIn der Drucksache","de"));
		verfasser.addProperty(RDFS.comment, allrisModel.createLiteral("VerfasserIn der Drucksache.","de"));
		
		Property beschluss = allrisModel.createProperty(this.urlBase+"beschluss");
		beschluss.addProperty(RDFS.label, allrisModel.createLiteral("Beschluss zur Drucksache","de"));
		beschluss.addProperty(RDFS.comment, allrisModel.createLiteral("Link zum zugehörigen Beschluss. Autogeneriert: Falls die Drucksache keinen Beschluss hat, ist die Seite trotzdem verlinkt.","de"));
		
		for(Drucksache d : this.drucksachen) {

			Resource drucksacheResource=allrisModel.createResource(this.urlBase+"drucksache/"+d.getVolfdnr());
			drucksacheResource.addProperty(subject, allrisModel.createProperty(d.getUri()));
			if(d.getBetreff()!=null) {
				drucksacheResource.addProperty(RDFS.label, allrisModel.createLiteral(d.getBetreff(),"de"));
				drucksacheResource.addProperty(betreff, allrisModel.createLiteral(d.getBetreff(),"de"));
			}
			drucksacheResource.addProperty(RDF.type, druckClass);
			
			if(d.getStatus()!=null) 	drucksacheResource.addProperty(status, allrisModel.createLiteral(d.getStatus(),"de"));
			if(d.getArt()!=null)		drucksacheResource.addProperty(druckArt, allrisModel.createLiteral(d.getArt(),"de"));
			if(d.getInitiator()!=null) {
				drucksacheResource.addProperty(initiator, allrisModel.createLiteral(d.getInitiator(),"de"));
				String initiatorUri = this.getInitiatorUri(d);
				if(initiatorUri!=null) {
					drucksacheResource.addProperty(initiator, allrisModel.createProperty(initiatorUri));
				}
			}
			if(d.getText()!=null) {
				drucksacheResource.addProperty(text, allrisModel.createLiteral(d.getText(),"de"));
			}
			if(d.getVerfasser()!=null && !d.getVerfasser().isEmpty()) {
				drucksacheResource.addProperty(verfasser, allrisModel.createLiteral(d.getVerfasser(),"de"));
				String verfasserUri = this.getVerfasserUri(d.getVerfasser());
				if(verfasserUri!=null) {
					drucksacheResource.addProperty(verfasser, allrisModel.createProperty(verfasserUri));
				}
			}
			if(d.getBeschlussLink()!=null) {
				drucksacheResource.addProperty(beschluss, allrisModel.createProperty(d.getBeschlussLink()));
			}		
			
		}
		
		Property name = allrisModel.createProperty(this.urlBase+"name");
		name.addProperty(RDFS.label, allrisModel.createLiteral("Name der Fraktion oder Person","de"));
		name.addProperty(RDFS.comment, allrisModel.createLiteral("Der Name der Fraktio, Gruppe oder Person.","de"));
		
		Property fraktionClass = allrisModel.createProperty(this.urlBase+"Fraktion");
		
		Property adresse = allrisModel.createProperty(this.urlBase+"adresse");
		adresse.addProperty(RDFS.label, allrisModel.createLiteral("Adresse","de"));
		adresse.addProperty(RDFS.comment, allrisModel.createLiteral("Die Adresse bezeichnet einen physikalischen Ort, an dem das Subjekt zu erreichen ist.","de"));
		
		Property sprechzeiten = allrisModel.createProperty(this.urlBase+"sprechzeiten");
		sprechzeiten.addProperty(RDFS.label, allrisModel.createLiteral("Sprechzeiten","de"));
		sprechzeiten.addProperty(RDFS.comment, allrisModel.createLiteral("Die Sprechzeiten der Fraktion oder eines Politikers.","de"));
		
		Property email = allrisModel.createProperty(this.urlBase+"email");
		email.addProperty(RDFS.label, allrisModel.createLiteral("Email-Adresse","de"));
		email.addProperty(RDFS.comment, allrisModel.createLiteral("Die Email einer Fraktion oder eines Politikers.","de"));
		
		Property homepage = allrisModel.createProperty(this.urlBase+"homepage");
		homepage.addProperty(RDFS.label, allrisModel.createLiteral("Homepage","de"));
		homepage.addProperty(RDFS.comment, allrisModel.createLiteral("Die Homepage einer Fraktion oder eines Politikers.","de"));
		
		Property politikerClass = allrisModel.createProperty(this.urlBase+"Politiker");
		
		Property politikerArt = allrisModel.createProperty(this.urlBase+"fraktionsFunktion");
		politikerArt.addProperty(RDFS.label, allrisModel.createLiteral("Funktion in Fraktion","de"));
		politikerArt.addProperty(RDFS.comment, allrisModel.createLiteral("Die Funktion eines Politikers in einer Fraktion.","de"));
		
		for(Fraktion f : fraktionen) {
			Resource fraktionResource=allrisModel.createResource(this.urlBase+"fraktion/F"+f.getId());
			fraktionResource.addProperty(subject, allrisModel.createProperty(f.getUri()));
			if(f.getName()!=null) {
				fraktionResource.addProperty(RDFS.label, allrisModel.createLiteral(f.getName(),"de"));
				fraktionResource.addProperty(name, allrisModel.createLiteral(f.getName(),"de"));
			}
			fraktionResource.addProperty(RDF.type, fraktionClass);
			
			if(f.getAdresse()!=null) 	fraktionResource.addProperty(adresse, allrisModel.createLiteral(f.getAdresse(),"de"));
			if(f.getSprechzeiten()!=null)	fraktionResource.addProperty(sprechzeiten, allrisModel.createLiteral(f.getSprechzeiten(),"de"));
			if(f.getEmail()!=null)	fraktionResource.addProperty(email, allrisModel.createLiteral(f.getEmail(),"de"));
			if(f.getHomepage()!=null)	fraktionResource.addProperty(homepage, allrisModel.createLiteral(f.getHomepage(),"de"));
			
			List<Politiker> fraktionsMitglieder = f.getMitglieder();
			if(!fraktionsMitglieder.isEmpty()) {
				for(Politiker p : fraktionsMitglieder) {
					
					Resource politikerResource=allrisModel.createResource(this.getVerfasserUri(p.getName()));
					politikerResource.addProperty(subject, allrisModel.createProperty(p.getUri()));
					if(p.getName()!=null) {
						politikerResource.addProperty(RDFS.label, allrisModel.createLiteral(p.getName(),"de"));
						politikerResource.addProperty(name, allrisModel.createLiteral(p.getName(),"de"));
					}
					politikerResource.addProperty(RDF.type, politikerClass);
					
					if(p.getArt()!=null) 	politikerResource.addProperty(politikerArt, allrisModel.createLiteral(p.getArt(),"de"));
					
				}
			}
		}
		
		return allrisModel;
	}
	
	/**
	 * This matches the initator and gives me a uri for him. Don't blame me, I know its ugly.
	 * This would need some fuzzy matching or levenshtein fun
	 * @param Drucksache d
	 * @return Strung URL of Initiator
	 */
	private String getInitiatorUri(Drucksache d) {
		String url = null;
		String initiator = d.getInitiator();
		if(initiator.toLowerCase().contains("grüne")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("grüne")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("piraten")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("piraten")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("spd")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("spd")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("cdu")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("cdu")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("pds")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("pds")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("linke")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("linke")) {
					return f.getUri();
				}
			}
		} else if(initiator.toLowerCase().contains("fdp")) {
			for(Fraktion f : this.fraktionen) {
				if(f.getName().toLowerCase().contains("fdp")) {
					return f.getUri();
				}
			}
		}
		
		return url;
	}
	
	//most names are lastname, forename
	private String getVerfasserUri(String verfasserString) {
		String uri = null;
		if(verfasserString.contains(",")) {
			String[] nameParts = verfasserString.split(",");
			if(nameParts.length>2) {
				return null;
			}
			verfasserString = nameParts[1].trim()+"+"+nameParts[0].trim();
		}
		verfasserString = verfasserString.replaceAll(" ", "+");
		uri = this.urlBase+"person/"+verfasserString;
		return uri;
	}
}
