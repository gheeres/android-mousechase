package com.heeresonline.mousechase;

import java.util.Iterator;

import android.graphics.PointF;

public class Mouse extends GameObject {
  public static final String TAG = "Mouse";
  
  private GameObject target;
  private PointF next = new PointF(); 
  private float time = 0.0f;
  
  public Mouse(int id, float x, float y) {
    super(id, x, y);
  }

  public Mouse(int id, float x, float y, GameObject target) {
    this(id, x, y);
    this.target = target;
  }

  /**
   * Set's the target.
   * @param target The target
   */
  public void setTarget(GameObject target) {
    this.target = target; 
  }
  
  /**
   * Gets the elapsed time that the mouse has been "alive".
   * @return The number of milliseconds since created.
   */
  public float getTime() {
    return(time);
  }
  
  @Override
  public void step(float deltaTime, Iterable<GameObject> objects) {
    if (target == null) return;
    time += deltaTime;

    if ((Math.abs(target.position.x - position.x) > POSITION_PRECISION) || 
        (Math.abs(target.position.y - position.y) > POSITION_PRECISION)) {
      getNextPosition(deltaTime, target.position.x, target.position.y, next);
      
      if (collidesWith(next.x, next.y, objects, Barrier.class) == null) {
        position.x = next.x;
        position.y = next.y;
      }
      direction = getDirectionTo(target.position.x, target.position.y);

      //Log.v(World.TAG, String.format("[%4d] Mouse move %3.1fx%3.1f (%3.3fx%3.3f) heading %3.2f to %3.1fx%3.1f...", 
      //                               id, position.x, position.y, deltaX, deltaY, direction, target.position.x, target.position.y));
    }
  }
}