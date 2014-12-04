package com.heeresonline.mousechase;

/**
 * Represents a mouse change event.
 */
public interface GameObjectChangeEvent {
  /**
   * Indicates that a game object has been removed.
   * @param obj The removed game object.
   */
  public void removed(GameObject obj);
  
  /**
   * Indicates that a game object has been added.
   * @param obj The newly added game object.
   */
  public void added(GameObject obj);

  /**
   * Indicates that all of the game objects have been cleared.
   */
  public void cleared();
}