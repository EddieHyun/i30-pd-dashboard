package com.veloster.vehicle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * 플로팅 창 레이아웃: rectangular windows with their own inside (짧은 캡슐, 주인공 + 목록, 단수 중심, 사선 분할, 반원 계기,
 * 변속등 머리) beside the grid shapes (카드, 아래 띠, 옆 세로). Geometry is in base px (size 보통) and scales with k. Colours
 * and type come from the skin: the plain window's theme colours, or a palette per skin (FloatSkin), so 계기판 따라 dresses
 * them too. The round gauge skin has no palette of its own and draws these plain.
 */
final class FloatLayouts extends ClusterKit {
    static final int PILL=5,HERO=6,GEAR=7,DIAG=8,DIAL=9,SHIFT=10;
    private static final FloatLayouts K=new FloatLayouts();
    protected void draw(Canvas c){}

    /** Cells shown: the pill has room for two. */
    static int used(int shape,int n){return shape==PILL?Math.min(n,2):n;}
    private static int rows(int n){return Math.min(3,n-1);}
    static float w(int shape,int n){
        switch(shape){case PILL:return 44+used(shape,n)*81;case HERO:return rows(n)>0?248:140;case DIAL:return rows(n)>0?248:132;case SHIFT:return n*70;default:return 248;}
    }
    static float h(int shape,int n){
        switch(shape){case PILL:return 40;case HERO:return 112;case GEAR:return 112;case DIAG:return n>=4?144:72;case DIAL:return 104;default:return 64;}
    }
    /** Cell i's rectangle in base px, or null when the layout does not show it. */
    static float[] rect(int shape,int n,int i){
        if(i>=used(shape,n))return null;float W=w(shape,n),H=h(shape,n);
        switch(shape){
            case PILL:return new float[]{38+i*81,0,38+(i+1)*81,H};
            case HERO:return i==0?new float[]{0,0,140,H}:new float[]{140,6+(i-1)*32,W,6+i*32};
            case DIAL:return i==0?new float[]{0,0,132,H}:new float[]{132,8+(i-1)*30,W,8+i*30};
            case GEAR:return new float[]{i%2==0?0:154,i<2?0:56,i%2==0?94:248,i<2?56:112};
            case DIAG:return new float[]{i%2==0?0:128,(i/2)*72,i%2==0?128:248,(i/2+1)*72};
            default:return new float[]{i*70,0,(i+1)*70,H};
        }
    }

    // ---- this frame's palette -------------------------------------------------------------------------------------
    private int bg,edge,div,txt,mut,track,acc,box,boxInk;private Typeface face,bold;private boolean plain,greenRed;
    private static Typeface chakra(){return LambdaCluster.FACE!=null?LambdaCluster.FACE:Typeface.DEFAULT;}
    private static Typeface chakraBold(){return LambdaCluster.FACE_BOLD!=null?LambdaCluster.FACE_BOLD:Typeface.DEFAULT_BOLD;}
    private void palette(int skin,float af){
        plain=false;greenRed=true;face=chakra();bold=chakraBold();box=0xffa8dcf2;boxInk=0xff061015;
        switch(skin){
            case FloatSkin.RACE:set(0xeb0e1316,0xff2a353c,0xfff2f6f8,0xff8fa0a8,0xff26323a,0xff5bc8f5);break;
            case FloatSkin.F1:set(0xed080a0c,0xff2a343b,0xffffffff,0xff8fa0a8,0xff1d2428,0xff2fe06a);box=0xffffffff;break;
            case FloatSkin.RETRO:set(0xf0060402,0xffb4500f,0xffffb347,0xffc46a1c,0xff2a1606,0xffff9a2e);face=Typeface.MONOSPACE;bold=Typeface.MONOSPACE;box=0xffb4500f;boxInk=0xff140800;greenRed=false;break;
            case FloatSkin.VFD:set(0xf0020a09,0x385ef2d2,0xff5ef2d2,0xb35ef2d2,0x1a5ef2d2,0xff5ef2d2);box=0xff5ef2d2;boxInk=0xff021210;greenRed=false;break;
            case FloatSkin.HUD:set(0x40000000,0xff62ff8a,0xff62ff8a,0xb362ff8a,0x3362ff8a,0xff62ff8a);face=Typeface.MONOSPACE;bold=Typeface.MONOSPACE;box=0xff62ff8a;boxInk=0xff031a0a;greenRed=false;break;
            case FloatSkin.PIXEL:set(0xf0000000,0xffffffff,0xffffffff,0xffffd84a,0xff2a2a2a,0xff3ddc5a);face=Typeface.MONOSPACE;bold=Typeface.MONOSPACE;box=0xffffffff;boxInk=0xff000000;break;
            case FloatSkin.VU:set(0xf2ece0bc,0xff3a3226,0xff14110c,0xff5a4e3a,0xffd8c89c,0xffc8302a);box=0xff3a3226;boxInk=0xffece0bc;greenRed=false;break;
            case FloatSkin.NIGHT:set(0xcc000000,0xff2b3134,0xffc5ccd0,0xff8a959a,0xff2b3134,0xff8a9499);face=Typeface.DEFAULT;bold=Typeface.DEFAULT;box=0xffc5ccd0;boxInk=0xff000000;greenRed=false;break;
            default:plain=true;bg=FloatPanel.BG;edge=FloatPanel.EDGE;div=FloatPanel.DIV;txt=FloatPanel.TXT;mut=FloatPanel.MUT;track=FloatPanel.TRACK;acc=FloatPanel.ACC;box=FloatPanel.themed(0xffa8dcf2);boxInk=FloatPanel.themed(0xff061015);break;
        }
        if(!plain){div=(edge&0xffffff)|0x99000000;int al=Math.max(0,Math.min(255,Math.round((bg>>>24)*af)));bg=al<<24|bg&0xffffff;}
    }
    private void set(int b,int e,int t,int m,int tr,int a){bg=b;edge=e;txt=t;mut=m;track=tr;acc=a;}
    private int warn(int lv,int normal){return lv==2?0xffff4b3c:lv==1?0xffffaa3c:normal;}

    private void body(Canvas c,float w,float h,float k,float rad){
        if(plain&&rad<0){FloatPanel.outline(c,w,h,k,bg,edge);return;}
        float rr=rad<0?6*k:rad;round(c,0,0,w,h,rr,bg);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(edge);r.set(.5f,.5f,w-.5f,h-.5f);c.drawRoundRect(r,rr,rr,p);p.setStyle(Paint.Style.FILL);
    }
    private void fit(Canvas c,String s,float x,float y,float size,float max,int col,Paint.Align al,Typeface tf){
        float w=width(s,size,tf);if(w>max&&w>0)size*=max/w;text(c,s,x,y,size,col,al,tf);}
    /** A value with its unit: LEFT starts at x, RIGHT ends (the unit) at x; the value shrinks to max px. */
    private void vu(Canvas c,FloatSkin.Cell d,float x,float y,float size,float max,float k,Paint.Align al){
        float us=Math.max(7*k,size*.36f),uw=d.unit.isEmpty()?0:width(d.unit,us,face)+3*k;
        float vs=size,vw=width(d.val,vs,bold);if(vw>max-uw&&vw>0){vs*=(max-uw)/vw;vw=max-uw;}
        int col=warn(d.lv,txt);
        if(al==Paint.Align.RIGHT){text(c,d.unit,x,y,us,mut,Paint.Align.RIGHT,face);text(c,d.val,x-uw,y,vs,col,Paint.Align.RIGHT,bold);}
        else{text(c,d.val,x,y,vs,col,Paint.Align.LEFT,bold);text(c,d.unit,x+vw+3*k,y,us,mut,Paint.Align.LEFT,face);}
    }
    private void bar(Canvas c,FloatSkin.Cell d,float x,float y,float w,float h,float k,boolean peak){
        fill(c,x,y,x+w,y+h,track);if(Float.isNaN(d.f)||!d.bar)return;float f=Math.max(0,Math.min(1,d.f)),z=d.zero,a=Math.min(z,f),b=Math.max(z,f);
        fill(c,x+w*a,y,Math.max(x+w*b,x+w*a+2),y+h,warn(d.lv,acc));
        if(peak&&d.pk!=null&&!Float.isNaN(d.pf)){float px=x+w*Math.max(0,Math.min(1,d.pf));fill(c,px-k,y-2*k,px+k,y+h+2*k,plain?0xffffffff:txt);}
    }
    private int led(int i,int n){float t=(i+.5f)/n;if(greenRed)return t<.45f?0xff2fe06a:t<.8f?0xffff3b3b:0xff4aa8ff;return t<.6f?acc:t<.85f?0xffffaa3c:0xffff4b3c;}

    /** Draws layout shape at 0,0 with cells d[0..], the rpm fraction and the gear's text. */
    static void draw(Canvas c,int shape,int n,float k,int skin,float af,FloatSkin.Cell[] d,float rpm,String gear){K.drawImpl(c,shape,n,k,skin,af,d,rpm,gear);}
    private void drawImpl(Canvas c,int shape,int n,float k,int skin,float af,FloatSkin.Cell[] d,float rpm,String gear){
        palette(skin==FloatSkin.ROUND_GAUGE?FloatSkin.PLAIN:skin,af);
        float W=w(shape,n)*k,H=h(shape,n)*k;int u=used(shape,n);boolean rOk=!Float.isNaN(rpm);float rf=rOk?Math.max(0,Math.min(1,rpm)):0;
        switch(shape){
        case PILL:{   // a short pill: gear in a ring that fills with rpm, then up to two values
            body(c,W,H,k,H/2);float cx=20*k,cy=20*k;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3*k);p.setColor(track);c.drawCircle(cx,cy,14*k,p);
            if(rOk){r.set(cx-14*k,cy-14*k,cx+14*k,cy+14*k);p.setColor(rf>.85f?0xffff4b3c:acc);p.setStrokeCap(Paint.Cap.ROUND);c.drawArc(r,-90,Math.max(1,360*rf),false,p);p.setStrokeCap(Paint.Cap.BUTT);}
            p.setStyle(Paint.Style.FILL);fit(c,gear,cx,cy+6*k,17*k,22*k,txt,Paint.Align.CENTER,bold);
            for(int i=0;i<u;i++){float x=(44+i*81)*k;if(i>0)fill(c,x-7*k,9*k,x-6*k,31*k,div);
                fit(c,d[i].name,x,14*k,7.5f*k,72*k,mut,Paint.Align.LEFT,Typeface.DEFAULT);vu(c,d[i],x,33*k,16*k,72*k,k,Paint.Align.LEFT);}
            break;}
        case HERO:case DIAL:{   // one big reading on the left (a bar, or a half dial with a needle), the next three as rows
            body(c,W,H,k,-1);FloatSkin.Cell a=d[0];boolean dial=shape==DIAL;float lw=(dial?132:140)*k;
            fit(c,a.name,(dial?10:12)*k,(dial?18:20)*k,(dial?9:10)*k,lw*.55f,mut,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
            if(a.pk!=null)text(c,"▲ "+a.pk,lw-12*k,(dial?18:20)*k,8.5f*k,mut,Paint.Align.RIGHT,face);
            if(!dial){vu(c,a,12*k,66*k,34*k,116*k,k,Paint.Align.LEFT);bar(c,a,12*k,84*k,116*k,6*k,k,true);}
            else{float cx=66*k,cy=78*k,rad=50*k;r.set(cx-rad,cy-rad,cx+rad,cy+rad);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6*k);p.setColor(track);c.drawArc(r,180,180,false,p);
                boolean has=!Float.isNaN(a.f);float f=has?Math.max(0,Math.min(1,a.f)):0;
                if(has){float lo=Math.min(a.zero,f),hi=Math.max(a.zero,f);p.setColor(warn(a.lv,acc));c.drawArc(r,180+180*lo,Math.max(1,180*(hi-lo)),false,p);}
                p.setStyle(Paint.Style.FILL);
                for(int i=0;i<=10;i++){double an=Math.toRadians(180+18*i);float r1=rad-9*k,r2=rad-(i%5==0?15:12)*k;line(c,cx+(float)Math.cos(an)*r1,cy+(float)Math.sin(an)*r1,cx+(float)Math.cos(an)*r2,cy+(float)Math.sin(an)*r2,k,mut);}
                if(a.pk!=null&&!Float.isNaN(a.pf)){double an=Math.toRadians(180+180*Math.max(0,Math.min(1,a.pf)));line(c,cx+(float)Math.cos(an)*(rad-4*k),cy+(float)Math.sin(an)*(rad-4*k),cx+(float)Math.cos(an)*(rad+4*k),cy+(float)Math.sin(an)*(rad+4*k),2*k,plain?0xffffffff:txt);}
                if(has){double an=Math.toRadians(180+180*f);p.setStrokeCap(Paint.Cap.ROUND);line(c,cx,cy,cx+(float)Math.cos(an)*(rad-6*k),cy+(float)Math.sin(an)*(rad-6*k),2*k,0xffff6b5b);p.setStrokeCap(Paint.Cap.BUTT);}
                dot(c,cx,cy,4*k,0xffff6b5b);fit(c,a.val,cx,cy+18*k,15*k,100*k,warn(a.lv,txt),Paint.Align.CENTER,bold);}
            int rows=rows(n);if(rows>0)fill(c,lw,10*k,lw+1,H-10*k,div);
            for(int i=1;i<=rows;i++){FloatSkin.Cell b=d[i];float y=(dial?10+(i-1)*30:12+(i-1)*32)*k,x=lw+10*k,rr=W-(dial?10:12)*k;
                fit(c,b.name,x,y+(dial?12:10)*k,(dial?8:7.5f)*k,rr-x,mut,Paint.Align.LEFT,Typeface.DEFAULT);
                String v=b.val+(b.unit.isEmpty()?"":" "+b.unit);fit(c,v,rr,y+(dial?24:12)*k,(dial?14:13)*k,(rr-x)*(dial?1:.6f),warn(b.lv,txt),Paint.Align.RIGHT,bold);
                if(!dial){FloatSkin.Cell bb=b;bar(c,bb,x,y+18*k,rr-x,2*k,k,false);}}
            break;}
        case GEAR:{   // the race screen's gear box in the middle, an rpm line over it, a reading in each corner
            body(c,W,H,k,-1);fill(c,94*k,18*k,154*k,96*k,box);fit(c,gear,124*k,78*k,58*k,54*k,boxInk,Paint.Align.CENTER,bold);
            fill(c,94*k,10*k,154*k,13*k,track);if(rOk)fill(c,94*k,10*k,94*k+60*k*rf,13*k,rf>.85f?0xffff4b3c:acc);
            for(int i=0;i<Math.min(4,n);i++){boolean left=i%2==0;float x=(left?10:238)*k,y=(i<2?10:62)*k;Paint.Align al=left?Paint.Align.LEFT:Paint.Align.RIGHT;
                fit(c,d[i].name,x,y+11*k,8*k,76*k,mut,al,Typeface.DEFAULT);vu(c,d[i],x,y+34*k,20*k,78*k,k,al);}
            break;}
        case DIAG:{   // the N logo's slant splits each band: a big reading left, the next one right
            body(c,W,H,k,-1);
            for(int b=0;b*2<n&&b<2;b++){float y=b*72*k;FloatSkin.Cell a=d[b*2];
                if(b>0)fill(c,8*k,y,W-8*k,y+1,div);
                fit(c,a.name,12*k,y+18*k,9*k,60*k,mut,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);if(a.pk!=null)text(c,"▲ "+a.pk,112*k,y+18*k,8*k,mut,Paint.Align.RIGHT,face);
                vu(c,a,12*k,y+52*k,28*k,100*k,k,Paint.Align.LEFT);bar(c,a,12*k,y+60*k,96*k,3*k,k,true);
                if(b*2+1<n){FloatSkin.Cell e=d[b*2+1];line(c,138*k,y+8*k,118*k,y+64*k,2*k,acc);
                    fit(c,e.name,236*k,y+18*k,9*k,80*k,mut,Paint.Align.RIGHT,Typeface.DEFAULT_BOLD);vu(c,e,236*k,y+52*k,26*k,86*k,k,Paint.Align.RIGHT);bar(c,e,150*k,y+60*k,86*k,3*k,k,true);}}
            break;}
        default:{   // SHIFT: a shift-light strip the window's width, the readings in a row under it
            body(c,W,H,k,-1);int leds=n*5;float lw=(W-16*k)/leds;
            for(int i=0;i<leds;i++){boolean on=rOk&&(i+.5f)/leds<rf;fill(c,8*k+i*lw+k,6*k,8*k+(i+1)*lw-k,10*k,on?led(i,leds):track);}
            for(int i=0;i<n;i++){float x=i*70*k;if(i>0)fill(c,x,18*k,x+1,56*k,div);
                fit(c,d[i].name,x+10*k,28*k,8*k,54*k,mut,Paint.Align.LEFT,Typeface.DEFAULT);vu(c,d[i],x+10*k,52*k,17*k,54*k,k,Paint.Align.LEFT);}
            break;}
        }
    }
}
