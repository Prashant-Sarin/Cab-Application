package com.sarindev.cabapp.giver;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sarindev.cabapp.*;
import com.sarindev.cabapp.R;
import com.sarindev.cabapp.taker.TakerMapActivity;

import java.util.List;
import java.util.Map;

public class GiverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {

    private static final String TAG = GiverMapActivity.class.getSimpleName();
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location lastLocation;
    LocationRequest mLocationRequest;
    String[] mLocationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_PERMISSION_CODE = 121;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Button logout_btn;
    private String takerId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sarindev.cabapp.R.layout.activity_giver_map);
        Log.d(TAG,"GiverMapActivity Called onCreate method");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        logout_btn=(Button)findViewById(R.id.giver_logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(GiverMapActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedTaker();

    }
    // For getting assigned customer or Cab Taker
    private void getAssignedTaker() {
        String giverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedTakerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Giver").child(giverId);
        // Event listener to check events of any customer assigned to this giver
        assignedTakerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("TakerRideId")){
                    Map<String,Object> takerMap= (Map<String, Object>) dataSnapshot.getValue();
                    if (takerMap.get("TakerRideId")!= null){
                        takerId = takerMap.get("TakerRideId").toString();
                        getAssignedTakerLocation();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedTakerLocation() {
        DatabaseReference takerLocationRef = FirebaseDatabase.getInstance().getReference().child("takerRequest").child(takerId).child("l");
        takerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat =0;
                    double locationLng =0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickUpLatLng = new LatLng(locationLat,locationLng);
                    mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("PickUp Location"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"onMapReady called");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mLocationPermissions,LOCATION_PERMISSION_CODE);
            }
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    // When user first come to this activity we try to connect Google services for location and map related work
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG,"buildGoogleApiClient called");
        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext()!=null) {
            lastLocation = location;
            Log.d(TAG, "location = " + location.toString());
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            // To avoid app crash when logged out
            if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("GiversAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("GiversWorking");
                //GeoFire is an open source library, used to query firebase database
                GeoFire geoFireAvailable = new GeoFire(refAvailable);
                GeoFire geoFireWorking = new GeoFire(refWorking);
                // Based on Availability w'll update firebase
                switch (takerId) {
                    case "":
                        geoFireWorking.removeLocation(user_id);
                        geoFireAvailable.setLocation(user_id, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        break;
                    default:
                        geoFireAvailable.removeLocation(user_id);
                        geoFireWorking.setLocation(user_id, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        break;
                }
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG,"onConnected called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //To check whether location settings are good to proceed or not.
        LocationSettingsRequest locationSettingsRequest;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        locationSettingsRequest= builder.build();
        /*
          Check if the device's location settings are adequate for the app's needs using the
          {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
         * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
         */
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,locationSettingsRequest);
        result.setResultCallback(this);
        startLocationUpdates();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_CODE: // if permission requested for ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1]== PackageManager.PERMISSION_GRANTED) ){
                    Toast.makeText(this,"Permisiions granted",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            // removing location from firebase
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("GiversAvailable");
            GeoFire geoFire = new GeoFire(reference);
            geoFire.removeLocation(user_id);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mLocationPermissions,LOCATION_PERMISSION_CODE);
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        Log.d(TAG,"status = "+status.getStatusMessage());
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(GiverMapActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }
}
