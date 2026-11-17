package com.veloster.carplayhub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.veloster.i18n.UiText;

/**
 * 대시보드 앱 삭제 (before selling the car): puts ☆ back, turns the edge button off, removes the dashboard, then My Apps itself.
 * Two steps so a stray tap does nothing: a dialog that says what goes, then a red button held for HOLD_MS (letting go early
 * cancels). Removal goes through PackageManager.deletePackage (hidden; DELETE_PACKAGES is granted to our platform-signed app);
 * where that is refused, the system's own uninstall screen opens for each app instead.
 */
final class SelfRemove {
    static final String DASHBOARD="com.veloster.dashboard";
    static final long HOLD_MS=3000;
    private SelfRemove(){}

    /** Step 1: what will go. */
    static void ask(final Activity a){
        new AlertDialog.Builder(a).setTitle(UiText.t("대시보드 앱 삭제"))
            .setMessage(UiText.t("차를 팔거나 넘기기 전에 쓰는 기능입니다.\n\n삭제되는 것:\n· i30 계기판 앱과 그 설정 · 기록\n· 내 앱과 그 설정\n\n되돌리는 것:\n· ☆ 버튼을 원래 기능으로\n· 화면 가장자리 바로가기 버튼 끄기\n\n다시 쓰려면 설치기로 새로 설치하면 됩니다."))
            .setPositiveButton(UiText.t("계속"),new android.content.DialogInterface.OnClickListener(){public void onClick(android.content.DialogInterface d,int n){hold(a);}})
            .setNegativeButton(UiText.t("취소"),null).show();
    }

    /** Step 2: hold the red button for three seconds; the count shows, letting go early cancels. */
    private static void hold(final Activity a){
        final Handler h=new Handler();
        LinearLayout box=new LinearLayout(a);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(24,20,24,8);
        TextView note=Look.label(a,UiText.t("빨간 버튼을 3초 동안 누르고 있으면 삭제합니다. 중간에 떼면 취소됩니다."),16,Look.TXT,false);
        box.addView(note);
        final TextView red=Look.label(a,UiText.t("눌러서 삭제"),22,0xffffffff,true);
        red.setGravity(Gravity.CENTER);red.setBackgroundColor(Look.RED);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,110);p.topMargin=18;box.addView(red,p);
        final AlertDialog dialog=new AlertDialog.Builder(a).setTitle(UiText.t("대시보드 앱 삭제")).setView(box).setNegativeButton(UiText.t("취소"),null).create();
        final long[] down={0};
        final Runnable count=new Runnable(){public void run(){
            if(down[0]==0)return;
            long left=HOLD_MS-(SystemClock.elapsedRealtime()-down[0]);
            if(left<=0){down[0]=0;dialog.dismiss();remove(a);return;}
            red.setText(UiText.t("계속 누르세요")+"  "+(left/1000+1));h.postDelayed(this,100);
        }};
        red.setOnTouchListener(new View.OnTouchListener(){public boolean onTouch(View v,MotionEvent e){
            int act=e.getAction();
            if(act==MotionEvent.ACTION_DOWN){down[0]=SystemClock.elapsedRealtime();h.post(count);}
            else if(act==MotionEvent.ACTION_UP||act==MotionEvent.ACTION_CANCEL){if(down[0]!=0){down[0]=0;h.removeCallbacks(count);red.setText(UiText.t("눌러서 삭제"));}}
            return true;
        }});
        dialog.setOnDismissListener(new android.content.DialogInterface.OnDismissListener(){public void onDismiss(android.content.DialogInterface d){if(down[0]!=0){down[0]=0;}h.removeCallbacks(count);}});
        dialog.show();
    }

    /** Puts things back, removes the dashboard (waiting for it to go), then removes this app. */
    private static void remove(final Activity a){
        final Context c=a.getApplicationContext();
        final AlertDialog progress=new AlertDialog.Builder(a).setTitle(UiText.t("삭제하는 중")).setMessage(UiText.t("잠시 기다리세요. 끝나면 이 화면이 닫힙니다.")).setCancelable(false).create();
        progress.show();
        new Thread(new Runnable(){public void run(){
            try{StarKey.set(c,false);}catch(Exception e){Log.w("VNCarPlayHub","remove: star key not restored",e);}
            try{QuickEntryService.apply(c,QuickEntryPolicy.OFF);}catch(RuntimeException e){Log.w("VNCarPlayHub","remove: shortcut not turned off",e);}
            boolean silent=true;
            if(installed(c,DASHBOARD)){
                silent=delete(c,DASHBOARD);
                for(int i=0;silent&&i<100&&installed(c,DASHBOARD);i++)SystemClock.sleep(200);   // up to 20 s
            }
            final boolean ok=silent;
            a.runOnUiThread(new Runnable(){public void run(){
                if(ok&&delete(c,c.getPackageName()))return;   // the process ends as the package goes
                progress.dismiss();   // refused: the system's own uninstall screens, the dashboard first
                if(installed(c,DASHBOARD))uninstallScreen(a,DASHBOARD);
                uninstallScreen(a,c.getPackageName());
            }});
        }},"vn-self-remove").start();
    }

    static boolean installed(Context c,String pkg){
        try{c.getPackageManager().getPackageInfo(pkg,0);return true;}catch(PackageManager.NameNotFoundException e){return false;}
    }
    /** PackageManager.deletePackage(pkg, null observer, 0): false when the call itself is refused or missing. */
    private static boolean delete(Context c,String pkg){
        try{PackageManager pm=c.getPackageManager();
            pm.getClass().getMethod("deletePackage",String.class,Class.forName("android.content.pm.IPackageDeleteObserver"),int.class).invoke(pm,pkg,null,0);
            return true;}
        catch(Exception e){Log.w("VNCarPlayHub","remove: deletePackage refused for "+pkg,e);return false;}
        catch(LinkageError e){Log.w("VNCarPlayHub","remove: deletePackage missing",e);return false;}
    }
    private static void uninstallScreen(Activity a,String pkg){
        try{a.startActivity(new Intent(Intent.ACTION_DELETE,Uri.parse("package:"+pkg)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
        catch(RuntimeException e){Log.w("VNCarPlayHub","remove: uninstall screen unavailable",e);
            new AlertDialog.Builder(a).setMessage(UiText.t("이 내비에서는 앱이 스스로 삭제할 수 없습니다. PC 설치기나 휴대폰 설치 앱의 [삭제]를 쓰세요.")).setPositiveButton(UiText.t("확인"),null).show();}
    }
}
