package com.sarindev.cabapp.giver;

import android.Manifest;
import android.content.Intent;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sarindev.cabapp.*;
import com.sarindev.cabapp.R;

public class GiverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = GiverMapActivity.class.getSimpleName();
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location lastLocation;
    LocationRequest mLocationRequest;
    String[] mLocationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_PERMISSION_CODE = 121;
    Button logout_btn;

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
        Log.d(TAG,"onLocationChanged called");
        lastLocation =location;
        Log.d(TAG,"location = "+location.toString());
        LatLng latLng=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // saving location to firebase
        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        //GeoFire is an open source library, used to query firebase database
        GeoFire geoFire=new GeoFire(reference);
        geoFire.setLocation(user_id,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG,"onConnected called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mLocationPermissions,LOCATION_PERMISSION_CODE);
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
        // removing location from firebase
        String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFire=new GeoFire(reference);
        geoFire.removeLocation(user_id);
    }
}
