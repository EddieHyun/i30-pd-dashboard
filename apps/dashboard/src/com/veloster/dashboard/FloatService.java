package com.veloster.dashboard;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.veloster.vehicle.FloatPanel;
import com.veloster.vehicle.VehicleDash;

/**
 * 플로팅 창: while the dashboard is not on screen (any other app: navi, home, media), keeps reading the car (the
 * VehicleDataClient it shares with the activity) and shows FloatPanel over everything. Started by DashboardActivity.onPause and
 * at boot when 설정 → 플로팅 → 플로팅 창 is on; stopped when the dashboard comes back. Looks 20 times a second, draws when something changed. A tap opens
 * the dashboard, a long press 내 앱, a drag moves it (remembered per shape, cell count and size). Steps aside while the rear camera shows. With
 * 플로팅 버튼 숨기기 on it asks 내 앱 to hide its edge button, again every 30 s while up (내 앱 brings the button back on
 * its own when that stops); on the way out it asks for the button back.
 */
public final class FloatService extends Service implements Runnable {
    static final String HUB="com.veloster.carplayhub",HIDE_ACTION="com.veloster.carplayhub.QUICK_ENTRY_HIDE";
    private static final long FRAME_MS=50,HIDE_REPEAT_MS=30000;   // 20 frames a second, like the race screen: the car sends every 40-80 ms
    /** A tick runs on the first vsync at least this long after the last one: every third frame at 60 Hz (50 ms), in step with the display
     *  instead of a 50 ms timer that drifts by the work it does. */
    private static final long TICK_MS=FRAME_MS-8;
    /** GPU drawing for the window, on by default since 2026-11-17 (our car: draw 5-10 ms -> 2.5-4 ms, worst 51 -> 12 ms, the same picture).
     *  Off while /sdcard/i30pd_float_sw exists (adb shell touch / rm), looked at every 2 s, so the two can still be compared without a new build. */
    private static final String SW_FLAG="i30pd_float_sw";
    private boolean hw=true,alive;private long hwCheckedAt;
    // ---- frame-rate measurement: one "FLOAT ..." line a second (tag VehicleDash), see i30pd_port FLOAT_FPS.md -------------------
    private int mTicks,mInval,mDraws;private long mDrawNs,mDrawMax,mTickNs,mTickMax,mGapMax,mLastTick,mLogAt;private boolean mHwCanvas;
    private final Choreographer.FrameCallback frame=new Choreographer.FrameCallback(){public void doFrame(long ns){
        if(!alive)return;
        if(android.os.SystemClock.elapsedRealtime()-mLastTick>=TICK_MS)run();
        Choreographer.getInstance().postFrameCallback(this);}};
    private final Handler handler=new Handler();
    private VehicleDataClient client;
    private WindowManager windows;
    private View panel;
    private WindowManager.LayoutParams layout;
    private int shown=-1;   // the key (shape, cells, size) of the window up, -1 none
    private long hideSentAt;private boolean hideSent;

    /** Starts the window if it is switched on (the dashboard just left the screen, or the head unit booted). */
    static void startIfOn(Context c){
        VehicleDash.loadFloat(c.getSharedPreferences("pit_vehicle_ui",0));
        if(DashboardActivity.resumed)return;   // boot's broadcast can come after the dashboard is already up
        if(FloatPanel.shape!=FloatPanel.OFF)try{wanted=c.startService(new Intent(c,FloatService.class))!=null;}catch(RuntimeException e){android.util.Log.w("VehicleDash","Floating window not started",e);}
    }
    /** The service is starting or up: it holds the shared car data client, so the dashboard leaves it running on its way out. */
    static volatile boolean wanted;
    static void stop(Context c){wanted=false;try{c.stopService(new Intent(c,FloatService.class));}catch(RuntimeException ignored){/* not running */}}

    public IBinder onBind(Intent intent){return null;}
    public void onCreate(){
        super.onCreate();
        windows=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        com.veloster.i18n.LanguageSettings.init(this);VehicleDash.fonts(this);wanted=true;client=VehicleDataClient.shared(this);client.start();   // already running when the dashboard handed it over: no gap, values kept
        alive=true;Choreographer.getInstance().postFrameCallback(frame);
    }
    public int onStartCommand(Intent intent,int flags,int startId){
        VehicleDash.loadFloat(prefs());
        if(FloatPanel.shape==FloatPanel.OFF||DashboardActivity.resumed){stopSelf();return START_NOT_STICKY;}   // also when it is restarted with the dashboard up
        return START_STICKY;
    }
    private SharedPreferences prefs(){return getSharedPreferences("pit_vehicle_ui",0);}

    public void run(){
        long t0=System.nanoTime(),now0=android.os.SystemClock.elapsedRealtime();
        if(mLastTick>0)mGapMax=Math.max(mGapMax,now0-mLastTick);mLastTick=now0;
        if(now0-hwCheckedAt>=2000){hwCheckedAt=now0;boolean want=!new java.io.File(android.os.Environment.getExternalStorageDirectory(),SW_FLAG).exists();if(want!=hw){hw=want;remove();}}   // a new window with the other renderer
        int want=VehicleDash.rearCamera()||FloatPanel.shape==FloatPanel.OFF?-1:FloatPanel.shape*100+FloatPanel.count*10+FloatPanel.size+(FloatPanel.round()?1000:0);   // the round gauge has square cells: its own window
        if(want!=shown){remove();if(want>=0)add(FloatPanel.shape,FloatPanel.count,FloatPanel.size);}
        boolean changed=FloatPanel.shape!=FloatPanel.OFF&&FloatPanel.tick(FloatPanel.count,FloatPanel.size,VehicleDash.floatSamples(FloatPanel.count),VehicleDash.profileNow(),android.os.SystemClock.elapsedRealtime());
        VehicleDash.chimeTick();
        if(panel!=null){if(changed){panel.invalidate();mInval++;}   // drawn only when it would look different: nothing to composite over the navi at a steady reading
            long now=android.os.SystemClock.elapsedRealtime();
            if(FloatPanel.hideButton&&(!hideSent||now-hideSentAt>=HIDE_REPEAT_MS)){hide(true);hideSentAt=now;}
            else if(!FloatPanel.hideButton&&hideSent)hide(false);}
        long dt=System.nanoTime()-t0;mTickNs+=dt;mTickMax=Math.max(mTickMax,dt);mTicks++;
        if(now0-mLogAt>=1000){
            if(mLogAt>0&&panel!=null)android.util.Log.i("VehicleDash",String.format(java.util.Locale.US,
                "FLOAT fps=%d ticks=%d redraw=%d%% draw_ms=%.1f/%.1f tick_ms=%.1f/%.1f gap_ms=%d hw=%d/%d shape=%d n=%d size=%d",
                mDraws,mTicks,mTicks==0?0:mInval*100/mTicks,mDraws==0?0f:mDrawNs/1e6f/mDraws,mDrawMax/1e6f,mTicks==0?0f:mTickNs/1e6f/mTicks,mTickMax/1e6f,mGapMax,
                hw?1:0,mHwCanvas?1:0,FloatPanel.shape,FloatPanel.count,FloatPanel.size));
            mLogAt=now0;mTicks=0;mInval=0;mDraws=0;mDrawNs=0;mDrawMax=0;mTickNs=0;mTickMax=0;mGapMax=0;}
    }
    private void add(final int shape,final int n,final int size){
        final int w=FloatPanel.width(shape,n,size),h=FloatPanel.height(shape,n,size);final boolean rnd=FloatPanel.round();final String at=shape+"_"+n+"_"+size+(rnd?"_r":"");
        layout=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|(hw?WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED:0),PixelFormat.TRANSLUCENT);   // x / y on the whole screen, not below the status bar (it sat 56 px low)
        layout.gravity=Gravity.TOP|Gravity.LEFT;
        layout.x=prefs().getInt("float_x_"+at,FloatPanel.defaultX(shape,n,size));layout.y=prefs().getInt("float_y_"+at,FloatPanel.defaultY(shape,n,size));
        final View v=new View(this){protected void onDraw(Canvas c){long t=System.nanoTime();
            FloatPanel.draw(c,shape,n,size,VehicleDash.floatSamples(n),VehicleDash.profileNow(),android.os.SystemClock.elapsedRealtime());
            long d=System.nanoTime()-t;mDraws++;mDrawNs+=d;mDrawMax=Math.max(mDrawMax,d);mHwCanvas=c.isHardwareAccelerated();}};
        v.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY;int startX,startY;boolean moved,held;
            final Runnable longPress=new Runnable(){public void run(){if(panel!=v)return;held=true;open(HUB,"com.veloster.carplayhub.HubActivity");}};
            public boolean onTouch(View view,MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();startX=layout.x;startY=layout.y;moved=false;held=false;handler.postDelayed(longPress,700);return true;
                    case MotionEvent.ACTION_MOVE:{float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
                        if(!moved&&Math.abs(dx)+Math.abs(dy)<12)return true;
                        moved=true;handler.removeCallbacks(longPress);
                        layout.x=Math.max(0,Math.min(800-w,startX+(int)dx));layout.y=Math.max(0,Math.min(480-h,startY+(int)dy));
                        try{windows.updateViewLayout(view,layout);}catch(RuntimeException ignored){/* removed meanwhile */}return true;}
                    case MotionEvent.ACTION_UP:handler.removeCallbacks(longPress);
                        if(moved)prefs().edit().putInt("float_x_"+at,layout.x).putInt("float_y_"+at,layout.y).apply();
                        else if(!held)open(getPackageName(),"com.veloster.dashboard.DashboardActivity");
                        return true;
                    case MotionEvent.ACTION_CANCEL:handler.removeCallbacks(longPress);return true;
                }
                return false;
            }
        });
        try{windows.addView(v,layout);panel=v;shown=shape*100+n*10+size+(rnd?1000:0);}
        catch(RuntimeException e){android.util.Log.w("VehicleDash","Floating window refused",e);panel=null;shown=-1;stopSelf();}
    }
    private void remove(){
        if(panel!=null){try{windows.removeView(panel);}catch(RuntimeException ignored){/* already gone */}panel=null;}
        shown=-1;
        if(hideSent)hide(false);
    }
    private void open(String pkg,String cls){
        try{startActivity(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(pkg,cls)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));}
        catch(RuntimeException e){android.util.Log.w("VehicleDash","Could not open "+cls,e);}
    }
    /** Asks 내 앱 to hide (or bring back) its edge button. Only 내 앱 receives it. */
    private void hide(boolean on){
        hideSent=on;
        try{sendBroadcast(new Intent(HIDE_ACTION).setPackage(HUB).putExtra("hide",on));}catch(RuntimeException ignored){/* 내 앱 not installed */}
    }
    public void onDestroy(){
        alive=false;Choreographer.getInstance().removeFrameCallback(frame);handler.removeCallbacks(this);remove();
        wanted=false;if(client!=null){if(!DashboardActivity.resumed)client.stop();client=null;}
        super.onDestroy();
    }
}
