package here.com;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.ArCoreApk;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.SupportMapFragment;

import java.io.File;
import java.lang.ref.WeakReference;


public class MapFragmentView extends AppCompatActivity {

    //change intent activity for unsuported AR core devices
    private  boolean supportArCore = false;

    public AppCompatActivity m_activity;

    private SharedPreferences attendaceCheck;

    public SupportMapFragment m_mapFragment;
    private Map m_map;

    private FloatingActionButton geolocationBtn;
    private FloatingActionButton zoomInBtn;
    private FloatingActionButton zoomOutBtn;
    private FloatingActionButton infoBtn;

    private Button taskInfoBtn;
    private Button clearDBBtn;
    private ImageView teleportBtn;



    private PositioningManager posManager = null;
    private Boolean paused = false;
    private Boolean initialized = false;

    MapOnGestureListener mapOnGestureListener;
    MapGesture.OnGestureListener listener;

    TaskManager taskManager;

    SQLiteDatabase db;
    DBHelper dbHelper;

    private PositioningManager.OnPositionChangedListener positionListener = new PositioningManager.OnPositionChangedListener() {


        public void onPositionUpdated(PositioningManager.LocationMethod method,
                                      GeoPosition position, boolean isMapMatched) {

            if (!paused) {

            }

            if (!initialized) {

                initialized = true;

                m_map.setCenter(posManager.getPosition().getCoordinate(),
                        Map.Animation.BOW);
            }

            try{

                GeoCoordinate currentPosition = new GeoCoordinate(position.getCoordinate());

                GeoPolygon checkPolygon = taskManager.getCurrentGeozone();

                if(checkPolygon.contains(currentPosition)){
                    teleportBtn = m_activity.findViewById(R.id.fab);
                    teleportBtn.setVisibility(View.VISIBLE);
                }

            }catch(NullPointerException err){
                System.out.print(err);
            }

        }

        public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                         PositioningManager.LocationStatus status) {
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if (posManager != null) {
            posManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    @Override
    public void onPause() {
        if (posManager != null) {
            posManager.stop();
        }
        super.onPause();
        paused = true;
    }

    @Override
    public void onDestroy() {
        if (posManager != null) {
            posManager.removeListener(
                    positionListener);
        }
        m_map = null;
        super.onDestroy();
    }


    public MapFragmentView(AppCompatActivity activity) {
        m_activity = activity;

        initDatabase();
        initMapFragment ();
        checkArCore();


    }

    private SupportMapFragment getMapFragment () {
        return (SupportMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void initDatabase () {
        dbHelper = new DBHelper(m_activity);
        db = dbHelper.getReadableDatabase();
    }

    private void initMapFragment () {
        m_mapFragment = getMapFragment();

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                m_activity.getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "MapService");


        if(!success) {
            Toast.makeText(m_activity.getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else {
            m_mapFragment.init(error -> {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    m_map = m_mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    m_map.setCenter(new GeoCoordinate(55.750907459297785, 37.61693000793457, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    m_map.setZoomLevel((m_map.getMaxZoomLevel() + m_map.getMinZoomLevel()) / 2);


                    // Initialize and start geolocation service
                    posManager = PositioningManager.getInstance();

                    // Add geolocation listener to manager
                    posManager.addListener(new WeakReference<>(positionListener));

                    // Start listening geolocation
                    posManager.start(
                            PositioningManager.LocationMethod.GPS_NETWORK);

                    // Set geolocation idicator visible
                    m_map.getPositionIndicator().setVisible(true);
                    m_map.getPositionIndicator().setAccuracyIndicatorVisible(true);

                    // Initialize task manager
                    taskManager = new TaskManager(m_activity, m_map);


                    // Handle map events
//                    m_mapFragment.getMapGesture().addOnGestureListener(new MapOnGestureListener(m_activity, m_map),0, false);

                    checkFirstAttendance ();
                    initControlBtns();

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            });
        }

    }

    private void initControlBtns() {
        geolocationBtn = m_activity.findViewById(R.id.geolocationBtn);
        zoomInBtn = m_activity.findViewById(R.id.zoomInBtn);
        zoomOutBtn = m_activity.findViewById(R.id.zoomOutBtn);
        taskInfoBtn = m_activity.findViewById(R.id.task_info);
        clearDBBtn = m_activity.findViewById(R.id.clearDB);
        teleportBtn = m_activity.findViewById(R.id.fab);
        infoBtn = m_activity.findViewById(R.id.info);
//        confirmBtn = m_activity.findViewById(R.id.confirmBtn);

        taskInfoBtn.setOnClickListener(v -> {
            taskManager.openCurrentTaskDescription();
        });

        geolocationBtn.setOnClickListener(v -> {
            try{

                if(posManager.hasValidPosition()){
                    m_map.setCenter(posManager.getPosition().getCoordinate(),
                            Map.Animation.LINEAR);
                    m_map.setZoomLevel(17, Map.Animation.BOW);
                    m_map.setTilt(0, Map.Animation.BOW);
                    m_map.setOrientation(0, Map.Animation.BOW);
                };
            }catch(NullPointerException error) {
                Toast.makeText(getApplicationContext(), "Идет поиск местоположения", Toast.LENGTH_SHORT).show();
                System.out.print("Search in process");
            }
        });

        zoomInBtn.setOnClickListener(v ->{
            double zoomLevel = m_map.getZoomLevel();
            m_map.setCenter(posManager.getPosition().getCoordinate(),
                    Map.Animation.BOW);
            m_map.setZoomLevel( zoomLevel + 1, Map.Animation.LINEAR);
        });

        zoomOutBtn.setOnClickListener(v -> {
            double zoomLevel = m_map.getZoomLevel();
            m_map.setCenter(posManager.getPosition().getCoordinate(),
                    Map.Animation.BOW);
            m_map.setZoomLevel( zoomLevel - 1, Map.Animation.LINEAR);
        });

        clearDBBtn.setOnClickListener(v->{
            LayoutInflater inflater;
            View view;

            inflater = m_activity.getLayoutInflater();
            view = inflater.inflate(R.layout.dialog_exit, null);

            new MaterialAlertDialogBuilder(m_activity)
                    .setCancelable(true)
                    .setView(view)
                    .setPositiveButton("Ok", (dialogInterface, i) -> {

                        attendaceCheck = m_activity.getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor ed = attendaceCheck.edit();
                        ed.putString("new","0");
                        ed.commit();

                        ContentValues cv_active = new ContentValues();
                        ContentValues cv_last_task = new ContentValues();

                        cv_active.put(DBHelper.ACTIVE, 0);
                        db.update(DBHelper.TABLE_TEAMS, cv_active, null,null);

                        cv_last_task.put(DBHelper.LAST_TASK, 0);
                        db.update(DBHelper.TABLE_TEAMS, cv_last_task, null,null);

                        Intent intent = new Intent(m_activity, AuthActivity.class);
                        m_activity.startActivity(intent);
                        m_activity.finish();

                    }).setNegativeButton("Отмена", (dialogInterface, i) ->{
                    })
                    .show();

            posManager.stop();
        });

        teleportBtn.setOnClickListener(v -> {
            //Intent intent = new Intent(m_activity, cameraActivity.class);
            //m_activity.startActivity(intent);

            String lastTaskIndex =  taskManager.getLastTaskIndex();

            Intent intent;

            if(supportArCore){
                intent = new Intent(m_activity, arActivity.class);
            } else {
                intent = new Intent(m_activity, unsuportedArCore.class);
            }

            intent.putExtra("CURRENT_ZONE_NUMBER", lastTaskIndex);
            intent.putExtra("TASK_DONE", false);
            m_activity.startActivity(intent);
        });


        infoBtn.setOnClickListener(v -> {
            LayoutInflater inflater = m_activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_start_task, null);

            new MaterialAlertDialogBuilder(m_activity)
                    .setCancelable(false)
                    .setView(view)
                    .setPositiveButton("ОК", (dialogInterface, i) -> {
                    })
                    .show();
        });

    }

    private void openDialogWindow() {

        LayoutInflater inflater = m_activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_start_task, null);

        new MaterialAlertDialogBuilder(m_activity)
                .setCancelable(false)
                .setView(v)
                .setPositiveButton("Начать", (dialogInterface, i) -> {
                    taskManager.openCurrentTaskDescription();
                })
                .show();

    }

    private void checkFirstAttendance () {
        attendaceCheck = m_activity.getPreferences(MODE_PRIVATE);
        String res = attendaceCheck.getString("new","unknown");

        if(!res.equals("1")){

            SharedPreferences.Editor ed = attendaceCheck.edit();
            ed.putString("new","1");
            ed.commit();

            openDialogWindow();
        }
    }

    void checkArCore() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(m_activity);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkArCore();
                }
            }, 200);
        }

        if (availability.isSupported()) {
            supportArCore = false;

            // indicator on the button.
        } else { // Unsupported or unknown.
            supportArCore = false;

        }
    }

}
