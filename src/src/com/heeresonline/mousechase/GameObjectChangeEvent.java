package com.heeresonline.mousechase;

/**
 * Represents a mouse change event.
 */
public interface GameObjectChangeEvent {
  /**
   * Indicates that a game object has been added.
   * @param obj The newly added game object.
   */
  public void added(GameObject obj);
}