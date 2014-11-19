package com.heeresonline.justhoops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.util.Log;

public class PhysicsWorld {
  private final static String TAG = "PhysicsWorld";
  
  private final static int WORLD_ENTITY = 0x01;
  private final static int PHYSICS_ENTITY = 0x01 << 1;
  
//  private Vec2 upperLeft;
//  private Vec2 lowerRight;
//  private Vec2 upperRight;
//  private Vec2 lowerLeft;
  
  private List<Body> bodies;
  //private AABB worldAABB;
  private World world;
  private Body ground;
  //private Body box;
  
  private float minX;
  private float minY;
  private float maxX;
  private float maxY;
  
  public PhysicsWorld(float minX, float minY, float maxX, float maxY) {
    Log.d(TAG, String.format("Bounds: X(%5.2f / %5.2f), Y(%5.2f / %5.2f)",
                             minX, maxX, minY, maxX));
 
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
    
    create();
  }
 
  private void setBoundingBox(Vec2 upperLeft, Vec2 lowerRight) {
//    Vec2 upperRight = new Vec2(lowerRight.x, upperLeft.y);
//    Vec2 lowerLeft = new Vec2(upperLeft.x, lowerRight.y);
//    
//    // Set up walls body definitions
//    BodyDef leftDef = new BodyDef();
//    leftDef.position.set(lowerLeft);
//    BodyDef rightDef = new BodyDef();
//    rightDef.position.set(lowerRight);
//    BodyDef topDef = new BodyDef();
//    topDef.position.set(upperLeft);
//    BodyDef bottomDef = new BodyDef();
//    bottomDef.position.set(lowerLeft);
//
//    EdgeShape topEdge = new EdgeShape();
//    topEdge.set(upperLeft, upperRight);
//    EdgeShape bottomEdge = new EdgeShape();
//    bottomEdge.set(lowerLeft, lowerRight);
//    EdgeShape leftEdge = new EdgeShape();
//    leftEdge.set(upperLeft, lowerLeft);
//    EdgeShape rightEdge = new EdgeShape();
//    rightEdge.set(upperRight, lowerRight);
//
//    box = world.createBody(bottomDef);
//    box.createFixture(topEdge, 0.0f);
//    box.createFixture(bottomEdge, 0.0f);
//    box.createFixture(leftEdge, 0.0f);
//    box.createFixture(rightEdge, 0.0f);
  }
  
  public void create() 
  {
    create(new Vec2(0.0f, -9.8f));
  }
  
  public void create(Vec2 gravity) 
  {
    bodies = new CopyOnWriteArrayList<Body>();

    world = new World(gravity);
    world.setAllowSleep(true);
  
//    setBoundingBox(upperLeft, lowerRight);

//    // The Ground
//    BodyDef groundBodyDef = new BodyDef();
//    groundBodyDef.position.set(new Vec2(0.0f, -10.0f));
//    ground = world.createBody(groundBodyDef);
//    PolygonShape groundBox = new PolygonShape();
//    groundBox.setAsBox(50.0f, 10.0f);
//    ground.createFixture(groundBox, 0.0f);

    BodyDef groundDef = new BodyDef();
    groundDef.position.set(0.0f, 0.0f);
    EdgeShape groundShape = new EdgeShape();
    groundShape.set(new Vec2(minX, minY), new Vec2(maxX, minY));
    ground = world.createBody(groundDef);
    ground.createFixture(groundShape, 0.0f);
    

//    // Our (Square) Ball 
//    BodyDef ballDef = new BodyDef();
//    ballDef.type = BodyType.DYNAMIC;
//    ballDef.position.set(getCenterX(), getCenterY());
//    Body ballBody = world.createBody(ballDef);
//    PolygonShape ballBox = new PolygonShape();
//    ballBox.setAsBox(1.0f, 1.0f);
//    FixtureDef ballFixtureDef = new FixtureDef();
//    ballFixtureDef.shape = ballBox;
//    ballFixtureDef.density = 1.0f;
//    ballFixtureDef.friction = 0.3f;
//    ballFixtureDef.restitution = 0.8f;
//    ballBody.createFixture(ballFixtureDef);
//    bodies.add(ballBody);
    
    //Body groundBody = world.createDynamicBody(groundBodyDef);
    //boxShapeDef = new PolygonDef();
    //boxShapeDef.setAsBox((float) 50.0, (float) 10.0);
    //groundBody.createShape(boxShapeDef);
//    
//    Sprite sprite = new Sprite(context.getResources(), R.drawable.ic_launcher);
//    sprite.setPosition((width / 2) - (sprite.bitmap.getWidth() / 2), 0);
//    
//    world = new World(new Vec2(0, 0.1f));
//    float worldWidth = (width / PIXELS_TO_METERS);
//    float worldHeight = (height / PIXELS_TO_METERS);
//
//    // Ball
//    BodyDef bodyDef = new BodyDef();
//    bodyDef.type = BodyType.DYNAMIC;
//    bodyDef.position.set((sprite.x + sprite.width()/2) / PIXELS_TO_METERS, 
//                         (sprite.y + sprite.height()/2) / PIXELS_TO_METERS);
//    body = world.createBody(bodyDef);
//    body.setUserData(sprite.bitmap);
//    PolygonShape shape = new PolygonShape();
//    shape.setAsBox(sprite.width()/2 / PIXELS_TO_METERS, sprite.height()/2 / PIXELS_TO_METERS);
//    FixtureDef fixtureDef = new FixtureDef();
//    fixtureDef.shape = shape;
//    fixtureDef.density = 0.1f;
//    fixtureDef.restitution = 0.5f;
//    fixtureDef.filter.categoryBits = PHYSICS_ENTITY;
//    fixtureDef.filter.maskBits = WORLD_ENTITY;
//    body.createFixture(fixtureDef);
//    
//    // Edge
//    BodyDef floorDef = new BodyDef();
//    floorDef.type = BodyType.STATIC;
//    floorDef.position.set(new Vec2(0, 0));
//    EdgeShape edgeShape = new EdgeShape();
//    float worldFloor = (worldHeight-(50/PIXELS_TO_METERS))/2;
//    edgeShape.set(new Vec2(-worldWidth/2, -worldFloor),
//                  new Vec2(worldWidth/2, -worldFloor));
//    FixtureDef floorFixture = new FixtureDef();
//    floorFixture.shape = edgeShape;
//    floorFixture.filter.categoryBits = WORLD_ENTITY;
//    floorFixture.filter.maskBits = PHYSICS_ENTITY;
//    Body edgeScreen = world.createBody(floorDef);
//    edgeScreen.createFixture(floorFixture);
  }
  
  public void addBox(Vec2 pos, Vec2 size) {
    addBox(pos.x, pos.y, size.x, size.y);
  }
  public void addBox(Vec2 pos, float width, float height) {
    addBox(pos.x, pos.y, width, height);
  }
  public void addBox(float x, float y, float width, float height) {
    // Our (Square) Ball 
    BodyDef ballDef = new BodyDef();
    ballDef.type = BodyType.DYNAMIC;
    ballDef.position.set(x, y);
    Body ballBody = world.createBody(ballDef);
    if (ballBody != null) {
      PolygonShape ballBox = new PolygonShape();
      ballBox.setAsBox(width/2f, height/2f);
      FixtureDef ballFixtureDef = new FixtureDef();
      ballFixtureDef.shape = ballBox;
      ballFixtureDef.density = 1.0f;
      ballFixtureDef.friction = 0.3f;
      ballFixtureDef.restitution = 0.8f;
      ballBody.createFixture(ballFixtureDef);
      synchronized(bodies) {
        bodies.add(ballBody);
      }
    }
  }
  
  public void step(float deltaTime) {
    world.step(1f/60f, 6, 2);

    int index = 0;
    for(Iterator<Body> iterator = getBodies().iterator(); iterator.hasNext(); index++) {
      Body body = iterator.next();
      Vec2 position = body.getPosition();
      float angle = body.getAngle();
      if ((position.x < (minX - ((maxX - minX)*2))) || (position.x > (maxX + ((maxX - minX)*2)))) {
        Log.d(TAG, String.format("[%d] Removing body at x: %4.2f, y: %4.2f, angle: %4.2f\n", 
                                 index, position.x, position.y, angle));
        bodies.remove(index);
      }
      else Log.v(TAG, String.format("[%d] x: %4.2f, y: %4.2f, angle: %4.2f\n", 
                                    index, position.x, position.y, angle));
    }
  }

  public void dispose() {
    world.destroyBody(ground);
    //world.destroyBody(box);

    for(Iterator<Body> iterator = getBodies().iterator(); iterator.hasNext();) {
      Body body = iterator.next();
      iterator.remove();
      world.destroyBody(body);
    }
  }
  
  public Vec2 getCenter() {
    return(new Vec2(getCenterX(), getCenterY()));
  }

  public float getCenterX() {
    return(minX + (maxX - minX) / 2);
  }

  public float getCenterY() {
    return(minY + (maxY - minY) / 2);
  }

  public Iterable<Body> getBodies() {
    if (bodies == null) bodies = new CopyOnWriteArrayList<Body>();
    return(bodies);
  }
}
