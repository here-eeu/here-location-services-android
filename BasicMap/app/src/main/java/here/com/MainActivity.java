package here.com;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Map map = null;

    private AndroidXMapFragment mapFragment = null;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    private void initialize() {
        setContentView(R.layout.activity_main);
        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "MapService");

        if (!success) {
            Toast.makeText(getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                    if (error == OnEngineInitListener.Error.NONE) {
                        map = mapFragment.getMap();
                        map.setCenter(new GeoCoordinate(55.750907459297785, 37.61693000793457, 0.0),
                                Map.Animation.NONE);

                        requestLayer ();
                    } else {
                        System.out.println("ERROR: Cannot initialize Map Fragment");
                    }
                }
            });
        }
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();

        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }

                initialize();
                break;
        }
    }

    public void requestLayer () {
        String layerUrl = "https://xyz.api.here.com/hub/spaces/sTXqiVDP/iterate?access_token=APs9JH8eRoitxV4FztpZKwA";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, layerUrl,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {

                        JSONObject jsonObject = null;

                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("features");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jo = jsonArray.getJSONObject(i);
                                JSONObject geometry = jo.getJSONObject("geometry");

                                JSONArray coordsArray = geometry.getJSONArray("coordinates");
                                double latitude = (Double) coordsArray.get(1);
                                double longitude = (Double) coordsArray.get(0);

                                MapMarker mapMarker = new MapMarker(new GeoCoordinate(latitude, longitude, 0));
                                map.addMapObject(mapMarker);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }

}
