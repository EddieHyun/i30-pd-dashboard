package com.veloster.vehicle;

/** 앞차 피하기 on the Arcade screen: when it starts, stops and ends. */
public final class ArcadeGameTest {
    private static int checks;
    private static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    /** Runs ms at 20 ms steps with fixed inputs; returns the time after. */
    private static long run(ArcadeGame g,long t,long ms,boolean parked,boolean braking,float bar,float wheel){
        for(long e=t+ms;t<e;t+=20)g.update(t,0.02f,parked,braking,bar,wheel);return t;}
    public static void main(String[] a){
        ArcadeGame g=new ArcadeGame(1);long t=1000;
        t=run(g,t,500,false,true,30,0);check(g.state==ArcadeGame.IDLE,"rolling: no countdown");
        t=run(g,t,1000,true,true,30,0);check(g.state==ArcadeGame.COUNT&&g.count(t)==2,"parked, brake held: counting, 2 after a second");
        t=run(g,t,500,true,false,0,0);check(g.state==ArcadeGame.IDLE,"brake let go: called off");
        t=run(g,t,3100,true,true,30,0);check(g.state==ArcadeGame.PLAY&&g.hp==3,"held three seconds: playing");
        t=run(g,t,2000,true,true,30,0);check(g.speed>0.5f,"the brake held from the countdown does not slow it: "+g.speed);
        t=run(g,t,500,true,false,0,0);float before=g.speed;t=run(g,t,500,true,true,80,0);check(g.speed<before-0.3f,"let go and pressed again: it brakes");
        t=run(g,t,100,true,false,0,0);
        // steering past 90 is the same as 90, and right moves right
        ArcadeGame r=new ArcadeGame(2),r2=new ArcadeGame(2);long u=0;
        u=run(r,u,3100,true,true,30,0);run(r2,0,3100,true,true,30,0);
        run(r,u,800,true,false,0,90);run(r2,u,800,true,false,0,170);check(r.x>0.3f&&Math.abs(r.x-r2.x)<1e-4f,"right 90 = right 170: "+r.x+" "+r2.x);
        // three hits end it; moving drops it
        ArcadeGame h=new ArcadeGame(3);long v=0;v=run(h,v,3100,true,true,30,0);
        for(int k=0;k<3&&h.state==ArcadeGame.PLAY;k++){for(int i=0;i<ArcadeGame.CARS;i++){h.carM[i]=h.pos+50+i;}h.carM[0]=h.pos+1.5f;h.carLane[0]=1;h.carV[0]=0;h.x=0;v=run(h,v,40,true,false,0,0);v+=1300;}
        check(h.state==ArcadeGame.OVER&&h.best>0,"three hits: over, the distance kept as best");
        v=run(h,v,2000,true,false,0,0);v=run(h,v,100,true,true,30,0);check(h.state==ArcadeGame.COUNT,"over: let go and hold again counts down");
        v=run(h,v,40,false,true,30,0);check(h.state==ArcadeGame.IDLE&&!h.on(),"rolling: straight back to the plain view");
        System.out.println("PASS "+checks+" arcade game checks");
    }
}
