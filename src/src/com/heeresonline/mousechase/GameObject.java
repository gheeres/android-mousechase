package com.heeresonline.mousechase;

import java.util.Iterator;
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
  public float size = 64.0f;
  public float paddingPercentage = 0.1f;

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
    return((distance - ((this.size + size) / 2)) <= 0);
  }

  /**
   * Get's the distance between the points.
   * @param x1 The x position of the first position
   * @param y1 The y position of the first position
   * @param x2 The x position of the second position
   * @param y2 The y position of the second position
   * @return The distance between the positions.
   */
  public static float getDistance(float x1, float y1, float x2, float y2) {
    float deltaX = x1 - x2;
    float deltaY = y1 - y2;
    return((float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
  }
  
  /**
   * Get's the distance from the specified x,y coordinate.
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
   * Gets the size / radius of the item for collision calculations taking into
   * account the padding for the item.
   * @return
   */
  public float getCollisionRadius() {
    return(size / 2 - (size * paddingPercentage));
  }
  
  /**
   * Checks to see if the x,y coordinate collides with any of the specified game objects.
   * @param x The x coordinate to inspect.
   * @param y The y coordinate to inspect.
   * @param size The size/radius of the item.
   * @param paddingPercentage The padding (or buffer) percentage around the item.
   * @param objects The game objects.
   * @return True if collision, false if otherwise
   */
  public static boolean collidesWith(float x, float y, float size, float paddingPercentage, GameObject obj) {
    if (obj == null) return(false);

    float distance = getDistance(x, y, obj.position.x, obj.position.y);
    return((distance - (obj.getCollisionRadius() + ((size / 2) - (size * paddingPercentage)))) <= 0);
  }

  /**
   * Checks to see if the x,y coordinate collides with the specified game object.
   * @param size The size/radius of the item.
   * @param objects The game objects.
   * @return True if collision, false if otherwise
   */
  public boolean collidesWith(float x, float y, GameObject obj) {
    return(collidesWith(x, y, size, paddingPercentage, obj));
  }

  /**
   * Checks to see if the current position collides with any of the specified game object.
   * @param obj The game objects.
   * @return True if collision, false if otherwise
   */
  public boolean collidesWith(GameObject obj) {
    return(collidesWith(position.x, position.y, size, paddingPercentage, obj));
  }

  /**
   * Checks to see if the object collides with any of the specified game objects.
   * @param objects The game objects.
   * @param filter The filter / type of game object to check for.
   * @return The game object collided with
   */
  public GameObject collidesWith(Iterable<GameObject> objects, Class<?> filter) {
    return(collidesWith(position.x, position.y, size, paddingPercentage, objects, filter));
  }

  /**
   * Checks to see if the object collides with any of the specified game objects.
   * @param x The x coordinate to inspect.
   * @param y The y coordinate to inspect.
   * @param objects The game objects.
   * @param filter The filter / type of game object to check for.
   * @return The game object collided with
   */
  public GameObject collidesWith(float x, float y, Iterable<GameObject> objects, Class<?> filter) {
    return(collidesWith(x, y, size, paddingPercentage, objects, filter));
  }

  /**
   * Checks to see if the object collides with any of the specified game objects.
   * @param x The x coordinate to inspect.
   * @param y The y coordinate to inspect.
   * @param size The size/radius of the item.
   * @param paddingPercentage The padding (or buffer) around the item.
   * @param objects The game objects.
   * @param filter The filter / type of game object to check for.
   * @return The game object collided with
   */
  public static GameObject collidesWith(float x, float y, float size, float paddingPercentage, Iterable<GameObject> objects, Class<?> filter) {
    Iterator<GameObject> iterator = objects.iterator();
    while (iterator.hasNext()) {
      GameObject obj = iterator.next();
      if (obj.getClass() == filter) {
        if (collidesWith(x, y, size, paddingPercentage, obj)) {
          return(obj);
        }
      }
    }
    return(null);
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
   * @param objects The existing game objects.
   */
  public abstract void step(float deltaTime, Iterable<GameObject> objects);
}