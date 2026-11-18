package com.veloster.vehicle;

import java.util.Random;

/**
 * 앞차 피하기 on the Arcade screen while the car is parked: speed 0 in P (a 6MT: the park brake), the brake held for the
 * countdown (3 2 1, released = called off), then the road runs on its own and the steering wheel dodges the cars ahead.
 * The wheel counts to ±90° (past that it is the same); the brake pedal slows down once it has been let go after GO. Three
 * hits end it; holding the brake again starts over. Any roll or leaving P drops straight back to the plain Arcade view.
 * Positions are in the Arcade scene's scroll units (the road bands; about 1.5 of them from the camera to the player's car),
 * sideways in half road widths (lanes at -0.66, 0, 0.66). No Android: host-tested (ArcadeGameTest).
 */
final class ArcadeGame {
    static final int IDLE=0,COUNT=1,PLAY=2,OVER=3;
    static final long COUNT_MS=3000,OVER_MS=1500;
    /** Scroll units a second at full speed (shown as TOP_KMH; the plain view scrolls 0.06 a second per km/h). */
    static final float RATE=10.8f,TOP_KMH=180,LANE=0.66f;
    /** Where the player's car sits ahead of the camera, in scroll units: a car there in the same lane is a hit. */
    static final float NEAR=0.9f,FAR=2.3f,HIT_X=0.42f;
    static final int CARS=10;
    int state=IDLE,hp;
    long at;   // when the state began
    float pos,x,speed,dist,curve,target,segLeft,best;
    boolean brakeArmed;   // the brake counts as a brake (it was let go after GO)
    final float[] carM=new float[CARS],carV=new float[CARS];final int[] carLane=new int[CARS],carCol=new int[CARS];
    long hitAt=Long.MIN_VALUE/2;
    private final Random rnd;
    ArcadeGame(long seed){rnd=new Random(seed);}

    boolean on(){return state==PLAY||state==OVER;}
    /** The countdown's number now (3, 2, 1), or 0. */
    int count(long now){return state==COUNT?(int)Math.max(1,3-(now-at)/1000):0;}
    float kmh(){return speed*TOP_KMH;}

    /** Every frame. parked: speed 0 in P / park brake; braking: the brake switch or pressure; bar: pressure (bar);
     *  wheel: steering in degrees, right positive. */
    void update(long now,float dt,boolean parked,boolean braking,float bar,float wheel){
        if(!parked){state=IDLE;return;}
        switch(state){
            case IDLE:if(braking){state=COUNT;at=now;}return;
            case COUNT:if(!braking){state=IDLE;return;}if(now-at>=COUNT_MS)start(now);return;
            case OVER:if(!braking)brakeArmed=true;if(now-at>=OVER_MS&&braking&&brakeArmed){state=COUNT;at=now;}return;   // let go, then hold again
            default:break;
        }
        if(!braking)brakeArmed=true;
        float steer=Float.isNaN(wheel)?0:Math.max(-1,Math.min(1,wheel/90f));
        float top=0.7f+Math.min(0.3f,dist*0.15f),b=brakeArmed&&!Float.isNaN(bar)&&braking?Math.max(0,bar):0;
        speed+=(b>2?-1.6f*Math.min(100,b)/100:0.35f)*dt;
        if(Math.abs(x)>1&&speed>0.3f)speed-=0.9f*dt;   // off the road
        speed=Math.max(0,Math.min(top,speed));
        segLeft-=speed*RATE*dt;if(segLeft<=0){segLeft=30+rnd.nextFloat()*50;target=rnd.nextFloat()<0.3f?0:rnd.nextFloat()*2-1;}
        curve+=(target-curve)*Math.min(1,dt*0.8f);
        x+=steer*dt*2.2f*Math.max(0.25f,speed);x-=curve*0.8f*speed*speed*dt;x=Math.max(-2.2f,Math.min(2.2f,x));
        pos+=speed*RATE*dt;dist+=speed*TOP_KMH/3600*dt;
        for(int i=0;i<CARS;i++){carM[i]+=carV[i]*RATE*dt;if(carM[i]<pos-3)place(i,pos+40+rnd.nextFloat()*60);
            float d=carM[i]-pos;
            if(d>NEAR&&d<FAR&&Math.abs(x-(carLane[i]-1)*LANE)<HIT_X&&now-hitAt>1200){hitAt=now;hp--;speed=carV[i]*0.6f;
                if(hp<=0){state=OVER;at=now;brakeArmed=false;if(dist>best)best=dist;}}}
    }
    private void start(long now){
        state=PLAY;at=now;hp=3;pos=0;x=0;speed=0;dist=0;curve=0;target=0;segLeft=20;brakeArmed=false;hitAt=Long.MIN_VALUE/2;
        for(int i=0;i<CARS;i++)place(i,12+i*9+rnd.nextFloat()*4);
    }
    private void place(int i,float m){carM[i]=m;carLane[i]=rnd.nextInt(3);carV[i]=0.35f+rnd.nextFloat()*0.25f;carCol[i]=rnd.nextInt(4);}
}
