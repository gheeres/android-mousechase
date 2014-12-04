package com.heeresonline.mousechase;

public class Barrier extends GameObject {
  public static final String TAG = "Barrier";
  
  public Barrier(int id, float x, float y) {
    super(id, x, y);
  }

  @Override
  public void step(float deltaTime, Iterable<GameObject> objects) {
  }
}