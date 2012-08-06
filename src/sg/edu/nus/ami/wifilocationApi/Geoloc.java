package sg.edu.nus.ami.wifilocationApi;

import java.net.URI;

/** 
 * This is our location class based on our customized location schema from XMPP-0800 standard
 * @author qinfeng
 *
 */

public class Geoloc {
	final static String classname = "geoloc";
	final static String schemapath = "http://xmpp.org/schemas/geoloc.xsd";
	final static String namespace = "http://jabber.org/protocol/geoloc";
	
	/**
	 * The nation where the user is located
	 */
	String country;
	
	/**
	 * The ISO 3166 two-letter country code
	 */
	String countrycode;
	
	/**
	 * An administrative region of the nation, such as a state or province
	 */
	String region;
	
	/**
	 * A locality within the administrative region, such as a town or city
	 */
	String locality;
	
	/**
	 * A named area such as a campus or neighborhood
	 */
	String area;
	
	/**
	 * A thoroughfare within the locality, or a crossing of two thoroughfares
	 */
	String street;
	
	/**
	 * A specific building on a street or in an area
	 */
	String building;
	
	/**
	 * A particular floor in a building
	 */
	String floor;
	
	/**
	 * A particular room in a building
	 */
	String room;
	
	/**
	 * A code used for postal delivery
	 */
	String postalcode;

	/**
	 * Latitude in decimal degrees North
	 */
	double lat;
	
	/**
	 * Longitude in decimal degrees East
	 */
	double lon;
	
	/**
	 * Altitude in meters above or below sea level
	 */
	double alt;
	
	/**
	 * Horizontal GPS error in meters; this element obsoletes the <error/> element
	 */
	double accuracy;
	
	/**
	 * Horizontal GPS error in arc minutes; this element is deprecated in favor of <accuracy/>
	 */
	double error;	

	/**
	 * GPS bearing (direction in which the entity is heading to reach its next waypoint), 
	 * measured in decimal degrees relative to true north [1]
	 */
	double bearing;
	
	/**
	 * GPS datum [2]
	 */
	String datum;
	
	/**
	 * The speed at which the entity is moving, in meters per second
	 */
	double speed;
	
	/**
	 * A natural-language name for or description of the location
	 */
	String description;

	/**
	 * A catch-all element that captures any other information about the location
	 */
	String text;
	
	/**
	 * UTC timestamp specifying the moment when the reading was taken 
	 * (MUST conform to the DateTime profile of XMPP Date and Time Profiles [3])
	 */
	long timestamp;
	
	/**
	 * A URI or URL pointing to information about the location
	 */
	URI uri;
	
	/**
	 * This is an optional attribute for geoloc indicating the language in use
	 */
	String lang;
	
	/**
	 * The default class constructor setting the default language as "en" ( english )
	 */
	public Geoloc(){
		lang = "en";
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getError(){
		return error;
	}
	
	public void setError(double error){
		this.error = error;
	}
	
	public double getAlt() {
		return alt;
	}

	public void setAlt(double alt) {
		this.alt = alt;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public static String getClassname() {
		return classname;
	}
	
	public static String getSchemaPath() {
		return schemapath;
	}

	public static String getNameSpace() {
		return namespace;
	}
	
	/**
	 * override the toString() method to print the class in XML format
	 */
	public String toString(){
		String xmloutput = "";
			xmloutput = "<"+classname+" xmlns='"+namespace+"'"+" xml:lang='"+getLang()+"'"+">";
    		xmloutput +="<accuracy>"+getAccuracy()+"</accuracy>";
    		xmloutput +="<alt>"+getAlt()+"</alt>";
    		xmloutput +="<area>"+getArea()+"</area>";
    		xmloutput +="<bearing>"+getBearing()+"</bearing>";
    		xmloutput +="<building>"+getBuilding()+"</building>";
    		xmloutput +="<country>"+getCountry()+"</country>";
    		xmloutput +="<countrycode>"+getCountrycode()+"</countrycode>";
    		xmloutput +="<datum>"+getDatum()+"</datum>";
    		xmloutput +="<description>"+getDescription()+"</description>";
    		xmloutput +="<error>"+getError()+"</error>";
    		xmloutput +="<floor>"+getFloor()+"</floor>";
    		xmloutput +="<lat>"+getLat()+"</lat>";
    		xmloutput +="<locality>"+getLocality()+"</locality>";
    		xmloutput +="<lon>"+getLon()+"</lon>";
    		xmloutput +="<postalcode>"+getPostalcode()+"</postalcode>";
    		xmloutput +="<region>"+getRegion()+"</region>";
    		xmloutput +="<room>"+getRoom()+"</room>";
    		xmloutput +="<speed>"+getSpeed()+"</speed>";
    		xmloutput +="<street>"+getStreet()+"</street>";
    		xmloutput +="<text>"+getText()+"</text>";
    		xmloutput +="<timestamp>"+getTimestamp()+"</timestamp>";
    		xmloutput +="<uri>"+getUri()+"</uri>";
    		xmloutput +="</"+classname+">";
		return xmloutput;
	}
	
}
