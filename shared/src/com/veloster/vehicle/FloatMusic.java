package com.veloster.vehicle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Music on the floating window (FloatPanel): the 음악 띠 under any shape (play state, title · artist, time, a thin progress
 * line) and the 음악 shape (album art, title, artist, progress, then cell 0 large with the gear and cell 1 small). The track is
 * the OEM widget broadcasts' (MediaState) that VehicleDash and FloatService hand over before each tick and draw, in the
 * window's plain colours. Long titles are cut with an ellipsis.
 * ponytail: no scrolling title, it would redraw the window every frame over the navi; a marquee if a cut title proves a nuisance.
 */
final class FloatMusic {
    /** The strip's height under the body and the gap above it, the music shape's size, at size 보통. */
    static final int STRIP_H=30,GAP=4,W=262,H=118;
    static volatile MediaState.Snapshot track;
    static volatile Bitmap art;
    private static final Paint p=new Paint(3);private static final RectF r=new RectF();

    private FloatMusic(){}
    static boolean playing(MediaState.Snapshot t){return t!=null&&(t.title.length()>0||t.artist.length()>0);}
    /** Where the track is now: the last reported position, run on with the clock while it plays. */
    static long position(MediaState.Snapshot t){return Math.min(t.durationMs,t.positionMs+(t.playState==1?Math.max(0,t.timeAgeMs):0));}
    static float progress(MediaState.Snapshot t){return t!=null&&t.hasProgress()?Math.max(0,Math.min(1,position(t)/(float)t.durationMs)):0;}
    /** What the music part shows, for FloatPanel.tick's change hash: the texts, play state, the second and the art. */
    static int key(){MediaState.Snapshot t=track;if(t==null)return 0;
        int h=t.title.hashCode()*31+t.artist.hashCode();h=h*31+t.playState;h=h*31+(t.hasProgress()?(int)(position(t)/1000):-1);
        Bitmap a=art;return h*31+(a==null?0:System.identityHashCode(a));}
    /** The cells of the music shape (size 보통): cell 0 the large value under the progress line, cell 1 the small one right of it. */
    static float[] rect(int n,int i){return n<2?new float[]{118,62,W,H}:i==0?new float[]{118,62,200,H}:new float[]{200,62,W,H};}

    /** The strip at 0,y, w wide (the body's width), for size k. */
    static void strip(Canvas c,float w,float y,float k){
        MediaState.Snapshot t=track;boolean on=playing(t);float h=STRIP_H*k;
        c.save();c.translate(0,y);
        FloatPanel.outline(c,w,h,k,FloatPanel.BG,FloatPanel.EDGE);
        if(on)glyph(c,10*k,h/2-k,5*k,t.playState==1,FloatPanel.ACC);else text(c,"♪",12*k,h/2+4*k,12*k,FloatPanel.MUT,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);
        String time=on&&t.hasProgress()&&w>=200*k?MediaState.time(position(t))+" / "+MediaState.time(t.durationMs):"";   // a narrow window keeps its width for the title
        float tw=time.isEmpty()?0:width(time,10*k,Typeface.DEFAULT)+8*k;
        if(!time.isEmpty())text(c,time,w-9*k,h/2+3*k,10*k,FloatPanel.MUT,Paint.Align.RIGHT,Typeface.DEFAULT);
        String line=!on?com.veloster.i18n.UiText.t("재생 중인 곡 없음"):t.artist.isEmpty()?t.title:t.title.isEmpty()?t.artist:t.title+"  ·  "+t.artist;
        text(c,fit(line,12*k,Typeface.DEFAULT_BOLD,w-tw-32*k),24*k,h/2+3*k,12*k,on?FloatPanel.TXT:FloatPanel.MUT,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
        bar(c,10*k,h-5*k,w-10*k,2.5f*k,progress(t));
        c.restore();
    }
    /** The music shape at 0,0 for size k: n cells (1 or 2) of signals sigs, slots from first; rec feeds the peaks and line graphs. */
    static void shape(Canvas c,float k,int n,Telemetry.Sample[] q,int[] sigs,int first,boolean rec,long now){
        MediaState.Snapshot t=track;boolean on=playing(t);float w=W*k,h=H*k;
        FloatPanel.outline(c,w,h,k,FloatPanel.BG,FloatPanel.EDGE);
        float a=h-20*k,x=a+20*k,right=w-10*k;Bitmap b=art;
        r.set(10*k,10*k,10*k+a,10*k+a);
        if(b!=null&&!b.isRecycled())c.drawBitmap(b,null,r,p);   // Paint(3): filtered
        else{p.setStyle(Paint.Style.FILL);p.setColor(FloatPanel.TRACK);c.drawRoundRect(r,8*k,8*k,p);text(c,"♪",10*k+a/2,10*k+a*.68f,a*.5f,FloatPanel.ACC,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);}
        if(on){text(c,fit(t.title.isEmpty()?t.artist:t.title,15*k,Typeface.DEFAULT_BOLD,right-x),x,27*k,15*k,FloatPanel.TXT,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
            if(!t.title.isEmpty())text(c,fit(t.artist,11*k,Typeface.DEFAULT,right-x),x,42*k,11*k,FloatPanel.MUT,Paint.Align.LEFT,Typeface.DEFAULT);}
        else text(c,fit(com.veloster.i18n.UiText.t("재생 중인 곡 없음"),13*k,Typeface.DEFAULT_BOLD,right-x),x,30*k,13*k,FloatPanel.MUT,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
        bar(c,x,50*k,right,3*k,progress(t));
        p.setColor(FloatPanel.DIV);c.drawRect(x,62*k,right,62*k+1,p);
        // the right column first (the gear, and the small value under it), so the large value knows the room it has left of it
        Telemetry.Sample g=q[8];String gear=g!=null&&g.valid?Tiles.text(8,g.value):"-";   // the gear, in the accent, above the small value
        text(c,gear,right,(n<2?h-12*k:h-30*k),18*k,FloatPanel.ACC,Paint.Align.RIGHT,Typeface.DEFAULT_BOLD);
        float side=width(gear,18*k,Typeface.DEFAULT_BOLD);
        for(int j=Math.min(n,2)-1;j>=0;j--){int sig=sigs[j];float v=FloatPanel.value(q,sig);if(rec)FloatPanel.record(first+j,sig,v,now);
            int f=FloatPanel.fmt(sig),lv=Tiles.level(f,v),col=lv==2?0xffff4b3c:lv==1?0xffffaa3c:FloatPanel.TXT;
            String val=Tiles.text(f,v),unit=Telemetry.UNITS[f]==null?"":Telemetry.UNITS[f];
            if(j==0){text(c,FloatPanel.label(sig),x,76*k,10*k,FloatPanel.MUT,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
                // 26 down to 14 until value and unit end 8 short of the right column: -0.65 bar beside 1561 rpm ran into it
                float uw=unit.isEmpty()?0:3*k+width(unit,10*k,Typeface.DEFAULT),room=right-side-8*k-x,size=26*k;
                while(size>14*k&&width(val,size,Typeface.DEFAULT_BOLD)+uw>room)size-=k;
                float vw=width(val,size,Typeface.DEFAULT_BOLD);text(c,val,x,h-12*k,size,Float.isNaN(v)?FloatPanel.MUT:col,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
                text(c,unit,x+vw+3*k,h-12*k,10*k,FloatPanel.MUT,Paint.Align.LEFT,Typeface.DEFAULT);}
            else{String s=fit(val+(unit.isEmpty()?"":" "+unit),11*k,Typeface.DEFAULT,62*k);side=Math.max(side,width(s,11*k,Typeface.DEFAULT));
                text(c,s,right,h-12*k,11*k,Float.isNaN(v)?FloatPanel.MUT:col,Paint.Align.RIGHT,Typeface.DEFAULT);}}
    }
    private static void bar(Canvas c,float l,float t,float rt,float h,float f){p.setStyle(Paint.Style.FILL);p.setColor(FloatPanel.TRACK);r.set(l,t,rt,t+h);c.drawRoundRect(r,h/2,h/2,p);
        if(f>0){p.setColor(FloatPanel.ACC);r.set(l,t,l+(rt-l)*f,t+h);c.drawRoundRect(r,h/2,h/2,p);}}
    /** ▶ while it plays, ❚❚ otherwise. */
    private static void glyph(Canvas c,float x,float y,float s,boolean play,int col){p.setStyle(Paint.Style.FILL);p.setColor(col);
        if(play){android.graphics.Path a=new android.graphics.Path();a.moveTo(x,y-s);a.lineTo(x+s*1.6f,y);a.lineTo(x,y+s);a.close();c.drawPath(a,p);}
        else{c.drawRect(x,y-s,x+s*.55f,y+s,p);c.drawRect(x+s*.95f,y-s,x+s*1.5f,y+s,p);}}
    /** s, or as much of it as fits in max followed by an ellipsis. */
    static String fit(String s,float size,Typeface f,float max){if(max<=0)return "";if(width(s,size,f)<=max)return s;
        int lo=0,hi=s.length();while(lo<hi){int m=(lo+hi+1)/2;if(width(s.substring(0,m)+"…",size,f)<=max)lo=m;else hi=m-1;}
        return lo==0?"…":s.substring(0,lo).trim()+"…";}
    private static void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align a,Typeface face){
        p.setStyle(Paint.Style.FILL);p.setTypeface(face);p.setTextSize(size);p.setTextAlign(a);p.setColor(color);c.drawText(s,x,y,p);}
    private static float width(String s,float size,Typeface face){p.setTypeface(face);p.setTextSize(size);return p.measureText(s);}
}
