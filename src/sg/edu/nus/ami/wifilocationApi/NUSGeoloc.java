package sg.edu.nus.ami.wifilocationApi;

import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Create NUSGeoloc java API for AP based location web service
 * @author qinfeng
 *
 */
public class NUSGeoloc {
	/**
	 * A private String member URL directs to the AP based location web service 
	 */
	private String url = "http://172.18.101.125:8080/wifilocation/wifilocation";
	
	
	/**
	 * get the location based on the detected Access Point with the strongest signal 
	 * @param mac the MAC address of the Access Point
	 * @return an APLocation which is a location class for a WIFI Access Point
	 */
	public Vector<APLocation> getLocationBasedOnAP(Vector<String> mac){
		int length = mac.size();
		Vector<Double> strength = new Vector<Double>(length);
		for ( int i=0;i<length;i++){
			strength.add(new Double(0.0));
		}
		
		return getLocationBasedOnAP(mac, strength);
	}
	
	/**
	 * get the location based on the detected Access Point with the strongest signal
	 * @param mac the MAC address of the Access Point
	 * @param strength a parameter for calculate the accuracy ( To be developed )
	 * @return an APLocation which is a location class for a WIFI Access Point
	 */

	public Vector<APLocation> getLocationBasedOnAP(Vector<String> mac, Vector<Double> strength){
		Vector<APLocation> apLocation = new Vector<APLocation>();
		
		RestClient client = new RestClient(url);
		int n = mac.size();
		client.AddParam("number", String.valueOf(n));
		
		for(int i=0;i<n;i++){
			client.AddParam("mac"+i, mac.get(i));
			client.AddParam("strength"+i, String.valueOf(strength.get(i)));
		}

		
		try {
			client.Execute(RequestMethod.GET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String response = client.getResponse();
		
		String building;
		String ap_name;
		String ap_location;
		double accuracy;
		double ap_lat;
		double ap_long;
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(response));
			
			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("response");
			// the xml structure: 
			// <response>
			//		<location>
			// 			this is only one node with 6 child leaf nodes
			Element element = (Element) nodes.item(0);
			NodeList node_location = element.getElementsByTagName("location");
			int length = node_location.getLength();
			for(int i=0;i<length;i++){
				Element e_location = (Element) node_location.item(i);
				if(e_location.getElementsByTagName("errormessage").item(0)==null){
					building = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("building").item(0));
					ap_name = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("ap_name").item(0));
					ap_location = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("ap_location").item(0));
					String str_accuracy = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("accuracy").item(0));
					if (str_accuracy.equals("null"))
						accuracy = 100.0;
					else
						accuracy = Double.valueOf(str_accuracy);
					String str_ap_lat = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("ap_lat").item(0));
					if (str_ap_lat.equals("null"))
						ap_lat = 0.0;
					else
						ap_lat = Double.valueOf(str_ap_lat);
					String str_ap_long = getCharacterDataFromElement((Element) e_location
							.getElementsByTagName("ap_long").item(0));
					if (str_ap_long.equals("null"))
						ap_long = 0.0;
					else
						ap_long = Double.valueOf(str_ap_long);
					
					apLocation.add(new APLocation(building,ap_name,ap_location,accuracy,ap_lat,ap_long));
				}
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return apLocation;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getCharacterDataFromElement(Element e){
		Node child = e.getFirstChild();
		if(child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}
}
