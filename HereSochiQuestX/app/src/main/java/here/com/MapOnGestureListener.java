package here.com;

import android.graphics.PointF;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;

import java.util.List;

// Map gesture listener
public class MapOnGestureListener implements MapGesture.OnGestureListener {

    Map m_map;
    MapMarker posSimulator;
    TaskManager taskManager;
    AppCompatActivity m_activity;

    ImageView teleportBtn;

    public MapOnGestureListener(AppCompatActivity activity, Map map){
        m_map = map;
        m_activity = activity;

        taskManager = new TaskManager(m_activity, m_map);
        posSimulator = new MapMarker(new GeoCoordinate(55.86085, 37.48408));

        m_map.addMapObject(posSimulator);
    }

    private void checkPointInPolygon (GeoCoordinate point) {

        try{

            GeoPolygon checkPolygon = taskManager.getCurrentGeozone();

            if(checkPolygon.contains(point)){
                teleportBtn = m_activity.findViewById(R.id.fab);
                teleportBtn.setVisibility(View.VISIBLE);

                taskManager.updateMap();

//                Toast toast = Toast.makeText(m_activity,
//                        "в зоне", Toast.LENGTH_SHORT);
//                toast.show();

            }

        }catch(NullPointerException err){
            System.out.print(err);
        }
    }

    @Override
    public void onPanStart() {
    }

    @Override
    public void onPanEnd() {
    }

    @Override
    public void onMultiFingerManipulationStart() {
    }

    @Override
    public void onMultiFingerManipulationEnd() {
    }

    @Override
    public boolean onMapObjectsSelected(List<ViewObject> objects) {
        return false;
    }

    @Override
    public boolean onTapEvent(PointF p) {

        GeoCoordinate point = m_map.pixelToGeo(p);
        posSimulator.setCoordinate(point);
        checkPointInPolygon(point);

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(PointF p) {
        return false;
    }

    @Override
    public void onPinchLocked() {
    }

    @Override
    public boolean onPinchZoomEvent(float scaleFactor, PointF p) {
        return false;
    }

    @Override
    public void onRotateLocked() {
    }

    @Override
    public boolean onRotateEvent(float rotateAngle) {
        return false;
    }

    @Override
    public boolean onTiltEvent(float angle) {
        return false;
    }

    @Override
    public boolean onLongPressEvent(PointF p) {
        return false;
    }

    @Override
    public void onLongPressRelease() {
    }

    @Override
    public boolean onTwoFingerTapEvent(PointF p) {
        return false;
    }
}