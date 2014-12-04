package com.heeresonline.mousechase;

import android.graphics.PointF;
import android.util.Log;

public class Cat extends GameObject {
  public static final String TAG = "Cat";
  private PointF destination = new PointF();
  private PointF next = new PointF();
  
  public Cat(int id, float x, float y) {
    super(id, x, y);

    Log.d(TAG, String.format("Creating cat at %5.2fx%5.2f.", position.x, position.y));
    setDestination(x, y);
  }

  /**
   * Set's the destination that the Cat is moving towards
   * @param x The x parameter where to move.
   * @param y The y parameter where to move
   */
  public void setDestination(float x, float y) {
    destination.x = x;
    destination.y = y;

    Log.v(TAG, String.format("Setting cat destination to %5.2fx%5.2f. Currently at %5.2fx%5.2f", destination.x, destination.y, position.x, position.y));
  }
  
  /**
   * Stops movement.
   */
  public void stop() {
    destination.x = position.x;
    destination.y = position.y;

    Log.v(TAG, String.format("Stopping cat movement. Position: %5.2fx%5.2f.", position.x, position.y));
  }
  
  @Override
  public void step(float deltaTime, Iterable<GameObject> objects) {
    if ((Math.abs(destination.x - position.x) > POSITION_PRECISION) || 
        (Math.abs(destination.y - position.y) > POSITION_PRECISION)) {
      getNextPosition(deltaTime, destination.x, destination.y, next);

      GameObject obj;
      if ((obj = collidesWith(next.x, next.y, objects, Barrier.class)) == null) {
        position.x = next.x;
        position.y = next.y;
      }
      else {
        Log.v(TAG, String.format("Collision with GameObject %d (%5.2f,%5.2f) with size %5.2f @ %5.2fx%5.2f detected. Unable to move.", 
                                 obj.id, obj.position.x, obj.position.y, obj.size,
                                 position.x, position.y));
        destination.x = position.x;
        destination.y = position.y;
      }
      direction = getDirectionTo(destination.x, destination.y);
    }
  }
}