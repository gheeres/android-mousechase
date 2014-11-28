package com.heeresonline.mousechase;

import android.util.Log;

public class Mouse extends GameObject {
  public static final String TAG = "Mouse";

  private GameObject target;
  
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
  
  @Override
  public void step(float deltaTime) {
    if (target == null) return;

    if ((target.position.x != position.x) && 
        (target.position.y != position.y)) {
      float deltaX = target.position.x - position.x;
      float deltaY = target.position.y - position.y;
      float distance = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
      deltaX /= distance;
      deltaY /= distance;
      
      position.x += deltaX * speed;
      position.y += deltaY * speed;
      
      direction = getDirectionTo(target.position.x, target.position.y);
      //Log.v(World.TAG, String.format("[%4d] Mouse move %3.1fx%3.1f (%3.3fx%3.3f) heading %3.2f to %3.1fx%3.1f...", 
      //                               id, position.x, position.y, deltaX, deltaY, direction, target.position.x, target.position.y));
    }
  }
}