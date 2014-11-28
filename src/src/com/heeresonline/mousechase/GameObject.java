package com.heeresonline.mousechase;

import android.graphics.PointF;

public abstract class GameObject {
  public static final String TAG = "GameObject";
  
  public int id = 0;
  public final PointF position;
  public float direction;
  public float speed = 1.0f;
  
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
   * Update the game object position.
   * @param deltaTime The elapsed time in milliseconds since the last step.
   */
  public abstract void step(float deltaTime);
}