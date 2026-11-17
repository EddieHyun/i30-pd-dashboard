package com.veloster.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.veloster.vehicle.VehicleDash;
import com.veloster.i18n.LanguageSettings;

/** Own package, own task and own data subscriptions. Never starts the OEM NMode app. */
public final class DashboardActivity extends Activity {
    private VehicleDataClient client;
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        LanguageSettings.init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new FrameLayout(this));
        VehicleDash.attachStandalone(this);
        client=VehicleDataClient.shared(this);
    }
    // one car data client for the app (VehicleDataClient.shared): the floating window (FloatService) keeps it running while
    // the dashboard is not on screen, so nothing is cleared or asked twice on the way in or out
    static volatile boolean resumed;
    protected void onResume(){super.onResume();resumed=true;FloatService.stop(this);LanguageSettings.init(this);VehicleDash.resumeStandalone(this);client.start();}
    protected void onPause(){resumed=false;VehicleDash.pauseStandalone(this);FloatService.startIfOn(this);if(!FloatService.wanted)client.stop();super.onPause();}
    // Out of sight: with only the floating window up (it draws from FloatPanel, not the screens) the screens' bitmaps are
    // let go; also when Android says our UI is hidden or memory is short. Coming back draws them again once.
    protected void onStop(){super.onStop();android.util.Log.i("VehicleDash","dashboard stopped, floating "+(FloatService.wanted?"up":"off"));if(FloatService.wanted)VehicleDash.trimStandalone(this);}
    @Override public void onTrimMemory(int level){super.onTrimMemory(level);if(level>=TRIM_MEMORY_UI_HIDDEN&&!resumed)VehicleDash.trimStandalone(this);}
    protected void onDestroy(){if(client!=null&&!FloatService.wanted&&!resumed)client.stop();super.onDestroy();}
}
