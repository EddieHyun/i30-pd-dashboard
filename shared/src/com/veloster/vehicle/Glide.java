package com.veloster.vehicle;

/**
 * Display only: where a signal is by now, between its samples. The head unit hands the rpm on about every 80 ms, so a
 * needle drawn from the samples alone moves 12 times a second however often the screen is drawn. The last two samples
 * give the trend; the value follows it for one sample interval at most (the lead of the engine sound, EngineSynth).
 */
public final class Glide {
    static final float MAX_SLOPE=5;        // per ms: past 5000 rpm/s it is a gear change or a clutch, not a trend
    static final long MIN_GAP=20,MAX_GAP=300;   // ms between two samples that make a trend
    private float last=Float.NaN,slope;
    private long at,interval;

    /** v arrived at `arrived` (ms); the value to show at `now`. */
    public float value(float v,long arrived,long now){
        if(Float.isNaN(last)||arrived!=at){
            long gap=arrived-at;
            slope=!Float.isNaN(last)&&gap>=MIN_GAP&&gap<=MAX_GAP?(v-last)/gap:0;
            if(Math.abs(slope)>MAX_SLOPE)slope=0;
            interval=gap;last=v;at=arrived;
        }
        long ahead=now-at;
        // a value that stops changing sends no new sample: past 2.5 intervals the trend is over
        return slope==0||ahead<=0||ahead>2.5f*interval?v:v+slope*Math.min(ahead,interval);
    }
    public void reset(){last=Float.NaN;slope=0;}

    private static final Glide RPM=new Glide();
    /** The rpm of a frame's snapshot, moved to where the engine is by now. One dashboard, one state. */
    static void rpm(Telemetry.Sample[] s,long now){
        Telemetry.Sample r=s[0];
        if(!r.valid||r.ageMs==Long.MAX_VALUE){RPM.reset();return;}
        s[0]=new Telemetry.Sample(RPM.value(r.value,now-r.ageMs,now),true,r.changed,r.seen,r.stale,r.ageMs);
    }
}
