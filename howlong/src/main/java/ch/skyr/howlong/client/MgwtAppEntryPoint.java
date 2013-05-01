/*
 * Copyright 2010 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.skyr.howlong.client;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.layer.OSM;

import ch.skyr.howlong.client.activities.HomePlace;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.PhoneGapAvailableEvent;
import com.googlecode.gwtphonegap.client.PhoneGapAvailableHandler;
import com.googlecode.gwtphonegap.client.PhoneGapTimeoutEvent;
import com.googlecode.gwtphonegap.client.PhoneGapTimeoutHandler;
import com.googlecode.gwtphonegap.client.geolocation.GeolocationCallback;
import com.googlecode.gwtphonegap.client.geolocation.GeolocationOptions;
import com.googlecode.gwtphonegap.client.geolocation.Position;
import com.googlecode.gwtphonegap.client.geolocation.PositionError;
import com.googlecode.mgwt.mvp.client.AnimatableDisplay;
import com.googlecode.mgwt.mvp.client.AnimatingActivityManager;
import com.googlecode.mgwt.mvp.client.Animation;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.animation.AnimationHelper;
import com.googlecode.mgwt.ui.client.widget.Button;
import com.googlecode.mgwt.ui.client.widget.LayoutPanel;

/**
 * @author Daniel Kurka
 * 
 */
public class MgwtAppEntryPoint implements EntryPoint {

    private TextArea logTextArea;
    private final int LIMIT_LOG = 1000;

    private void start() {

        // set viewport and other settings for mobile
        MGWT.applySettings(MGWTSettings.getAppSetting());

        final ClientFactory clientFactory = new ClientFactoryImpl();

        // Start PlaceHistoryHandler with our PlaceHistoryMapper
        AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        historyHandler.register(clientFactory.getPlaceController(), clientFactory.getEventBus(), new HomePlace());

        createPhoneDisplay(clientFactory);

        historyHandler.handleCurrentHistory();

    }

    private void createPhoneDisplay(ClientFactory clientFactory) {
        AnimatableDisplay display = GWT.create(AnimatableDisplay.class);

        PhoneActivityMapper appActivityMapper = new PhoneActivityMapper(clientFactory);

        PhoneAnimationMapper appAnimationMapper = new PhoneAnimationMapper();

        AnimatingActivityManager activityManager = new AnimatingActivityManager(appActivityMapper, appAnimationMapper,
                clientFactory.getEventBus());

        activityManager.setDisplay(display);

        RootPanel.get().add(display);

    }

    // public void onModuleLoad() {
    public void onModuleLoad_singleButton() {
        // set viewport and other settings for mobile
        MGWT.applySettings(MGWTSettings.getAppSetting());

        // build animation helper and attach it
        AnimationHelper animationHelper = new AnimationHelper();
        RootPanel.get().add(animationHelper);

        // build some UI
        LayoutPanel layoutPanel = new LayoutPanel();
        Button button = new Button("Hello mgwt");
        layoutPanel.add(button);

        // animate
        animationHelper.goTo(layoutPanel, Animation.SLIDE);

    }

    @Override
    public void onModuleLoad() {

        // build some UI
        LayoutPanel layoutPanel = new LayoutPanel();
        logTextArea = new TextArea();
        logTextArea.setVisibleLines(4);
        logTextArea.setWidth("100%");
        layoutPanel.add(logTextArea);
        addMap(layoutPanel);
        RootPanel.get().add(layoutPanel);

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                Window.alert("uncaught: " + e.getLocalizedMessage());
                Window.alert(e.getMessage());
                // log.log(Level.SEVERE, "uncaught exception", e);
                handleExceptionUI(e);
            }
        });

        final PhoneGap phoneGap = GWT.create(PhoneGap.class);

        phoneGap.addHandler(new PhoneGapAvailableHandler() {

            @Override
            public void onPhoneGapAvailable(PhoneGapAvailableEvent event) {
                // startShowCase(phoneGap);

                logMessageUI("phone gap event: " + event.getClass() + ": " + event);

                GeolocationOptions options = new GeolocationOptions();
                options.setMaximumAge(1000);
                options.setEnableHighAccuracy(true);

                GeolocationCallback callback = new GeolocationCallback() {

                    @Override
                    public void onSuccess(Position position) {
                        // TODO Auto-generated method stub

                        String message = "position:\n" + position;
                        logMessageUI(message);

                    }

                    @Override
                    public void onFailure(PositionError error) {
                        // TODO Auto-generated method stub

                    }
                };
                phoneGap.getGeolocation().getCurrentPosition(callback, options);

            }
        });

        phoneGap.addHandler(new PhoneGapTimeoutHandler() {

            @Override
            public void onPhoneGapTimeout(PhoneGapTimeoutEvent event) {
                Window.alert("can not load phonegap (timeout)");

            }
        });

        phoneGap.initializePhoneGap();

        new Timer() {
            @Override
            public void run() {
                start();

            }
        }.schedule(1);
    }

    private void addMap(final LayoutPanel layoutPanel) {
        // build some UI
        MapOptions defaultMapOptions = new MapOptions();
        MapWidget mapWidget = new MapWidget("800px", "600px", defaultMapOptions);

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

        layoutPanel.add(mapWidget);
        logMessageUI("map added.");
    }

    // using google maps
    // private void addMap(final LayoutPanel layoutPanel) {
    // // build some UI
    // Maps.loadMapsApi("", "2", false, new Runnable() {
    // @Override
    // public void run() {
    // // Open a map centered on Satu Mare, Romania
    // LatLng SatuMareCity = LatLng.newInstance(47.792091, 22.885189);
    //
    // final MapWidget map = new MapWidget(SatuMareCity, 2);
    // map.setSize("100%", "100%");
    // // Add some controls for the zoom level
    // map.addControl(new LargeMapControl());
    //
    // // Add a marker
    // map.addOverlay(new Marker(SatuMareCity));
    //
    // // Add an info window to highlight a point of interest
    // map.getInfoWindow()
    // .open(map.getCenter(), new
    // InfoWindowContent("The River Somes is here in Satu Mare"));
    //
    // final DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
    // dock.addNorth(map, 500);
    //
    // // Add the map to the HTML host page
    // layoutPanel.add(dock);
    // }
    // });
    // }

    private void logMessageUI(String newMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(newMessage);
        sb.append("\n");
        String oldMessage = logTextArea.getText();
        if (oldMessage.length() > LIMIT_LOG) {
            sb.append(oldMessage.substring(0, 1000));
        } else {
            sb.append(oldMessage);
        }
        logTextArea.setText(sb.toString());
    }

    private void handleExceptionUI(Throwable e) {
        System.out.println("uncaught exception");
        e.printStackTrace();
        String message = "uncaught: " + e.getLocalizedMessage() + "\n" + e.getMessage();
        logMessageUI(message);
    }
}
