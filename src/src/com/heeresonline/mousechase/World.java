package com.heeresonline.mousechase;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.heeresonline.mousechase.SoundFactory.Sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

/**
 * Represents the game world.
 */
public class World implements Runnable {
  final static String TAG = "World";
  
  private final static int DEFAULT_WIDTH = 1024;
  private final static int DEFAULT_HEIGHT = 768;
  
  private final static int MAX_SOUND_STREAMS = 8;

  private final static int MAX_FPS = 40; //desired fps   
  private final static int MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private final static int MOUSE_INTERVAL = 6000; // The number of milliseconds when a new mouse is introduced
  
  private int width = 1024;
  private int height = 768;

  private final static int ELAPSED_GAME_TIME = 0;
  private final static int ELAPSED_SINCE_LAST_MOUSE = 1;
  private float[] time = new float[2];

  private boolean isRunning = false;
  private final Random random = new Random();
  private final List<GameObject> objects = new CopyOnWriteArrayList<GameObject>();
  private int count = 0;  
  private Cat cat;

  private Context context;
  private WorldState state = WorldState.INITIALIZING;
  private Thread loop;
  private SoundPool pool;
  private MediaPlayer mediaPlayer;
  private Vector<GameObjectChangeEvent> listeners;
  
  public World(Context context) {
    this(context, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }
  
  public World(Context context, int width, int height) {
    this.context = context;
    
    this.width = width;
    this.height = height;
    Log.i(TAG, String.format("Initializing world size to %dx%d.", width, height));
    
    //AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    //Log.d(TAG, String.format("Maximum volume: %d", mgr.getStreamVolume(AudioManager.STREAM_MUSIC)));
    initializeMediaPlayer(context.getAssets());
    loadSoundAssets(context.getAssets());
  }

  /**
   * Initializes the background media player. This is used for
   * looping a background sound since the SoundPool API is broken
   * in Android 4.3.
   * @param assets The asset manager.
   */
  protected void initializeMediaPlayer(AssetManager assets) {
    // Play proximity sound at 0 level. Loop forever
    // 
    // TODO: Looping is broken in Android 4.3
    // https://code.google.com/p/android/issues/detail?id=58113
    //SoundFactory.sounds.get("proximity").play(0, 0, -1);
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
    }
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setVolume(0, 0);
    mediaPlayer.setLooping(true);;
    mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, String.format("Media player initialized."));
        mediaPlayer.start();
      }
    });
    AssetFileDescriptor afd = null;
    try {
      if ((afd = assets.openFd("sounds/proximity.mp3")) != null) {
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepareAsync();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (afd != null) {
          afd.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Loads the sound assets for the application.
   * @param pool The sound pool to populate.
   */
  protected void loadSoundAssets(AssetManager assets) {
    final int SOUND_SAMPLES = 3;
    state = WorldState.INITIALIZING;

    pool = new SoundPool(MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, 0);
    pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
      private int count = 0;
      @Override
      public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        count++;
        Log.d(TAG, String.format("%d / %d sound samples loaded. Status[%d]: %d", count, SOUND_SAMPLES, sampleId, status));
        
        if (count == SOUND_SAMPLES) {
          state = WorldState.READY;
          Log.d(TAG, "All sound assets loaded.");
        }
      }
    });
    SoundFactory.loadSound(pool, "cat", assets, "sounds/cat.mp3");
    SoundFactory.loadSound(pool, "mouse", assets, "sounds/mouse.mp3");
    SoundFactory.loadSound(pool, "gameover", assets, "sounds/gameover.mp3");
    //SoundFactory.loadSound(pool, "proximity", assets, "sounds/proximity.mp3");
  }
  
  public void start() {
    resume();
    pool.autoResume();
  }
  
  public void pause() {
    isRunning = false;
    pool.autoPause();
    state = WorldState.PAUSED;
    Log.d(TAG, String.format("Pausing world loop."));
  }
  
  public void resume() {
    Log.d(TAG, String.format("Resuming world loop."));
    isRunning = true;
    if (loop == null) {
      loop = new Thread(this);
      loop.start();
    }
  }
  
  /**
   * Registers the specified listener for a GameObjectChangeEvent
   * @param listener The listener to register.
   */
  public void addGameObjectChangeEventListener(GameObjectChangeEvent listener) {
    if (listener == null) return;
    
    if (listeners == null) listeners = new Vector<GameObjectChangeEvent>();
    listeners.addElement(listener);
  }

  /**
   * Listener trigger for GameObjectChangeEvent handler when object is added.
   * @param obj The added GameObject
   */
  private void onGameObjectAdded(GameObject obj) {
    if (obj == null) return;
    if ((listeners != null) && (! listeners.isEmpty())) {
      Enumeration<GameObjectChangeEvent> e = listeners.elements();
      while (e.hasMoreElements()) {
        GameObjectChangeEvent listener = (GameObjectChangeEvent) e.nextElement();
        listener.added(obj);
      }
    }
  }
  
  /**
   * Initializes the game world. 
   */
  public void initialize() {
    objects.clear();
    
    cat = new Cat(0, width/2, height/2);
    cat.speed = (width / 2.0f);
    addGameObject(cat);
  }

  /**
   * Adds the specified game object to the world.
   * @param obj The GameObject to add.
   */
  public void addGameObject(GameObject obj) {
    objects.add(obj);
    if (state != WorldState.INITIALIZING) {
      if (obj instanceof Mouse) {
        SoundFactory.sounds.get("mouse").play();
      }
    }
    onGameObjectAdded(obj);
  }

  @Override
  public void run() {
    int sleepTime = 0;
    long startTime = System.currentTimeMillis();
    isRunning = true;

    // Don't start the game loop until we're ready...
    while ((state != WorldState.RUNNING) && (state != WorldState.READY)) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }      
    }
    Log.d(TAG, "World state is READY. Switching to RUNNING mode.");
    state = WorldState.RUNNING;
    SoundFactory.sounds.get("cat").play();

    while (isRunning) {
      int framesSkipped = 0;
      float deltaTime = (System.currentTimeMillis() - startTime);
      time[ELAPSED_GAME_TIME] += deltaTime;
      startTime = System.currentTimeMillis();

      step(deltaTime);
      
      // Constant frame rate / speed
      sleepTime = (int) (FRAME_PERIOD - deltaTime);
      if (sleepTime > 0) {
        try {
          //Log.v(TAG, String.format("Nothing to do, sleeping for %d msec", sleepTime));
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
      }
      while ((sleepTime < 0) && (framesSkipped < MAX_FRAME_SKIPS)) {
        //Log.v(TAG, String.format("%d / %d frame(s) skipped. Behind by %d msec", 
        //                        framesSkipped+1, MAX_FRAME_SKIPS, sleepTime));
        step(sleepTime);
        sleepTime += FRAME_PERIOD;
        framesSkipped++;
      }
    }
  }
  
  /**
   * Get's a random entry point from the edge of the screen.
   * @param bottom The lower bounds
   * @param left The left bounds
   * @param right The right bounds
   * @param top The top bounds
   * @return The random entry point at the edge of the bounds.
   */
  protected Point getRandomEntryPoint(int bottom, int left, int right, int top) {
    switch(random.nextInt(3)) {
      // Bottom entry
      case 0:
        return(new Point(left + random.nextInt(right - left), bottom));
      // Left entry
      case 1:
        return(new Point(left, bottom + random.nextInt(top - bottom)));
      // Right entry
      case 2:
        return(new Point(right, bottom + random.nextInt(top - bottom)));
      // Top entry
      case 3:
        return(new Point(left + random.nextInt(right - left), top));

      // Middle (Not enabled... Advanced...)
      default:
        return(new Point(left + random.nextInt(right - left), bottom + random.nextInt(top - bottom)));
    }
  }

  /**
   * Get's the number of mouse currently in the game world.
   * @return The current number of mice.
   */
  public int getCount() {
    return(count);
  }

  /**
   * Sets the width of the game world.
   * @param width The width.
   */
  public void setWidth(int width) {
    Log.d(TAG, String.format("Changing world width from %d to %d.", this.width, width));
    this.width = width;
  }

  /**
   * Sets the height of the game world.
   * @param height The height.
   */
  public void setHeight(int height) {
    Log.d(TAG, String.format("Changing world height from %d to %d.", this.height, height));
    this.height = height;
  }
  
  /**
   * Adds a mouse at a random location.
   * @param count The number of mice to add.
   */
  public void addMouse(int mice) {
    time[ELAPSED_SINCE_LAST_MOUSE] = 0;

    for(int index = 0; index < mice; index++) {
      Point entry = getRandomEntryPoint(0, 0, this.width, this.height);
      Mouse mouse = new Mouse(count, entry.x, entry.y, cat);
      mouse.speed = random.nextFloat() * cat.speed; // Vary speed of the mice.
      mouse.direction = cat.getDirectionTo(mouse.position.x, mouse.position.y);

      count++;
      Log.d(TAG, String.format("Adding new mouse to screen at %5.1fx%5.1f with speed %5.2f heading %5.2f.", mouse.position.x, mouse.position.y, mouse.speed, mouse.direction));
      addGameObject(mouse);
    }
  }

  /**
   * Requests the game object to update based on inputs
   * @param deltaTime the time in milliseconds since the last update.
   */
  public void step(float deltaTime) {
    if (state != WorldState.RUNNING) return;
    
    //Log.d(TAG, String.format("Elapsed: %12.8f, Diff: %9.8f", (int)elapsed / 1000, (((int) elapsed / 1000) % MOUSE_INTERVAL)));
    time[ELAPSED_SINCE_LAST_MOUSE] += deltaTime;
    if (time[ELAPSED_SINCE_LAST_MOUSE] > MOUSE_INTERVAL) {
      time[ELAPSED_SINCE_LAST_MOUSE] = time[ELAPSED_SINCE_LAST_MOUSE] - MOUSE_INTERVAL;

      int mice = (int) Math.min(getCount() / 3, 6);
      addMouse((mice <= 0) ? 1 : mice);
    }

    float proximity = Float.MAX_VALUE;
    for(Iterator<GameObject> iterator = getGameObjects().iterator(); iterator.hasNext(); ) {
      GameObject obj = iterator.next();
      if (obj != null) {
        obj.step(deltaTime);
        
        // Collision check / Game Over?
        if (! (obj instanceof Cat)) {
          float distance = cat.getDistanceFrom(obj.position.x, obj.position.y);
          if ((distance - obj.size - cat.size) < proximity) proximity = distance - obj.size - cat.size;
          
          if (cat.intersectsWith(obj.position.x, obj.position.y, obj.size)){
            mediaPlayer.stop();
            SoundFactory.sounds.get("gameover").play();
            Log.d(TAG, String.format("GAME OVER. Collision with [%d] at %5.2fx%5.2f.", obj.id, obj.position.x, obj.position.y));
            state = WorldState.GAMEOVER;
          }
        }
      }
    }

    if ((cat != null) && (state != WorldState.GAMEOVER)) {
      float volume = Math.min(cat.size / ((proximity > 0) ? proximity : cat.size), 1.0f);
      mediaPlayer.setVolume(volume, volume);
    }
  }

  /**
   * Retrieves the current game objects.
   * @return
   */
  public Iterable<GameObject> getGameObjects() {
    return(objects != null ? objects : Collections.<GameObject>emptyList());
  }

  /**
   * Plays the meow sound for the cat.
   */
  public void meow() {
    SoundFactory.sounds.get("cat").play();
  }

  public void moveCatTo(float x, float y) {
    cat.setDestination(x,  y);
  }

  public void stopCat() {
    cat.stop();
  }
  
  public enum WorldState {
    INITIALIZING, READY, RUNNING, PAUSED, GAMEOVER
  }
  
  /**
   * Gets the current world state.
   * @return The state of the world
   */
  public WorldState getState() {
    return(state);
  }
}
