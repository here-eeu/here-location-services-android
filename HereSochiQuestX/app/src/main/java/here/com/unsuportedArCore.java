package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Camera;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.HashMap;

public class unsuportedArCore extends AppCompatActivity {

    TaskManager taskManager;



    private String lastTaskIndex;
    private boolean taskDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsuported_ar_core);

        //по хорошему определить отдельную функцию и вызвать из разных мест...
        //но я просто скопирую тот же самый код. ИБО БЛЯТЬ МОГУ
        lastTaskIndex = (String)getIntent().getSerializableExtra("CURRENT_ZONE_NUMBER");
        taskDone = (boolean)getIntent().getSerializableExtra("TASK_DONE");
        //Log.e("intent",  lastTaskIndex);

        TextView arText = (TextView)findViewById(R.id.morphArText);

        Button arButton = (Button)findViewById(R.id.morphArButton);

        ImageView arImage= (ImageView)findViewById(R.id.questImage);
        ImageView notAR= (ImageView)findViewById(R.id.notAr);


        taskManager = new TaskManager(this, null);

        HashMap<String, String> questData = taskManager.getCurrentTaskDesctiption();

        String questLanguage = getResources().getString(R.string.newTask) + questData.get("language");
        String questImg = questData.get("img_code");

        if(taskDone){


            //TODO вставить получение задания
            arText.setText(questLanguage);
            arImage.setImageResource(getResources().getIdentifier("drawable/" + questImg, null, getPackageName()));
            arButton.setText(getResources().getString(R.string.backToMap));

        } else {
            arText.setText(getResources().getString(R.string.done));
            arButton.setText(getResources().getString(R.string.stringSelfieButton));
        }

//        Toast toast = Toast.makeText(getApplicationContext(),
//                lastTaskIndex, Toast.LENGTH_SHORT);
//        toast.show();

        switch (lastTaskIndex){
            case ("1"):
                notAR.setImageResource(R.drawable.misiks);
                break;
            case ("2"):
                notAR.setImageResource(R.drawable.sheptun);
                break;
            case ("3"):
                notAR.setImageResource(R.drawable.head);
                break;
            case ("4"):
                notAR.setImageResource(R.drawable.birdman);
                break;
            case ("5"):
                notAR.setImageResource(R.drawable.axis);
                break;
            default:
                notAR.setImageResource(R.drawable.jopasranchik);
                break;
        }


    }

    //как ты поняд, это просто ctrl+c ctrl+v
    public void goSelfie(View target) {
        //Да, мы таскаем lastTaskIndex по активностям... что бы не было ошибок ;3
        //Ахуенно, не правда ли?

        //6:20 на часах
        //Зацени какой костль придумал. Сам в ахуе
        //Я щас такой думал запилить переключение через switch. Булевой переменной.
        //КАРЛ, БУЛЕВАЯ ПЕРЕМЕННАЯ В ОПЕРАТОРЕ SWITCH
        //А когда он начал ругаться на это, думал bool в int перевести
        if(taskDone){
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        } else {
            Intent intent = new Intent(this, cameraActivity.class);
            intent.putExtra("CURRENT_ZONE_NUMBER", lastTaskIndex);
            intent.putExtra("TRUE_AR", false);
            this.startActivity(intent);
        }

    }

}
