package com.heeresonline.justhoops;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RenderView extends SurfaceView implements Runnable {
  private static final String TAG = "RenderView";
  private static final float  PIXELS_TO_METERS = 100f;
  private final static int    MAX_FPS = 50; //desired fps   
  private final static int    MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int    FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private Context context;
  private SurfaceHolder holder;
  private Thread thread;
  private volatile boolean isRunning = false;
  
  private Paint paint;
  private World world;
  private Body body;

  class Sprite {
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
      
      this.x = x;
      this.y = y;
    }
    
    public int width() {
      return(bitmap.getWidth());
    }

    public int height() {
      return(bitmap.getHeight());
    }
  }
  
  public RenderView(Context context) {
    super(context);
    this.context = context;
    
    holder = getHolder();
    paint = new Paint();
  }

  protected void initialize(int width, int height) {    
    Log.d(TAG, String.format("Display area: %dx%d", width, height));

    Sprite sprite = new Sprite(context.getResources(), R.drawable.ic_launcher);
    world = new World(new Vec2(0, 0.1f));

    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    //bodyDef.setPosition(new Vec2(width/2 / PIXELS_TO_METERS, height/2 / PIXELS_TO_METERS));
    bodyDef.setPosition(new Vec2(width/2 / PIXELS_TO_METERS, 0));
    body = world.createBody(bodyDef);
    body.setUserData(sprite.bitmap);
    
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(sprite.width()/2 / PIXELS_TO_METERS, sprite.height()/2 / PIXELS_TO_METERS);
    
    body.createFixture(shape, 0.1f);
  }
  
  @Override
  public void run() {
    int sleepTime = 0;
    long startTime = System.nanoTime();

    while (isRunning) {
      if (! holder.getSurface().isValid()) continue;

      int framesSkipped = 0;
      float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
      startTime = System.nanoTime();
      Log.d(TAG, String.format("DeltaTime: %f", deltaTime));

      Canvas canvas = holder.lockCanvas();
      if (world == null) {
        initialize(canvas.getWidth(), canvas.getHeight());
      }
      update(deltaTime);
      render(canvas, deltaTime);
      holder.unlockCanvasAndPost(canvas);
      
      // Constant framerate / speed
      sleepTime = (int) (FRAME_PERIOD - deltaTime);
      if (sleepTime > 0) {
        try {
          thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
      }
      while ((sleepTime < 0) && (framesSkipped < MAX_FRAME_SKIPS)) {
        Log.d(TAG, String.format("%d / %d frame(s) skipped. Behind: %d msec", 
                                 framesSkipped+1, MAX_FRAME_SKIPS, sleepTime));
        update(sleepTime);
        sleepTime += FRAME_PERIOD;
        framesSkipped++;
      }
    }
  }

  protected void update(float deltaTime) {
    // Calculate Physics
    world.step(1f/deltaTime, 6, 2);
  }
  
  protected void render(Canvas canvas, float deltaTime) {
    canvas.drawRGB(0, 0, 0);
    
    canvas.drawBitmap((Bitmap) body.getUserData(), 
                      body.getPosition().x * PIXELS_TO_METERS, 
                      body.getPosition().y * PIXELS_TO_METERS, paint);
  }
  
  public void resume() {
    isRunning = true;
    thread = new Thread(this);
    thread.start();
  }
  
  public void pause() {
    isRunning = false;
    while (true) {
      try {
        thread.join();         
      }
      catch(InterruptedException e) {
      }
    }
  }
}
