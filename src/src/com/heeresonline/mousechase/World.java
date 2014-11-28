package com.heeresonline.mousechase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbox2d.dynamics.Body;

import android.graphics.Canvas;
import android.graphics.Point;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Represents the game world.
 */
public class World implements Runnable {
  final static String TAG = "World";
  
  private final static int DEFAULT_WIDTH = 1024;
  private final static int DEFAULT_HEIGHT = 768;
  
  private final static int MAX_FPS = 40; //desired fps   
  private final static int MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private final static int MOUSE_INTERVAL = 6000; // The number of milliseconds when a new mouse is introduced
  private final static int CAT_INTERVAL = 10000; // The number of milliseconds when a new mouse is introduced
  
  private int width = 1024;
  private int height = 768;

  private final static int ELAPSED_GAME_TIME = 0;
  private final static int ELAPSED_SINCE_LAST_MOUSE = 1;
  private final static int ELAPSED_SINCE_LAST_CAT = 2;
  private float[] time = new float[3];

  private boolean isRunning = false;
  private final Random random = new Random();
  private final List<GameObject> objects = new CopyOnWriteArrayList<GameObject>();
  private int count = 0;  
  private final Cat cat;
  
  public World() {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }
  
  public World(int width, int height) {
    this.width = width;
    this.height = height;
    Log.i(TAG, String.format("Initializing world size to %dx%d.", width, height));

    cat = new Cat(0, width/2, height/2);
    objects.add(cat);
    
    Log.d(TAG, String.format("N/0   = %5.2f", cat.getDirectionTo(cat.position.x, height)));
    Log.d(TAG, String.format("NE    = %5.2f", cat.getDirectionTo(width, height)));
    Log.d(TAG, String.format("E/90  = %5.2f", cat.getDirectionTo(width, cat.position.y)));
    Log.d(TAG, String.format("SE    = %5.2f", cat.getDirectionTo(width, 0)));
    Log.d(TAG, String.format("S/180 = %5.2f", cat.getDirectionTo(cat.position.x, 0)));
    Log.d(TAG, String.format("SW    = %5.2f", cat.getDirectionTo(0, 0)));
    Log.d(TAG, String.format("W/270 = %5.2f", cat.getDirectionTo(0, cat.position.y)));
    Log.d(TAG, String.format("NW    = %5.2f", cat.getDirectionTo(0, height)));
  }
  
  public void pause() {
    isRunning = false;
  }
  
  @Override
  public void run() {
    int sleepTime = 0;
    long startTime = System.currentTimeMillis();
    isRunning = true;

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
          Log.v(TAG, String.format("Nothing to do, sleeping for %d msec", sleepTime));
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
      }
      while ((sleepTime < 0) && (framesSkipped < MAX_FRAME_SKIPS)) {
        Log.v(TAG, String.format("%d / %d frame(s) skipped. Behind by %d msec", 
                                framesSkipped+1, MAX_FRAME_SKIPS, sleepTime));
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
    this.width = width;
  }

  /**
   * Sets the height of the game world.
   * @param height The height.
   */
  public void setHeight(int height) {
    this.height = height;
  }
  
  /**
   * Adds a mouse at a random location.
   */
  public void addMouse() {
    time[ELAPSED_SINCE_LAST_MOUSE] = 0;
    
    Point entry = getRandomEntryPoint(0, 0, this.width, this.height);
    Mouse mouse = new Mouse(count, entry.x, entry.y, cat);
    mouse.speed += random.nextFloat() * 0.1; // Vary speed of the mice.
    mouse.direction = cat.getDirectionTo(mouse.position.x, mouse.position.y);

    count++;
    objects.add(mouse);
    Log.d(TAG, String.format("Adding new mouse to screen at %5.1fx%5.1f with speed %5.2f heading %5.2f.", mouse.position.x, mouse.position.y, mouse.speed, mouse.direction));
  }

  /**
   * Requests the game object to update based on inputs
   * @param deltaTime the time in milliseconds since the last update.
   */
  public void step(float deltaTime) {
    //Log.d(TAG, String.format("Elapsed: %12.8f, Diff: %9.8f", (int)elapsed / 1000, (((int) elapsed / 1000) % MOUSE_INTERVAL)));
    time[ELAPSED_SINCE_LAST_MOUSE] += deltaTime;
    if (time[ELAPSED_SINCE_LAST_MOUSE] > MOUSE_INTERVAL) {
      time[ELAPSED_SINCE_LAST_MOUSE] = time[ELAPSED_SINCE_LAST_MOUSE] - MOUSE_INTERVAL;
      addMouse();
      
      for(Iterator<GameObject> iterator = getGameObjects().iterator(); iterator.hasNext();) {
        GameObject obj = iterator.next();
        if ((obj != null) && (obj.getClass() == Mouse.class)) {
          Log.d(TAG, String.format("[%4d] Mouse at %3.1fx%3.1f heading %3.2f to %3.1fx%3.1f...", 
                                   obj.id, obj.position.x, obj.position.y, obj.direction, 
                                   cat.position.x, cat.position.y));
        }
      }
    }

    time[ELAPSED_SINCE_LAST_CAT] += deltaTime;
    if (time[ELAPSED_SINCE_LAST_CAT] > CAT_INTERVAL) {
      time[ELAPSED_SINCE_LAST_CAT] = time[ELAPSED_SINCE_LAST_CAT] - CAT_INTERVAL;
      cat.position.x = random.nextInt(width);
      cat.position.y = random.nextInt(height);
      Log.d(TAG, String.format("Moving cat to %5.2fx%5.2f...", cat.position.x, cat.position.y));
    }

    int index = 0;
    for(Iterator<GameObject> iterator = getGameObjects().iterator(); iterator.hasNext(); index++) {
      GameObject obj = iterator.next();
      if (obj != null) {
        obj.step(deltaTime);
      }
    }
  }

  /**
   * Retrieves the current game objects.
   * @return
   */
  public Iterable<GameObject> getGameObjects() {
    return(objects != null ? objects : Collections.<GameObject>emptyList());
  }
}
