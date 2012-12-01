import java.util.ArrayList;
import java.util.List;


public class Fraktion {
	
	private String name;
	private String uri;
	private String sprechzeiten;
	private String adresse;
	private List<Politiker> mitglieder;
	private boolean isAktuell;
	private String homepage;
	private String email;
	private int id;
	
	public Fraktion() {
		this.mitglieder = new ArrayList<Politiker>();
		this.isAktuell = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSprechzeiten() {
		return sprechzeiten;
	}

	public void setSprechzeiten(String sprechzeiten) {
		this.sprechzeiten = sprechzeiten;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	
	public void appendAdresse(String adresse) {
		if(this.adresse==null || this.adresse.isEmpty()) {
			this.adresse=adresse;
		} else {
			this.adresse+=", " + adresse;
		}
	}

	public List<Politiker> getMitglieder() {
		return mitglieder;
	}

	public void setMitglieder(List<Politiker> mitglieder) {
		this.mitglieder = mitglieder;
	}

	public boolean isAktuell() {
		return isAktuell;
	}

	public void setAktuell(boolean isAktuell) {
		this.isAktuell = isAktuell;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
