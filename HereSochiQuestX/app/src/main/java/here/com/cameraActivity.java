package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class cameraActivity extends AppCompatActivity {

    TaskManager taskManager;

    private String lastTaskIndexCamera;

    private boolean trueAr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }

        lastTaskIndexCamera = (String)getIntent().getSerializableExtra("CURRENT_ZONE_NUMBER");
        trueAr = (boolean)getIntent().getSerializableExtra("TRUE_AR");
    }


    public void doneSelfie(View target) {
        taskManager = new TaskManager(this, null);

        //Да, мы таскаем lastTaskIndex по активностям... что бы не было ошибок ;3
        //Ахуенно, не правда ли?
        Intent intent;

        if(trueAr){
            intent = new Intent(this, arActivity.class);
        } else {
            intent = new Intent(this, unsuportedArCore.class);
        }

        intent.putExtra("CURRENT_ZONE_NUMBER", lastTaskIndexCamera);
        intent.putExtra("TASK_DONE", true);

        taskManager.completeCurrentTask();
        this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            //возможно понадобиться потом. Но это не точно.
            //тебе наверно интересно почему половина комментов на русском, а половина на английском?
            //ИБО МНЕ БЛЯТЬ ЛЕНЬ ПЕРЕВОДИТЬ ТЕКСТ В 6 УТРА БЛЯТЬ
            Toast toast = Toast.makeText(getApplicationContext(),
                    "BACK KEY", Toast.LENGTH_SHORT);
            //toast.show();
            return true;
        }

        return onKeyDown(keyCode, event);
    }
}
