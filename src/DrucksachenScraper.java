import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class DrucksachenScraper {
	
	private String baseUri;
	private String drucksachenXmlUri;
	private List<Drucksache> drucksachen;
	private String outputFolder;
	private String outputEncoding;
	private String inputEncoding;
	
	public DrucksachenScraper(String baseUri, String outputFolder) {
		this.baseUri=baseUri;
		this.drucksachen = new ArrayList<Drucksache>();
		this.outputFolder = outputFolder;
		setInputEncoding("ISO-8859-1");
		setOutputEncoding("UTF-8");
		
	}
	
	public void init(String xmlUri) {
		this.drucksachenXmlUri = xmlUri;	
		try {
			System.out.println("Connecting to basedocument");
			Document doc = Jsoup.connect(xmlUri)
					.userAgent("Uni-Leipzig: ALLRIS Scraper")
					.timeout(60000)
					.get();	
			System.out.println("Parsing basedocument");
			Document xmlDoc = Jsoup.parse(doc.toString(), "", Parser.xmlParser());
			Elements druckElements = xmlDoc.select("element");
			Iterator<Element> druckIt = druckElements.iterator();
			while(druckIt.hasNext()) {
				Element dElement = druckIt.next();
				Drucksache drucksache = new Drucksache();
				String volfdnr = dElement.getElementsByTag("volfdnr").text();
				if(volfdnr != null) {
					drucksache.setVolfdnr(volfdnr);
				} else {
					continue;
				}
				drucksache.setAktenzeichen(dElement.getElementsByTag("voname").text());
				drucksache.setBetreff(dElement.getElementsByTag("vobetr").text());
				drucksache.setInitiator(dElement.getElementsByTag("atname").text());
				drucksache.setUri(this.baseUri+dElement.getElementsByTag("link_vo").text());
				drucksache.setStatus(dElement.getElementsByTag("voost").text());
				drucksache.setVerfasser(dElement.getElementsByTag("voverf").text());
				drucksache.setArt(dElement.getElementsByTag("vaname").text());
				this.drucksachen.add(drucksache);
			}
			System.out.println("Done.");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public List<String> scrapeDrucksachen(boolean writeToFile) {
		
		int numberTotal = drucksachen.size();
		if(numberTotal==0) {
			System.out.println("Konnte keine Drucksachen finden, bitte Konfiguration überprüfen.");
			return null;
		}
		int count = 0;	
		List<String> failedToParse = new ArrayList<String>();
		for(Drucksache druck : this.drucksachen) {
			count++;
			if(count>2857) {
				boolean success = this.parseDrucksache(druck,20000);
				if(!success) {
					wait(5000);
					success = this.parseDrucksache(druck,40000);
					if(!success) {
						failedToParse.add(druck.getUri());
						continue;
					}
				}
				if(writeToFile) {
					this.writeDruckObjectToFile(druck);
					System.out.println("Wrote object " + count + " of " + numberTotal + ".");
				}
				wait(1000);
				//if(count==5) break;
			}
		}
		return failedToParse;
		
	}
	
	private boolean parseDrucksache(Drucksache druck, int timeout) {
		
		boolean success = true;
		Connection.Response response = null;
		
		try {
			System.out.println("fu");
			response = Jsoup.connect(druck.getUri())
							.userAgent("Uni-Leipzig: ALLRIS Scraper")
							.timeout(timeout)
							.execute();
			
			System.out.println(response.statusCode());
			
			Document doc = response.parse();
			//dieser path soll zur Drucksache führen. Leider ist das html als Tabelle formatiert. Clusterfuck.
			Elements textElements = doc.select("table.risdeco tbody tr td div div p span");
			
			if(!textElements.isEmpty()) {
				Iterator<Element> textElIt = textElements.iterator();
				String text = "";
				
				while(textElIt.hasNext()) {
					Element textEl = textElIt.next();
					text+=textEl.text();
				}
				//String output = this.convertToOutputEncoding(text, this.inputEncoding, this.outputEncoding);
				druck.setText(text);
				
			} else {
				druck.setText("Dokument nicht vorhanden.");
			}
			
			Elements linkEl = doc.select("table.tk1 tbody tr td.me1 form[action*=vo021]");
			
			if(!linkEl.isEmpty()) {
				//uri bauen. kein kommentar...
				String beschlussUri = this.baseUri+linkEl.get(0).attr("action")+"?VOLFDNR="+druck.getVolfdnr()+"&options=16";
				druck.setBeschlussLink(beschlussUri);
			}
			
		} catch(HttpStatusException httpe) {
			if(httpe.getStatusCode()==500) {
				System.out.println("Document " + druck.getUri() + " does not exist");
			}
			success = false;
		} catch(SocketTimeoutException ste) {
			System.out.println(druck.getUri() + " timed out.");
			success = false;
		} catch(IOException ioe) {
			System.out.println(druck.getUri() + " could not be reached.");
			success = false;
		} 	
		return success;	
	}
	
	public void writeDruckObjectToFile(Drucksache druck) {
		ObjectOutputStream outputStream = null;
        
        try {
            
            //Construct the LineNumberReader object
        	FileOutputStream fileOut = new FileOutputStream(this.outputFolder+druck.getVolfdnr());
            outputStream = new ObjectOutputStream(fileOut);
            outputStream.writeObject(druck);
            outputStream.flush();
      
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the ObjectOutputStream
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	}
	
	public List<Drucksache> readDrucksachenFromFolder() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		List<Drucksache> druckList = new ArrayList<Drucksache>();
		
		try {
			File mdbFolder = new File(this.outputFolder);
			File[] druckFiles = mdbFolder.listFiles();
			if(druckFiles.length==0) {
				System.out.println("Keine Drucksachen gefunden, Konfiguration überprüfen.");
				return druckList;
			}
			
			for(File mdbFile : druckFiles) {
				fis = new FileInputStream(mdbFile);
				ois = new ObjectInputStream(fis);			
				Drucksache druck = null;
				while(fis.available()>0) {
					if((druck = (Drucksache) ois.readObject())!= null) {
						druckList.add(druck);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            //Close the ObjectOutputStream
            try {
                if (ois != null) {
                    ois.close();
                    fis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		
		return druckList;
		
	}
	
// I will care about this later as it is an edge case
//	
//	private String convertToOutputEncoding(String toConvert, String inputEncoding, String outputEncoding) {
//		String output = "";
//		char[] characters = toConvert.toCharArray();
//		for(int i = 0; i < characters.length; i++) {
//			if(Character.isISOControl(characters[i])) {
//				System.out.println((int)characters[i]);
//				try {
//					characters[i]=new String(Character.toString(characters[i]).getBytes(inputEncoding),outputEncoding).charAt(0);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		output = new String(characters);
//		return output;
//	}
	

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		if(baseUri.endsWith("/")) {
			this.baseUri = baseUri.substring(0, baseUri.length()-1);
		} else {
			this.baseUri = baseUri;
		}
	}
	
	public String getDrucksachenXmlUri() {
		return drucksachenXmlUri;
	}

	public void setDrucksachenXmlUri(String drucksachenXmlUri) {
		this.drucksachenXmlUri = drucksachenXmlUri;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public void setOutputEncoding(String encoding) {
		this.outputEncoding = encoding;
	}
	
	public String getInputEncoding() {
		return inputEncoding;
	}

	public void setInputEncoding(String encoding) {
		this.inputEncoding = encoding;
	}
	
	public List<Drucksache> getDrucksachen() {
		return drucksachen;
	}

	public void setDrucksachen(List<Drucksache> drucksachen) {
		this.drucksachen = drucksachen;
	}
	
	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
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
		DrucksachenScraper druck = new DrucksachenScraper("http://www.berlin.de","./drucksachen/");
		//druck.init("http://www.berlin.de/ba-friedrichshain-kreuzberg/bvv-online/vo040.asp?selfaction=ws&template=xyz");
		//List<String> failedUris = druck.scrapeDrucksachen(false);
		
		List<Drucksache> textDrucksachen = druck.readDrucksachenFromFolder();
		Collections.sort(textDrucksachen);
		
	}
	
}
