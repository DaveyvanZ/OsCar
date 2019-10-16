package com.dmnn.oscar;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

/**
 * Fragment home class word gebruikt om een map met de
 *
 * gebruikers adres,
 * alle hoofdbestuurder adressen(Rit)
 *
 * samen met de fragment_home.xml
 *
 * ------------------------------------
 *
 * Alle objecten worden boven aan gemaakt en in methods geinitializeerd
 * overeekomstig met wanneer ze geinitializeerd moeten worden
 *
 * @author : Nathaniel Veldkamp
 */

public class FragmentHome extends Fragment implements OnMapReadyCallback
{
    /** MedewerkerID: het unieke ID van de huidige medewerker die ingelogd" is. **/
    private static final String  MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private int medewerkerID;
    protected String gebruikerAdres;
    private GeoApiContext mGeoApiContext = null;

    Polyline polyline;

    GoogleMap mMap;
    MapView mMapView;
    View mView;
    Marker RideMarker, BedrijfMarker, RiderMarker, UserMarker;

    ArrayList<Marker> rideMarkers;
    ArrayList<Marker> riderMarkers;
    ArrayList<Marker> bedrijfMarker;

    Button registerBtn;
    Bundle data;

    Rit rit;

    public FragmentHome(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        /**
         * Views en andere design nodes worden geinitializeerd
         *
         * medewerker id word uit de bundle gehaald
         *
         * init google map word aangeroepen vor map initializering
         *
         */

        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = mView.findViewById(R.id.mapView);

        registerBtn = mView.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(e -> Register());

        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");
        data = new Bundle();
        data.putInt("medewerkerID", medewerkerID);

        initGoogleMap(savedInstanceState);

        return mView;
    }

    private void initGoogleMap(Bundle savedInstanceState){

        /**
         * map word geinitializeerd
         *
         * en de api key word gelinked samen met een build functie
         *
         */

        Bundle mapViewBundle = null;
        if(savedInstanceState != null){
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        if(mGeoApiContext == null){

            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();

        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        /**
         * de map markers worden in de on view created geinitializeerd
         *
         * een query voor de ingelogde medewerker word aangeroepen
         * en een query voor de hoofdbestuurder adressen
         */

        super.onViewCreated(view, savedInstanceState);

        rideMarkers = new ArrayList<>();
        riderMarkers = new ArrayList<>();
        bedrijfMarker = new ArrayList<>();

        DbConnectMedewerker();

        DbConnectRides();

    }

    private void DbConnectMedewerker(){

        // query voor de medewerker gegevens

        HashMap<String, String> dataMID = new HashMap<>();
        dataMID.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetSpecificMedewerker.php",
                dataMID, false, output -> { getGebruikerGegevens(output); });


    }

    private void DbConnectMedewerkersInRit(int medewerkerID){

        // query voor alle gebruiker in een rit, word gebruikt om alle rit tussen stoppen weer te geven

        Log.d("MYTAG", "DbConnectMedewerkersInRit: "+ medewerkerID);
        HashMap<String, String> dataRideIDs = new HashMap<>();
        dataRideIDs.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetSpecificMedewerker.php",
                dataRideIDs, true, output -> {getRidergegevens(output);});

    }

    private void getRidergegevens(String output) {

        /**
         * output van de medewerkersInRit query word als json array opgeroepen
         *
         * Het adres, naam , en achternaam word uit de database gehaald
         *
         * createRiderMarkers() functie word gebruikt met de db info om een marker op de map te kunnen zetten
         */

        try{

            JSONArray array = new JSONArray(output);

            for(int i = 0; i < array.length(); i++) {

                JSONObject o = (JSONObject) array.get(i);

                String adres = (String) o.get("Adres");
                String name = (String) o.get("Naam");
                String lastname = (String) o.get("Achternaam");
                String fullname = name + " " + lastname;

                Log.d("MYTAG", "getRidergegevens: RiderAdres: " + adres);
                Log.d("MYTAG", "getRidergegevens: RiderNaam: " + fullname);

                CreateRiderMarkers(adres, fullname);

            }

        }catch(JSONException e){
            e.printStackTrace();
        }

    }

    private void DbConnectRitInschrijving(String ritID){

        /**
         * query uitvoeren voor het vinden van alle rit gegevens van een specifieke rit
         */

        HashMap<String, String> dataRitID = new HashMap<>();
        dataRitID.put("ritID", ritID);
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetRitInschrijvingen.php",
                dataRitID, true, output -> {getRitInschrivingen(output);});

    }

    private void getRitInschrivingen(String output) {

        // output voor de rit gegevens, alle medewerkers die in een rit zijn worden uit de DB gehaald
        // de id's worden gebruikt voor de query uitvoerings functie

        try{

            JSONArray array = new JSONArray(output);

            for(int i = 0; i < array.length(); i++){

                JSONObject o = (JSONObject) array.get(i);
                String riders = (String) o.get("MedewerkerID");

                Log.d("MYTAG", "getRitInschrivingen: medewerkerID's " + riders);
                DbConnectMedewerkersInRit(Integer.parseInt(riders));

            }

        }catch(JSONException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void DbConnectRides(){

        // uitvoeren van een query om alle rit locaties te krijgen

        HashMap<String, String> dataMID = new HashMap<>();
        dataMID.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetAlleRittenOrderedByDay.php",
                dataMID, true, output -> {CreateRideMarker(output);});

    }

    private void getGebruikerGegevens(String output)
    {
        /**
         * functie om de ingelogde gebruiker gegevens op te halen
         *
         * de gebruikers adres, en de bedrijf waar hij/zij werkt word hier uit gehaald
         *
         * op een marker click:
         *
         * word gechekd of een rit marker is geclicked
         * word de ritID uit de marker gehaald,
         * word de DB query geexecute om het adres van het bedrijf te krijgen
         *
         * en dan worden alle rit markers behalve de geclickte marker van de map gehaald
         *
         */

        try
        {
            JSONArray array = new JSONArray(output);

            for(int i = 0; i < array.length(); i++)
            {

                JSONObject o =  (JSONObject) array.get(i);
                gebruikerAdres = (String) o.get("Adres");
                Log.d("MYTAG", "getGebruikerGegevens: Adres = " + gebruikerAdres);
                CreateUserMarker(gebruikerAdres);
                data.putString("GebruikerAdres", gebruikerAdres);

                String bedrijf = (String) o.get("Bedrijf");
                Log.d("MYTAG", "getGebruikerGegevens: Bedrijf = " + bedrijf);

                Geocoder gCoder = new Geocoder(this.getActivity());
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if(marker.getTitle().equals("Ride")){

                            String ritID = marker.getTag().toString();
                            DbConnectRitInschrijving(ritID);
                            DBconnectBed(bedrijf);
                            Log.d("MYTAG", "onMarkerClick: " + ritID);

                            for(Marker mark : rideMarkers){

                                if(!mark.getId().equals(marker.getId())){

                                    mark.remove();
                                }

                            }

                            LatLng markerLoc = marker.getPosition();
                            try {
                                ArrayList<Address> addresses = (ArrayList<Address>) gCoder.getFromLocation(markerLoc.latitude,
                                        markerLoc.longitude,
                                        1);

                                for(Address ad : addresses){

                                    Log.d("MYTAG", "onMarkerClick: " + ad.getAddressLine(0));

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }else if(marker.getTitle().equals("Rider")){

                            Toast.makeText(getContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();

                        }

                        return false;
                    }
                });

            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    private void DBconnectBed(String bedrijf){

        // query voor het krijgen van adres gegevens

        HashMap<String, String> data = new HashMap<>();
        data.put("bedrijfsnaam", bedrijf);
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetBedrijfAdres.php",
                data, false, output -> {CreateBedMarker(output);});

    }

    @Override
    public void onSaveInstanceState(Bundle ouState){
        super.onSaveInstanceState(ouState);

        Bundle mapViewBundle = ouState.getBundle(MAPVIEW_BUNDLE_KEY);
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            ouState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // map word hier aangemaakt met een override method
        // een on map click word gemaakt

        /**
         * de map word helemaal leeg gehaald
         *
         * en de gebruiker + alle ritten worden weer op de map gezet
         */

        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                mMap.clear();

                DbConnectMedewerker();
                DbConnectRides();


            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            /**
             * @param marker
             *
             * een info window on click word gemaakt
             * als de info window word geclicked:
             *
             * word een bundle gemaakt en word de rit id van de marker waar de infowindow vandaan komt er in gezet
             * de route fragment word dan gestart en word de bundle ook daar naartoe meegestuurd
             */

            @Override
            public void onInfoWindowClick(Marker marker) {

                if(marker.getTitle().equals("Ride")) {
                    Bundle data = new Bundle();
                    data.putInt("medewerkerID", medewerkerID);

                    String ritID = marker.getTag().toString();
                    if(ritID != null) {
                        HashMap d = new HashMap<>();
                        d.put("ritID", ritID);
                        DBConnection.getInstance().executeQuery(getActivity(), "GetSpecificRit.php", d, true,
                                output ->
                                {
                                    try {
                                        JSONArray array = new JSONArray(output);

                                        if (array.length() > 0)
                                        {
                                            JSONObject o = (JSONObject) array.get(0);

                                            String rid = (String) o.get("ID");
                                            String vertrekdatumString = (String) o.get("Vertrekdatum");
                                            java.sql.Date vertrekdatum = FragmentRoutes.dateStringToSQLDate(vertrekdatumString);
                                            String aankomsttijdString = (String) o.get("Aankomsttijd");
                                            Time aankomsttijd = FragmentRoutes.timeStringToSQLTime(aankomsttijdString);
                                            String vertrekplaats = (String) o.get("Vertrekplaats");

                                            String status = (String) o.get("Status");
                                            String hinderStatus = (String) o.get("HinderStatus");
                                            int vrijePlaatsen = Integer.parseInt((String) o.get("VrijePlaatsen"));

                                            rit = new Rit(rid, vertrekdatum, aankomsttijd, vertrekplaats, status, hinderStatus, vrijePlaatsen);
                                        }

                                        data.putSerializable("rit", rit);

                                        Fragment fragRit = new FragmentRit();
                                        fragRit.setArguments(data);
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
                                        fragTransaction.replace(R.id.frame_content, fragRit);
                                        fragTransaction.commit();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                }
            }
        });


    }

    private void CreateBedMarker(String bedAdres) {

        // functie voor het maken van een marker voor het bedrijf van de ingelogde medewerker

        Geocoder gCoder = new Geocoder(this.getActivity());
        try {
            //Log.d("TAG", "onMapReady: " + gebruikerAdres);
            ArrayList<Address> BedAdres = (ArrayList<Address>) gCoder.getFromLocationName(bedAdres, 10);

            for (Address add : BedAdres) {

                double userLat = add.getLatitude();
                double userLong = add.getLongitude();

                LatLng UserAd = new LatLng(userLat, userLong);
                BedrijfMarker = mMap.addMarker(new MarkerOptions().position(UserAd).title("Werk").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                bedrijfMarker.add(BedrijfMarker);

            }

        }catch(IOException e ){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void CreateUserMarker(String adres) {

        // functie voor het maken van een marker voor het thuis adres van de ingelogde gebruiker

        Geocoder gCoder = new Geocoder(this.getActivity());
        try {
            //Log.d("TAG", "onMapReady: " + gebruikerAdres);
            ArrayList<Address> userAdres = (ArrayList<Address>) gCoder.getFromLocationName(adres, 10);

            for (Address add : userAdres) {

                double userLat = add.getLatitude();
                double userLong = add.getLongitude();

                LatLng UserAd = new LatLng(userLat, userLong);
                UserMarker = mMap.addMarker(new MarkerOptions()
                        .position(UserAd)
                        .title("Thuis")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                CameraPosition cam = new CameraPosition.Builder()
                        .target(UserAd)
                        .zoom(13.5f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));



            }

        }catch(IOException e ){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void CreateRiderMarkers(String adres, String snippet) {

        // functie voor het maken van markers voor meeriders.
        // gebeurt alleen op een on marker click voor een rit marker

        Geocoder gCoder = new Geocoder((this.getActivity()));

        try{

            ArrayList<Address> RiderAdressen;

            RiderAdressen = (ArrayList<Address>) gCoder.getFromLocationName(adres, 10);

            for(Address address : RiderAdressen){

                double addLat = address.getLatitude();
                double addLong = address.getLongitude();

                LatLng RiderPos = new LatLng(addLat, addLong);

                RiderMarker = mMap.addMarker(new MarkerOptions()
                        .position(RiderPos)
                        .title("Rider")
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                RiderMarker.setTag(RiderPos);
                riderMarkers.add(RiderMarker);

                if(riderMarkers.size() >= 1){

                    calculateDirections();

                }

            }

        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void calculateDirections() {

        /**
         * wanneer alle nodige markers op de map zijn,
         *
         * - Rit
         * - Gebruiker
         * - Meerijders
         * - Bedrijf
         *
         * word er een route gecalculeerd tussen alle markers,
         *
         * met Rit locatie als start punt, gebruikers locatie en meerijders als tussenpunten, en bedrijfs locatie als eindpunt
         *
         * dit word gedaan met de google directions api
         *
         * wanneer er een result is voor de route word deze getekent op de map met addPolylinesToMap(result) functie
         */

        Log.d("MYTAG", "calculateDirections: called" + UserMarker.getPosition());

        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.origin( new com.google.maps.model.LatLng(RideMarker.getPosition().latitude, RideMarker.getPosition().longitude))
                .mode(TravelMode.DRIVING)
                .waypoints(new com.google.maps.model.LatLng(RiderMarker.getPosition().latitude, RiderMarker.getPosition().longitude)
                        , new com.google.maps.model.LatLng(UserMarker.getPosition().latitude, UserMarker.getPosition().longitude))
                .optimizeWaypoints(true)
                .destination(new com.google.maps.model.LatLng(BedrijfMarker.getPosition().latitude, BedrijfMarker.getPosition().longitude))
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {

                        Log.d("MYTAG", "onResult: startAdres: " + result.routes[0].legs[0].startAddress);
                        Log.d("MYTAG", "onResult: waypoints: " + result.routes[0].waypointOrder.toString());
                        Log.d("MYTAG", "onResult: EndAdres: " + result.routes[0].legs[0].endAddress);

                        addPolylinesToMap(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {

                        Log.d("MYTAG", "onFailure: Failed to get directions: " + e.getMessage());

                    }
                });

    }



    private void addPolylinesToMap(final DirectionsResult result){

        // functie voor het tekenen van een route op de map

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("MYTAG", "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d("MYTAG", "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                    polyline.setClickable(false);

                }
            }
        });
    }

    private void CreateRideMarker(String output) {

        /**
         * functie voor het maken van rit markers
         *
         * de gegevens van een rit word opgeroepen
         * en het adres,
         * aankomstijd,
         * en id word uit de database gehaald en word gebruikt om een marker op de map te zetten
         */

        Geocoder gCoder = new Geocoder(this.getActivity());
        Log.d("MYTAG", "CreateRideMarker: Called");
        try {

            JSONArray array = new JSONArray(output);
            ArrayList<Address> hoofdAdressen;

            for(int i = 0; i < array.length(); i++)
            {
                Log.d("MYTAG", "CreateRideMarker: for loop started");
                JSONObject o =  (JSONObject) array.get(i);
                String HfdAdres = (String) o.get("Vertrekplaats");
                String aankomst = (String) o.get("Aankomsttijd");
                String ritID = (String) o.get("ID");

                Log.d("MYTAG", "hoofdbestuurder adres: " + HfdAdres);

                hoofdAdressen = (ArrayList<Address>) gCoder.getFromLocationName(HfdAdres, 10);
                for (Address add : hoofdAdressen) {

                    double addLatitude = add.getLatitude();
                    double addLongitude = add.getLongitude();

                    LatLng bedLatLong = new LatLng(addLatitude, addLongitude);
                    RideMarker = mMap.addMarker(new MarkerOptions()
                            .position(bedLatLong)
                            .title("Ride")
                            .snippet("Aankomsttijd : " + aankomst)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                    RideMarker.setTag(ritID);
                    rideMarkers.add(RideMarker);

                }

            }

        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e ){
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void Register(){

        // button on click voor rit registreren knop word gedaan in de xml file:
        // fragment_home.xml

        Fragment fragAanmelding = new FragmentAanmelding();
        fragAanmelding.setArguments(data);
        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
        fragTransaction.replace(R.id.frame_content, fragAanmelding);
        fragTransaction.commit();

    }

}
