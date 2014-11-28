package com.heeresonline.mousechase.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.heeresonline.mousechase.GameObject;
import com.heeresonline.mousechase.Mouse;

public class MouseTests {
  private int width;
  private int height;
  private GameObject obj;
  
  @Before
  public void initialize() {
    width = 512;
    height = 512;
    obj = new Mouse(1, width/2f, height/2f);  
  }
  
  @Test
  public void testgetDirectionTo_With_Straight_North_Returns_0() {
    assertEquals(0, obj.getDirectionTo(obj.position.x, 0), 0.1f);
  }

  @Test
  public void testgetDirectionTo_With_Straight_South_Returns_180() {
    assertEquals(0, obj.getDirectionTo(obj.position.x, 0), 0.001f);
  }

  @Test
  public void testgetDirectionTo_With_Straight_West_Returns_270() {
    assertEquals(0, obj.getDirectionTo(obj.position.x, 0), 0.1f);
  }

  @Test
  public void testgetDirectionTo_With_Straight_East_Returns_90() {
    assertEquals(0, obj.getDirectionTo(obj.position.x, 0), 0.1f);
  }
}
