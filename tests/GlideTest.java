import com.veloster.vehicle.Glide;

public final class GlideTest {
    private static int checks;
    private static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    private static boolean near(float a,float b){return Math.abs(a-b)<.01f;}
    public static void main(String[] args){
        Glide g=new Glide();
        check(near(g.value(1000,1000,1040),1000),"one sample: no trend yet");
        check(near(g.value(1100,1080,1080),1100),"at the sample: the sample");
        check(near(g.value(1100,1080,1120),1150),"half an interval on: half a step ahead");
        check(near(g.value(1100,1080,1160),1200),"one interval on: one step ahead");
        check(near(g.value(1100,1080,1240),1200),"no further than one step");
        check(near(g.value(1100,1080,1290),1100),"no new sample for 2.5 intervals: the value stopped changing");
        check(near(g.value(1090,1500,1540),1090),"after a long gap there is no trend");
        g.reset();g.value(6000,0,0);
        check(near(g.value(4000,80,120),4000),"a gear change (25000 rpm/s) is not a trend");
        check(near(g.value(3900,160,200),3850),"falling: ahead downwards");
        g.reset();
        check(near(g.value(2000,500,540),2000),"after a reset: one sample again");
        System.out.println("PASS "+checks+" glide checks");
    }
}
