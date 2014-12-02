package com.heeresonline.mousechase;

import java.util.Random;

import android.graphics.PointF;

public abstract class GameObject {
  public static final String TAG = "GameObject";
  protected static final float POSITION_PRECISION = 0.99999f;
  protected static Random random = new Random();

  public int id = 0;
  public final PointF position;
  public float direction;
  public float speed = 1.0f;
  public float size = 10.0f;

  /**
   * Creates an instance of the GameObject class.
   * @param id The optional unique id for the game object.
   * @param x The x position.
   * @param y The y position.
   */
  protected GameObject(int id, float x, float y) {
    this.id = id;
    position = new PointF(x, y);
  }
  
  /**
   * Gets the direction / angle from the point to this game object.
   * @param x The x position to calculate from.
   * @param y The y position to calculate from.
   * @return The direction or angle to.
   */
  public float getDirectionTo(float x, float y) {
    double direction = Math.atan2(position.y - y, position.x - x);
    
    if (direction < 0) direction = Math.abs(direction);
    else direction = 2*Math.PI - direction;
    direction -= Math.toRadians(90);

    float angle = (float) Math.toDegrees(direction);
    if (angle == 360) return(0);
    if (angle < 0) return(360 + angle);
    return(angle);
  }

  /**
   * Checks to see if the game object intersects with the x and y coordinate of the specified circle.
   * @param x The center x position to check.
   * @param x The center y position to check.
   * @param size The size of the item at the position.
   * @return True if the object is intersected, false if otherwise.
   */
  public boolean intersectsWith(float x, float y, float size) {
    float distance = getDistanceFrom(x, y);
    return(distance - (this.size + size) <= 0);
  }

  /**
   * Get's the distance between the points.
   * @param x The x position
   * @param y The y position
   * @return The distance between the positions.
   */
  public float getDistanceFrom(float x, float y) {
    float deltaX = x - position.x;
    float deltaY = y - position.y;
    return((float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
  }
  
  /**
   * Gets the next step position based on the speed and direction.
   * @param deltaTime The time elapsed in milliseconds.
   * @param x The destination x position.
   * @param y The destination y position.
   * @param next The calculated next position.
   */
  public PointF getNextPosition(float deltaTime, float x, float y, PointF next) {
    next.x = position.x;
    next.y = position.y;
    if ((Math.abs(x - position.x) > POSITION_PRECISION) || 
        (Math.abs(y - position.y) > POSITION_PRECISION)) {
      float distance = getDistanceFrom(x, y);
      float deltaX = x - position.x;
      float deltaY = y - position.y;
      deltaX /= distance;
      deltaY /= distance;
        
      float distanceX = deltaX * (speed * (deltaTime / 1000));
      if ((position.x + distanceX > x) || (position.x - distanceX < x)) next.x += distanceX;
      else next.x = x;
      
      float distanceY = deltaY * (speed * (deltaTime / 1000));
      if ((position.y + distanceY > y) || (position.y - distanceY < y)) next.y += distanceY;
      else next.y = y;
    }
    return(next);
  }
  
  /**
   * Update the game object position.
   * @param deltaTime The elapsed time in milliseconds since the last step.
   */
  public abstract void step(float deltaTime);
}