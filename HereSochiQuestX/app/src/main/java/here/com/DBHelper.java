package here.com;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class DBHelper extends SQLiteOpenHelper {

    private Context fContext;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbSochi";
    public static final String TABLE_TEAMS = "teams";
    public static final String TABLE_ZONES = "zones";

    public static final String KEY_ID = "_id";
    public static final String TEAM_CODE = "team_code";
    public static final String ACTIVE = "active";
    public static final String ROUTE = "route";
    public static final String LAST_TASK = "last_task";

    public static final String ZONE_NUMBER = "zone_number";
    public static final String ZONE_NAME = "zone_name";
    public static final String LANGUAGE = "language";
    public static final String IMG_CODE = "img_code";
    public static final String GEOM = "geom";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        fContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_TEAMS +
                "(" + KEY_ID + " integer primary key autoincrement,"
                + TEAM_CODE + " varchar(200),"
                + ACTIVE + " integer,"
                + ROUTE + " varchar(200),"
                + LAST_TASK + " integer " + ")"
        );

        db.execSQL("create table " + TABLE_ZONES +
                "(" + KEY_ID + " integer primary key autoincrement,"
                + ZONE_NUMBER + " integer,"
                + ZONE_NAME + " varchar(200),"
                + LANGUAGE + " varchar(200),"
                + IMG_CODE + " varchar(200),"
                + GEOM + " varchar(200)" + ")"
        );

        ContentValues values_teams = new ContentValues();
        ContentValues values_zones = new ContentValues();
        Resources res = fContext.getResources();

        XmlResourceParser teams_xml = res.getXml(R.xml.teams_records);
        XmlResourceParser zones_xml = res.getXml(R.xml.zones_records);

        try {
            // Ищем конец документа
            int eventType = teams_xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Ищем теги record
                if ((eventType == XmlPullParser.START_TAG)
                        && (teams_xml.getName().equals("record"))) {
                    // Тег Record найден, теперь получим его атрибуты и
                    // вставляем в таблицу
                    String team_code = teams_xml.getAttributeValue(null,TEAM_CODE);
                    int active = Integer.valueOf(teams_xml.getAttributeValue(null,ACTIVE));
                    String route = teams_xml.getAttributeValue(null,ROUTE);
                    String last_task = teams_xml.getAttributeValue(null,LAST_TASK);

                    values_teams.put(DBHelper.TEAM_CODE, team_code);
                    values_teams.put(DBHelper.ACTIVE, active);
                    values_teams.put(DBHelper.ROUTE, route);
                    values_teams.put(DBHelper.LAST_TASK, last_task);

                    db.insert(TABLE_TEAMS, null, values_teams);
                }
                eventType = teams_xml.next();
            }
        }
        // Catch errors
        catch (XmlPullParserException e) {
            Log.e("Test", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("Test", e.getMessage(), e);

        } finally {
            // Close the xml file
            teams_xml.close();
        }


        try {
            // Ищем конец документа
            int eventType = zones_xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Ищем теги record
                if ((eventType == XmlPullParser.START_TAG)
                        && (zones_xml.getName().equals("record"))) {
                    // Тег Record найден, теперь получим его атрибуты и
                    // вставляем в таблицу
                    int zone_number = Integer.valueOf(zones_xml.getAttributeValue(null,ZONE_NUMBER));
                    String zone_name = zones_xml.getAttributeValue(null,ZONE_NAME);
                    String language = zones_xml.getAttributeValue(null,LANGUAGE);
                    String img_code = zones_xml.getAttributeValue(null,IMG_CODE);
                    String geom = zones_xml.getAttributeValue(null,GEOM);

                    values_zones.put(DBHelper.ZONE_NUMBER, zone_number);
                    values_zones.put(DBHelper.ZONE_NAME, zone_name);
                    values_zones.put(DBHelper.LANGUAGE, language);
                    values_zones.put(DBHelper.IMG_CODE, img_code);
                    values_zones.put(DBHelper.GEOM, geom);

                    db.insert(TABLE_ZONES, null, values_zones);
                }
                eventType = zones_xml.next();
            }
        }
        // Catch errors
        catch (XmlPullParserException e) {
            Log.e("Test", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("Test", e.getMessage(), e);

        } finally {
            // Close the xml file
            zones_xml.close();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + DATABASE_NAME);
        onCreate(db);
    }
}
