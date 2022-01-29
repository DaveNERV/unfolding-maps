package module3;

import java.util.*;
import processing.core.*;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author David Murasov Dmitri
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	private static final long serialVersionUID = 1L;
	private static final boolean offline = false;
	public static final float THRESHOLD_MODERATE = 5;
	public static final float THRESHOLD_LIGHT = 4;
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	private UnfoldingMap map;
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	public void setup() {
		size(950, 600, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";
		}else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
		}
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);
	    List<Marker> markers = new ArrayList<>();
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		for(PointFeature pointF : earthquakes){
			Marker mk = createMarker(pointF);
			markers.add(mk);
		}
	    map.addMarkers(markers);
	}

/* TODO (Step 4): Add code to this method so that it adds the proper*/
	private SimplePointMarker createMarker(PointFeature feature) {
		SimplePointMarker marker = new SimplePointMarker(feature.getLocation());
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());
	    int yellow = color(255, 255, 0);

		// TODO (Step 4): Add code below to style the marker's size and color
		if(mag < THRESHOLD_LIGHT){
			marker.setColor(color(0, 0, 255));
			marker.setRadius(5);
		}else if(THRESHOLD_LIGHT <= mag && mag < THRESHOLD_MODERATE){
			marker.setColor(yellow);
			marker.setRadius(10);
		}
		else{
			marker.setColor(color(255, 0, 0));
			marker.setRadius(17);
		}
	    return marker;
	}
	
	public void draw(){
	    background(10);
	    map.draw();
	    addKey();
	}

	// TODO: Implement this method to draw the key
	private void addKey(){
		fill(137, 225, 0);
		rect(25, 40, 170, 270);
		fill(0, 0 ,0);
		textSize(15);
		text("Earthquake Key", 45, 65);
		text("5.0+ Magnitude", 70, 125);
		text("4.0+ Magnitude", 70, 185);
		text("Below 4.0", 70, 245);
		fill(255, 0, 0);
		ellipse(50, 120, 25, 25);
		fill(255, 255, 0);
		ellipse(50, 178, 20, 20);
		fill(0, 0, 255);
		ellipse(50, 238, 10, 10);
	}
}
