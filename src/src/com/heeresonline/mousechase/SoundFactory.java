package com.heeresonline.mousechase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;

/**
 * Factory for loading / accessing sounds assets.
 */
public class SoundFactory {
  public static final String TAG = "SoundFactory";

  public static final Map<String, Sound> sounds = new HashMap<String, Sound>();

  /**
   * Loads the specified sound.
   * @param pool The sound pool to load the sound into.
   * @param name The name of the sound asset.
   * @param assets The asset manager to use.
   * @param filename The name of the sound to load.
   * @return The id of the sound. If equal 0, then sound load failed.
   */
  public static int loadSound(SoundPool pool, String name, Context context, int resourceId) {
    int id = pool.load(context, resourceId, 1);
    if (id > 0) {
      sounds.put(name, new Sound(pool, id));
      return(id);
    }
    return(0);
  }
  
  /**
   * Loads the specified sound.
   * @param pool The sound pool to load the sound into.
   * @param name The name of the sound asset.
   * @param assets The asset manager to use.
   * @param filename The name of the sound to load.
   * @return The id of the sound. If equal 0, then sound load failed.
   */
  public static int loadSound(SoundPool pool, String name, AssetManager assets, String filename) {
    AssetFileDescriptor afd = null;
    try {
      if ((afd = assets.openFd(filename)) != null) {
        int id = pool.load(afd, 1);
        if (id > 0) {
          sounds.put(name, new Sound(pool, id));
          return(id);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (afd != null) {
          afd.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return(0);
  }
  
  /**
   * Represents information about a sound
   */
  public static class Sound {
    public final SoundPool pool;
    public final int id;
    private int streamId;
    
    /**
     * Creates an instance of the Sound class.
     * @param pool The pool that holds the specified sound.
     * @param id The unique id of for the sound.
     */
    public Sound(SoundPool pool, int id) {
      this.id = id;
      this.pool = pool;
    }
    
    /**
     * Sets the loop for the sound.
     * @param loop loop mode (0 = no loop, -1 = loop forever, x = number of loops)
     */
    public void setLoop(int loop) {
      pool.setLoop(streamId, loop);
    }

    /**
     * Sets the volume for the specified sound.
     * @param left left volume value (range = 0.0 to 1.0)
     * @param right right volume value (range = 0.0 to 1.0)
     */
    public void setVolume(float leftVolume, float rightVolume) {
      pool.setVolume(streamId, leftVolume, rightVolume);
    }

    /**
     * Plays the specified sound. 
     * @returns The stream id for the sound to control volume, etc.
     */
    public int play() {
      return(play(1.0f, 1.0f, 0));
    }

    /**
     * Plays the specified sound. 
     * @param loop loop mode (0 = no loop, -1 = loop forever, x = number of loops)
     * @returns The stream id for the sound to control volume, etc.
     */
    public int play(int loop) {
      return(play(1.0f, 1.0f, loop));
    }

    /**
     * Plays the specified sound. 
     * @param left left volume value (range = 0.0 to 1.0)
     * @param right right volume value (range = 0.0 to 1.0)
     * @returns The stream id for the sound to control volume, etc.
     */
    public int play(float leftVolume, float rightVolume) {
      return(play(leftVolume, rightVolume, 0));
    }

    /**
     * Plays the specified sound. 
     * @param left left volume value (range = 0.0 to 1.0)
     * @param right right volume value (range = 0.0 to 1.0)
     * @param loop loop mode (0 = no loop, -1 = loop forever, x = number of loops)
     * @returns The stream id for the sound to control volume, etc.
     */
    public int play(float leftVolume, float rightVolume, int loop) {
      return(streamId = pool.play(id, leftVolume, rightVolume, 1, loop, 1.0f));
    }

    /**
     * Stops the specified sound from playing.
     */
    public void stop() {
      pool.stop(id);
    }
  }
}
