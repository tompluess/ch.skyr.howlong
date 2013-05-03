package ch.skyr.howlong.client.map;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.layer.OSM;

import ch.skyr.howlong.client.UiLogger;

import com.googlecode.gwtphonegap.client.geolocation.Coordinates;

public class MapController {
    private final UiLogger uiLogger;
    private MapWidget mapWidget;

    public MapController(UiLogger uiLogger) {
        this.uiLogger = uiLogger;
    }

    public MapWidget getMapWidget() {
        if (mapWidget == null) {
            createMapWidget();
        }
        return mapWidget;
    }

    public void createMapWidget() {

        MapOptions defaultMapOptions = new MapOptions();
        // : 960 x 640
        mapWidget = new MapWidget("100%", "500", defaultMapOptions);

        OSM osm_2 = OSM.Mapnik("Mapnik"); // Label for menu 'LayerSwitcher'
        osm_2.setIsBaseLayer(true);

        OSM osm_3 = OSM.CycleMap("CycleMap");
        osm_3.setIsBaseLayer(true);

        // OSM osm_4 = OSM.Maplint("Maplint");
        // osm_4.setIsBaseLayer(true);

        Map map = mapWidget.getMap();
        map.addLayer(osm_2);
        map.addLayer(osm_3);
        // map.addLayer(osm_4);
        map.addControl(new LayerSwitcher());
        map.addControl(new MousePosition());

        // map.setCenter(new LonLat(6.95, 50.94), 12); // Warning: In the case
        // of OSM-Layers the method 'setCenter()' uses Gauss-Krueger
        // coordinates,
        // thus we have to transform normal latitude/longitude values into this
        // projection first:
        LonLat lonLat = new LonLat(6.95, 50.94); // (6.95, 50.94) --> (773670.4,
                                                 // 6610687.2)
        lonLat.transform("EPSG:4326", "EPSG:900913"); //
        map.setCenter(lonLat, 12); // see
                                   // http://docs.openlayers.org/library/spherical_mercator.html
    }

    public void showCoordinates(Coordinates coordinates) {
        final String coordianateDesc = "(" + coordinates.getLongitude() + ", " + coordinates.getLatitude() + ")";
        LonLat pos = new LonLat(coordinates.getLongitude(), coordinates.getLatitude());
        pos.transform("EPSG:4326", "EPSG:900913");
        getMapWidget().getMap().panTo(pos);
        uiLogger.logMessageUI("panTo position: " + coordianateDesc);
    }
}
