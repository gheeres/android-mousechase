package com.heeresonline.justhoops;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

public class PhysicsWorld {
  private final static int    WORLD_ENTITY = 0x01;
  private final static int    PHYSICS_ENTITY = 0x01 << 1;
  
  private List<Body> bodies;
  private AABB worldAABB;
  private World world;
  private BodyDef groundBodyDef;
  //private PolygonDef boxShapeDef;
  
  public PhysicsWorld(int width, int height) {

    create(width, height);  
  }
  
  public Iterable<Body> getBodies() {
    return(bodies);
  }
  
  public void create(int width, int height) 
  {
    bodies = new ArrayList<Body>();

    world = new World(new Vec2(0.0f, (float)10.0f));
    world.setAllowSleep(true);

    // The Ground
    groundBodyDef = new BodyDef();
    groundBodyDef.position.set(new Vec2(0.0f, -10.0f));
    Body groundBody = world.createBody(groundBodyDef);
    PolygonShape groundBox = new PolygonShape();
    groundBox.setAsBox(50.0f, 10.0f);
    groundBody.createFixture(groundBox, 0.0f);
    
    // Our (Square) Ball 
    BodyDef ballDef = new BodyDef();
    ballDef.type = BodyType.DYNAMIC;
    ballDef.position.set(4.0f, 4.0f);
    Body ballBody = world.createBody(ballDef);
    PolygonShape ballBox = new PolygonShape();
    ballBox.setAsBox(1.0f, 1.0f);
    FixtureDef ballFixtureDef = new FixtureDef();
    ballFixtureDef.shape = ballBox;
    ballFixtureDef.density = 1.0f;
    ballFixtureDef.friction = 0.3f;
    ballBody.createFixture(ballFixtureDef);
    bodies.add(ballBody);
    
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
  
  public void step(float deltaTime) {
    world.step(1f/60f, 6, 2);
  }
}
