import java.io.Serializable;


public class Drucksache implements Serializable, Comparable<Drucksache>{
	
	private String aktenzeichen;
	private String volfdnr;
	private String betreff;
	private String status;
	private String initiator;
	private String verfasser;
	private String art;
	private String text;
	private String beschlussLink;
	private String uri;
	
	public Drucksache() {
		
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getAktenzeichen() {
		return aktenzeichen;
	}

	public void setAktenzeichen(String aktenzeichen) {
		this.aktenzeichen = aktenzeichen;
	}

	public String getVolfdnr() {
		return volfdnr;
	}

	public void setVolfdnr(String volfdnr) {
		this.volfdnr = volfdnr;
	}

	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(String betreff) {
		this.betreff = betreff;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInitiator() {
		return initiator;
	}

	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}

	public String getVerfasser() {
		return verfasser;
	}

	public void setVerfasser(String verfasser) {
		this.verfasser = verfasser;
	}

	public String getArt() {
		return art;
	}

	public void setArt(String art) {
		this.art = art;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getBeschlussLink() {
		return beschlussLink;
	}

	public void setBeschlussLink(String beschlussLink) {
		this.beschlussLink = beschlussLink;
	}

	@Override
	public int compareTo(Drucksache o) {
		return o.getVolfdnr().compareTo(this.getVolfdnr());
	}
	
	
}
