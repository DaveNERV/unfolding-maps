package module4;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author David Murashov
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	private static final long serialVersionUID = 1L;
	private static final boolean offline = false;

	public static String mbTilesString = "blankLight-1-3.mbtiles";
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	private UnfoldingMap map;
	private List<Marker> cityMarkers;
	private List<Marker> quakeMarkers;
	private List<Marker> countryMarkers;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		} else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
		}

		MapUtils.createDefaultEventDispatcher(this, map);
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<>();
	    
	    for(PointFeature feature : earthquakes) {
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  } else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    printQuakes();
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	}

	public void draw() {
		background(0);
		map.draw();
		addKey();
	}

	// TODO: Update this method as appropriate
	private void addKey() {
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		text("City Marker", 75, 97);
		text("Land Quake", 75, 120);
		text("Ocean Quake", 75, 145);
		text("Size - Magnitude", 50, 165);

		fill(90, 0, 0);
		triangle(50, 103, 57, 91, 63, 103);
		fill(255, 255, 255);
		ellipse(56, 120, 16, 16);
		rect(49, 137, 15, 15);

		//TODO: НЕ доделал кружочки

	}

	private boolean isLand(PointFeature earthquake) {
		for (Marker m : countryMarkers) {
			if(isInCountry(earthquake, m)){
				return true;
			}
		}
		return false;
	}

	private void printQuakes() {
		// TODO: Implement this method
		String name = null;
		EarthquakeMarker em;
		int totalQuakes = quakeMarkers.size();
		for (Marker cm : countryMarkers) {
			int quakeCounter = 0;
			name = (String)cm.getProperty("name");
			for (Marker m : quakeMarkers) {
				em = (EarthquakeMarker)m;
				if(em.isOnLand() == true){
					if(em.getStringProperty("country").equals(name)){
						quakeCounter++;
					}
				}
			}
			if(quakeCounter > 0){
				System.out.println(name + ": " + quakeCounter);
				totalQuakes -= quakeCounter;
			}
		}
		System.out.println("OCEAN QUAKES: " + totalQuakes);
	}

	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();
		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		}
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}
}
