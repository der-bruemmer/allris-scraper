
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;


public class AllrisScraper {

	private String baseUrl = null;
	private String allrisUrl = null;
	private boolean scrape;
	private String scrapeStartDate = null;
	private String cacheFolder = null;
	private int timeout = 0;
	private String outputFormat = null;
	private String outputFolder = null;
	private String outputUrlBase = null;

	
	public AllrisScraper(String baseUrl, String allrisUrl, boolean scrape, String scrapeStartDate, String cacheFolder, int timeout, String outputFormat, String outputFolder, String outputUrlBase) {
		this.baseUrl = formatUrl(baseUrl);
		this.allrisUrl = formatUrl(allrisUrl);
		this.scrape = scrape;
		this.scrapeStartDate = scrapeStartDate;
		this.cacheFolder = cacheFolder;
		this.timeout = timeout;
		this.outputFormat = outputFormat;
		this.outputFolder = outputFolder;
		this.outputUrlBase = formatUrl(outputUrlBase);
		
		List<Drucksache> drucksachen = new ArrayList<Drucksache>();
		System.out.println("Hole Drucksachen.");
		DrucksachenScraper druckScraper = new DrucksachenScraper(this.baseUrl,this.cacheFolder);
		if(scrape) {
			druckScraper.init(this.baseUrl+"/"+this.allrisUrl+"/vo040.asp?selfaction=ws&template=xyz");
			List<String> failedUris = druckScraper.scrapeDrucksachen(true);
			if(failedUris.size()>0) {
				System.out.println("Failed to parse " + failedUris.size() + " URLS:");
				for(String failed : failedUris) {
					System.out.println(failed);
				}
			}
			drucksachen = druckScraper.getDrucksachen();
		} else {
			System.out.println("Lese Drucksachen aus " + this.cacheFolder);
			drucksachen = druckScraper.readDrucksachenFromFolder();
		}
		
		List<Fraktion> fraktionen = new ArrayList<Fraktion>();
		System.out.println("Scrape Fraktionen.");
		FraktionScraper fruckScraper = new FraktionScraper(this.baseUrl);
		fraktionen = fruckScraper.parseFraktionen(this.baseUrl+"/"+this.allrisUrl+"/fr010.asp", this.timeout);
		System.out.println("Writer erstellt Datenmodell.");
		RDFWriter writer = new RDFWriter(drucksachen, fraktionen, outputUrlBase);
		Model rdfModel = writer.getRdfModel();
		System.out.println("Schreibe Datenmodell in "+this.outputFolder);
		writer.print(this.outputFormat, this.outputFolder, rdfModel);
		
	}
	
	private String formatUrl(String url) {
		String cleanUrl = null;
		if(!url.startsWith("/") && !url.endsWith("/")) {
			return url;
		} else if(!url.startsWith("/") && url.endsWith("/")) {
			cleanUrl = url.substring(0, url.length()-1);
		} else if(url.startsWith("/") && !url.endsWith("/")) {
			cleanUrl = url.substring(1);
		} else {
			cleanUrl = url.substring(1, url.length()-1);
		}
		return cleanUrl;
	}
	
	public static void main(String[] args) {

		String baseUrl = null;
		String allrisUrl = null;
		boolean scrape = false;
		String scrapeStartDate = null;
		String cacheFolder = null;
		int timeout = 0;
		String outputFormat = null;
		String outputFolder = null;
		String outputUrlBase = null;
		
		Properties properties = null;
		BufferedInputStream stream = null;
		try {

			properties = new Properties();
			stream = new BufferedInputStream(new FileInputStream(
					"config/scraper.properties"));
			properties.load(stream);
			stream.close();

			baseUrl = properties.getProperty(IConstants.BASE_URL);
			allrisUrl = properties.getProperty(IConstants.ALLRIS_URL);
			scrapeStartDate = properties.getProperty(IConstants.SCRAPE_INTERVAL);
			cacheFolder = properties.getProperty(IConstants.CACHE_FOLDER);
			timeout = Integer.valueOf(properties.getProperty(IConstants.TIMEOUT));
			outputFormat = properties.getProperty(IConstants.OUTPUT_FORMAT);
			outputFolder = properties.getProperty(IConstants.OUTPUT_FOLDER);
			outputUrlBase = properties.getProperty(IConstants.RDF_URL_BASE);
			
			if(properties.getProperty(IConstants.SCRAPE).equals("true") || properties.getProperty(IConstants.SCRAPE).equals("yes")) {
				scrape = true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		AllrisScraper scraper = new AllrisScraper(baseUrl,allrisUrl,scrape,scrapeStartDate,cacheFolder,timeout,outputFormat,outputFolder,outputUrlBase);
	}
}
