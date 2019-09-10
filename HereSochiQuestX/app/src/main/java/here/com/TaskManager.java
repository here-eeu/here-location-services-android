package here.com;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapPolygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TaskManager extends AppCompatActivity {

    AppCompatActivity m_activity;
    SQLiteDatabase db;
    DBHelper dbHelper;

    Map m_map;
    GeoPolygon polygon;
    static MapPolygon m_polygon;

    public TaskManager (AppCompatActivity activity, Map map){
        m_activity = activity;
        m_map = map;

        dbHelper = new DBHelper(m_activity);
        db = dbHelper.getReadableDatabase();

    }

    public void completeCurrentTask () {
        Cursor cursor;

        cursor = getActiveUser();

        if(cursor.moveToFirst()){

            int last_task = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.LAST_TASK)));

            if(questIsFinished ()){

            } else {

                last_task += 1;

                ContentValues cv = new ContentValues();
                cv.put(DBHelper.LAST_TASK, last_task);
                String where = DBHelper.ACTIVE + " = ?";
                String[] whereArgs = new String[] {"1"};
                db.update(DBHelper.TABLE_TEAMS, cv, where,whereArgs);
            }
        }
    }

    public void openCongratulationWindow(){
        LayoutInflater inflater;
        View v;

        if(questIsFinished()){
            inflater = m_activity.getLayoutInflater();
            v = inflater.inflate(R.layout.dialog_congratulation, null);

            new MaterialAlertDialogBuilder(m_activity)
                    .setCancelable(false)
                    .setView(v)
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                    })
                    .show();
        }

    }

    public void openCurrentTaskDescription () {
        Cursor cursor;
        Cursor cursorTask;

        LayoutInflater inflater;

        TextView zone_language_text;
        ImageView imgV;

        View v;

        inflater = m_activity.getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_task_img, null);

        imgV = v.findViewById(R.id.imageView2);
        zone_language_text = v.findViewById(R.id.language);

        cursor = getActiveUser();

        // If there is an active user
        if(cursor.moveToFirst()){

            // Get zone_number by last task index
            String zone_number = getCurrentZoneNumber ();

            // Get zone info by name
            cursorTask = getZoneByNumber(zone_number);

            if (cursorTask.moveToFirst()) {
                String language = cursorTask.getString(cursorTask.getColumnIndex(DBHelper.LANGUAGE));
                String img_code = cursorTask.getString(cursorTask.getColumnIndex(DBHelper.IMG_CODE));

                zone_language_text.setText(language);
                imgV.setImageResource(m_activity.getResources().getIdentifier("drawable/" + img_code, null, m_activity.getPackageName()));

                updateMap();

                new MaterialAlertDialogBuilder(m_activity)
                        .setCancelable(false)
                        .setView(v)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                        })
                        .show();
            }
        }
    }

    public HashMap<String, String> getCurrentTaskDesctiption () {
        Cursor cursor;

        HashMap<String, String> hmap = null;
        cursor = getActiveUser();

        if(cursor.moveToFirst()){

            // Get zone_number by last task index
            String zone_number = getCurrentZoneNumber ();

            // Get zone info by name
            cursor = getZoneByNumber(zone_number);

            if (cursor.moveToFirst()) {

                String number = cursor.getString(cursor.getColumnIndex(DBHelper.ZONE_NUMBER));
                String name = cursor.getString(cursor.getColumnIndex(DBHelper.ZONE_NAME));
                String language = cursor.getString(cursor.getColumnIndex(DBHelper.LANGUAGE));
                String img_code = cursor.getString(cursor.getColumnIndex(DBHelper.IMG_CODE));
                String geom = cursor.getString(cursor.getColumnIndex(DBHelper.GEOM));

                hmap = new HashMap<>();
                hmap.put(DBHelper.ZONE_NUMBER, number);
                hmap.put(DBHelper.ZONE_NAME, name);
                hmap.put(DBHelper.LANGUAGE, language);
                hmap.put(DBHelper.IMG_CODE, img_code);
                hmap.put(DBHelper.GEOM, geom);
                
                return hmap;
            }
        }

        return hmap;
    }

    public Boolean questIsFinished (){
        Cursor cursor;

        cursor = getActiveUser();

        if(cursor.moveToFirst()){
            int last_task = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.LAST_TASK)));

            if(last_task == 6){
               return true;
            } else {
                return false;
            }
        }

        return false;
    }

    // Clear map and add near polygon
    public void updateMap () {


        polygon = getCurrentGeozone();

        m_polygon = new MapPolygon(polygon);
        m_polygon.setFillColor(Color.TRANSPARENT);
        m_polygon.setLineWidth(2);
        m_polygon.setLineColor(Color.GREEN);

        m_map.addMapObject(m_polygon);
    }

    // Convert route to polygon
    private GeoPolygon geom2GeoPolygon (String geometry) {
        String[] points = geometry.split(";");

        List<GeoCoordinate> geoPoints = new ArrayList<GeoCoordinate>();

        for(String point : points){

            String[] coords = point.split(",");
            double lat = Double.parseDouble(coords[0]);
            double lng = Double.parseDouble(coords[1]);
            int alt = 0;

            geoPoints.add(new GeoCoordinate(lat,lng, alt));
        }

        GeoPolygon geoshape = new GeoPolygon(geoPoints);

        return geoshape;
    }

    // Query active user
    public Cursor getActiveUser () {
        String query;
        Cursor cursor;

        query = "SELECT * FROM " + DBHelper.TABLE_TEAMS + " WHERE active = ?";
        cursor = db.rawQuery(query, new String[] {"1"});

        return cursor;
    }

    // Query zone by number
    public Cursor getZoneByNumber (String zone_number) {
        String query;
        Cursor cursor;

        query = "SELECT * FROM " + DBHelper.TABLE_ZONES + " WHERE zone_number = ?";
        cursor = db.rawQuery(query, new String []{zone_number});

        return cursor;
    }

    public String getCurrentZoneNumber () {
        Cursor cursor = getActiveUser();

        if(cursor.moveToFirst()){
            //Get last task
            int last_task_index = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBHelper.LAST_TASK)));

            // Get team route & parse
            String route = cursor.getString(cursor.getColumnIndex(DBHelper.ROUTE));
            String [] zones = route.split(",");

            // Get zone_number by last task index
            String zone_number = zones[last_task_index];

            return zone_number;
        } else {
            return null;
        }
    }

    public GeoPolygon getCurrentGeozone () {
        GeoPolygon geoshape = null;
        String zone_number;
        Cursor cursor;

        zone_number = getCurrentZoneNumber ();
        cursor = getZoneByNumber(zone_number);

        if(cursor.moveToFirst()){
            geoshape = geom2GeoPolygon(cursor.getString(cursor.getColumnIndex(DBHelper.GEOM)));
        }

        return geoshape;
    }

    public String getLastTaskIndex () {
        Cursor cursor = getActiveUser();

        if(cursor.moveToFirst()){
            String last_task = cursor.getString(cursor.getColumnIndex(DBHelper.LAST_TASK));
            return last_task;
        }
        return null;
    }

}


