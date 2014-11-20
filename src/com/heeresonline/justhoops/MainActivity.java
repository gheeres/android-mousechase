package com.heeresonline.justhoops;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
  private IView view;
  //private RenderView view;
  
  /* (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
                         WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    super.onCreate(savedInstanceState);

    view = new RenderView(this);
    //view = new GLRenderView(this);
    setContentView((View) view);
  }

  /* (non-Javadoc)
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    view.resume();
  }

  /* (non-Javadoc)
   * @see android.app.Activity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
    view.pause();
  }
}
