package com.veloster.vehicle;

import android.os.IBinder;
import android.os.Parcel;

/**
 * tm_hscan_service (telematics ITM_HSCANService, com.infobank) read-only poller. Asks a few getters the hscan
 * broadcast does not carry — the engine warning lamp (activeMILonStatus) and the run / idle / drive seconds since
 * start — and feeds them to a sink while the gate is open (ignition on). READ ONLY: no set / request (climate,
 * charging) call is ever made. Only a bluelink head unit registers the service; where it is absent the poller backs
 * off and the signals stay empty. A binder service of its own, separate from the micom diagnostic channel ObdLive and
 * DtcReader share, so it does not contend with a trouble-code read.
 *
 * Values confirmed on the car 2026-11-19 (i30pd_port TmScan): MIL 0, run / idle / drive 985 / 969 / 16 s. The getter
 * transaction codes come from the firmware's ITM_HSCANService (static analysis): activeMILOnStatus 0x5 (int),
 * getCarInfoEnginRunTime 0xd, getCarInfoEnginIdleTime 0xc, getCarInfoEnginDriveTime 0xa (long seconds).
 */
public final class TmHscan {
    public interface Sink { void value(int sig,float v,boolean ok); }
    public interface Gate { boolean open(); }

    private static final String NAME="tm_hscan_service";
    private static final String IFACE="com.hkmc.telematics.hscan.tm_interface.ITM_HSCANService";
    /** signal, transaction code, 1 = the reply is a long (seconds), 0 = an int. */
    private static final int[][] GETTERS={
        {Telemetry.MIL,0x5,0},
        {Telemetry.ENGINE_RUNTIME,0xd,1},
        {Telemetry.ENGINE_IDLE,0xc,1},
        {Telemetry.ENGINE_DRIVE,0xa,1}};
    private static final long PERIOD=3000,ABSENT=30000;   // poll every 3 s; retry a missing service every 30 s

    private volatile boolean running;
    private Thread thread;

    public void start(final Sink sink,final Gate gate){
        if(running)return;running=true;
        thread=new Thread(new Runnable(){public void run(){loop(sink,gate);}},"tm-hscan-poll");
        thread.setDaemon(true);thread.start();
    }
    public void stop(){running=false;Thread t=thread;if(t!=null)t.interrupt();}

    private void loop(Sink sink,Gate gate){
        IBinder svc=null;boolean warned=false;
        while(running){
            if(!gate.open()){nap(PERIOD);continue;}
            if(svc==null||!svc.isBinderAlive()){
                try{svc=(IBinder)Class.forName("android.os.ServiceManager").getMethod("getService",String.class).invoke(null,NAME);}
                catch(Throwable t){svc=null;}
                if(svc==null){if(!warned){warned=true;android.util.Log.i("VehicleDash","tm_hscan_service not present");}nap(ABSENT);continue;}
                warned=false;
            }
            for(int[] g:GETTERS){
                if(!running)break;
                Parcel in=Parcel.obtain(),out=Parcel.obtain();
                try{
                    in.writeInterfaceToken(IFACE);
                    svc.transact(g[1],in,out,0);
                    out.readException();
                    float v=g[2]==1?(float)out.readLong():out.readInt();
                    sink.value(g[0],v,true);
                }catch(Throwable t){
                    if(t instanceof android.os.DeadObjectException)svc=null;   // rebind next round; a permission or absent reply leaves that one signal empty
                }finally{in.recycle();out.recycle();}
            }
            nap(PERIOD);
        }
    }
    private void nap(long ms){try{Thread.sleep(ms);}catch(InterruptedException e){}}
}
