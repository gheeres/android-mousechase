package com.heeresonline.justhoops;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Sprite {
  public int x;
  public int y;
  public Bitmap bitmap;
  
  public Sprite(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public Sprite(Resources res, int id) {
    this(BitmapFactory.decodeResource(res, id));
  }

  public Sprite(int x, int y, Resources res, int id) {
    this(x, y, BitmapFactory.decodeResource(res, id));
  }

  public Sprite(int x, int y, Bitmap bitmap) {
    this(bitmap);
    setPosition(x, y);
  }
  
  public int width() {
    if (bitmap == null) return(0);
    return(bitmap.getWidth());
  }

  public int height() {
    if (bitmap == null) return(0);
    return(bitmap.getHeight());
  }
  
  public Sprite setPosition(int x, int y) {
    this.x = x;
    this.y = y;
    return(this);
  }
}
