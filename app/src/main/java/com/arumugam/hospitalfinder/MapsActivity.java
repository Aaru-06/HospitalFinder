package com.arumugam.hospitalfinder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Location location;
    private MarkerOptions currmarker;
    private ArrayList<HospitalDetails> results;
    private Button searchbutton;
    private ArrayList<MarkerOptions> m;
    private Spinner sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchbutton = (Button)findViewById(R.id.search);

        sp = (Spinner) findViewById(R.id.category);
        String[] options = {"Hospital","Pharmacy"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,android.R.layout.simple_spinner_item,options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        checkingPermissions();

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            location = mMap.getMyLocation();
                            mMap.clear();

                            if(location==null)
                                return;
                            currmarker = new MarkerOptions();
                            LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
                            currmarker.position(latlng);
                            currmarker.title("My Location");
                            currmarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            currmarker.snippet("current location");
                            mMap.addMarker(currmarker);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,15.0f));

                            results = ApiQuery.ping(location,sp.getSelectedItem().toString().toLowerCase());

                            if(results==null)
                            {
                                Toast.makeText(getApplicationContext(),"Result null",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if(results.size()==0)
                                Toast.makeText(getApplicationContext(),"Please try again in few moments",Toast.LENGTH_SHORT).show();

                            m=new ArrayList();

                            for(int i=0;i<results.size();i++)
                            {
                                m.add(new MarkerOptions().position(new LatLng(results.get(i).getLocationlatlng()[0],results.get(i).getLocationlatlng()[1])));
                                m.get(i).snippet(i+"");
                            }

                            if(m==null)
                                return;

                            for(int i=0;i<m.size();i++)
                                mMap.addMarker(m.get(i));

                        }
                    });
                }
                catch(Exception e)
                {
                    Log.d("searchbutton",e.toString());
                }

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getSnippet().equals("current location"))
                {
                    Toast.makeText(getApplicationContext(),"My Location",Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent intent = new Intent(getApplicationContext(),HospitalView.class);
                intent.putExtra("obj",results.get(Integer.parseInt(marker.getSnippet())));
                Location l = mMap.getMyLocation();
                StringBuilder sb = new StringBuilder();
                sb.append(l.getLatitude()+","+l.getLongitude());
                String loc = sb.toString();
                intent.putExtra("loc",loc);
                startActivity(intent);
                return true;
            }
        });
    }
    private boolean checkingPermissions()
    {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this,"Location Services required",Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
            }
            //return false;
        }
        else{
            mMap.setMyLocationEnabled(true);
        }

        if(!checkPlayServices())
        {
            Toast.makeText(this,"Please install Google Play Services.!",Toast.LENGTH_SHORT).show();
        }

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(!lm.isProviderEnabled("gps"))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(this,"Cannot provide the location services",Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000);
            } else {
                finish();
            }

            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
