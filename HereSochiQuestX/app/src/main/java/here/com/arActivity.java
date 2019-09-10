package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.HashMap;


public class arActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ModelRenderable andyRenderable;
  private ArFragment arFragment;
  private AnchorNode anchorNode;
  private int DefRenderable;

  TaskManager taskManager;


  private String lastTaskIndex;
  private boolean taskDone;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);

    arFragment = (ArFragment)
            getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    arFragment.getPlaneDiscoveryController().hide();
    arFragment.getPlaneDiscoveryController().setInstructionView(null);


    lastTaskIndex = (String)getIntent().getSerializableExtra("CURRENT_ZONE_NUMBER");
    taskDone = (boolean)getIntent().getSerializableExtra("TASK_DONE");
    //Log.e("intent",  lastTaskIndex);

    TextView arText = (TextView)findViewById(R.id.morphArText);

    Button arButton = (Button)findViewById(R.id.morphArButton);

    ImageView arImage= (ImageView)findViewById(R.id.questImage);

    taskManager = new TaskManager(this, null);

    //оно  работает
    HashMap<String, String> questData = taskManager.getCurrentTaskDesctiption();

    String questLanguage = questData.get("language");
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

    Toast toast = Toast.makeText(getApplicationContext(),
            lastTaskIndex, Toast.LENGTH_SHORT);
    toast.show();

    switch (lastTaskIndex){
      case ("1"):
        DefRenderable =  R.raw.pickle_rick;
        break;
      case ("2"):
        DefRenderable =  R.raw.model;
        break;
      case ("3"):
        DefRenderable =  R.raw.butthole;
        break;
      case ("4"):
        DefRenderable =  R.raw.butterrobot;
        break;
      case ("5"):
        DefRenderable =  R.raw.pickle_rick;
        break;
      default:
        DefRenderable = R.raw.pickle_rick;
        break;
    }

    ModelRenderable.builder()
            .setSource(this, DefRenderable)
            .build()
            .thenAccept(renderable -> andyRenderable = renderable)
            .exceptionally(
                    throwable -> {
                      Log.e(TAG, "Unable to load Renderable.", throwable);
                      return null;
                    });

    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
  }

  private void onSceneUpdate(FrameTime frameTime) {
    // Let the fragment update its state first.
    arFragment.onUpdate(frameTime);

    // If there is no frame then don't process anything.
    if (arFragment.getArSceneView().getArFrame() == null) {
      return;
    }

    // If ARCore is not tracking yet, then don't process anything.
    if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
      return;
    }

    // Place the anchor 1m in front of the camera if anchorNode is null.
    if (this.anchorNode == null) {
      Session session = arFragment.getArSceneView().getSession();

      float[] pos = { 0,-1,-1 };
      float[] rotation = {0,-1,0,1};

      switch (lastTaskIndex){
        case ("1"):
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = 7;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
        case ("2"):
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = 0;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
        case ("3"):
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = 3;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
        case ("4"):
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = -1;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
        case ("5"):
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = 7;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
        default:
          pos[0] = 0;
          pos[1] = -1;
          pos[2] = -1;
          rotation[0] = 0;
          rotation[1] = 7;
          rotation[2] = 0;
          rotation[3] = 1;
          break;
      }

      Anchor anchor =  session.createAnchor(new Pose(pos, rotation));

      anchorNode = new AnchorNode(anchor);
      anchorNode.setRenderable(andyRenderable);
      anchorNode.setParent(arFragment.getArSceneView().getScene());
    }
  }

  //на часах 03:47. Спать хочется пиздец как. Ебал в рот разработку под андроид. То, что в других языках сделано легко и просто...
  //ЗДЕСЬ БЛЯТЬ АДСКАЯ ЁБАНЬ, СУКА БЛЯТЬ, КУРВА
  //Вот почему я не могу задать border и скругление углов простым параметром
  //НЕТ БЛЯТЬ, РИСУЙ КАРТИНКУ СУКА
  //В рот ебал
  //И да, я не хочу пользоваться многострочными комментариями
  //ИБО ЕСЛИ НАЧАЛ ЕБАШИТЬ КОСТЫЛИ, ЕБАШЬ ДО КОНЦА, СТАНЬ ЛУЧШИМ В ЭТОМ
  //P.S. sudo сделать красиво
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
      intent.putExtra("TRUE_AR", true);
      this.startActivity(intent);
    }

  }
}


//position for pickle rick
//pickle_rick
// float[] pos = { 0,-1,-1 };
// float[] rotation = {0,7,0,1};

//showball
//model
//float[] pos = { 0,-1,-1 };
//float[] rotation = {0,0,0,1};

//butthole
// float[] pos = { 0,-1,-1 };
//float[] rotation = {0,3,0,1};

// butterrobot
//float[] pos = { 0,-1,-1 };
//float[] rotation = {0,-1,0,1};

