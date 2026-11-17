package com.veloster.vehicle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * 플로팅 창: a small see-through window of readings that the dashboard keeps over every other screen (navi, home, media)
 * while it is not on screen itself. Chosen in 설정 → 플로팅: the shape (카드, 아래 띠, 옆 세로), how many cells (1, 2 or 4)
 * and the size; each cell a short name, the value and a thin bar that follows it (from zero for a value that goes
 * negative, like boost). Off by default. The dashboard app's FloatService owns the window; this draws it and holds the
 * choices. Host-testable.
 */
public final class FloatPanel {
    /** ROUND: 원형 게이지, a disc per reading laid out like the card, in the theme's colours (no 색감). */
    public static final int OFF=0,CARD=1,STRIP=2,SIDE=3,ROUND=4;
    /** The rectangular layouts with their own inside (FloatLayouts). */
    public static final int PILL=FloatLayouts.PILL,HERO=FloatLayouts.HERO,GEARBOX=FloatLayouts.GEAR,DIAG=FloatLayouts.DIAG,DIAL=FloatLayouts.DIAL,SHIFT=FloatLayouts.SHIFT,TICK=FloatLayouts.TICK,FILLS=FloatLayouts.FILLS,BARS=FloatLayouts.BARS,TWIN=FloatLayouts.TWIN,WEDGE=FloatLayouts.WEDGE,TOWER=FloatLayouts.TOWER;
    public static final String[] SHAPE_NAMES={"끔","카드","가로 띠","세로 띠","원형 게이지","캡슐","큰 값 + 목록","단수 중심","사선","계기 + 목록","변속등","얇은 띠","가로 막대","세로 막대","계기 묶음","RPM 막대","세로 단수"};
    /** The 모양 sheet's order. */
    public static final int[] FORMS={CARD,STRIP,SIDE,TICK, ROUND,DIAL,TWIN,BARS, HERO,FILLS,DIAG,PILL, GEARBOX,TOWER,SHIFT,WEDGE};   // rows: grids, gauges, lists, rpm and gear
    public static final int LAST_SHAPE=FloatLayouts.LAST;
    public static boolean layout(int s){return s>=PILL&&s<=FloatLayouts.LAST;}
    /** The cells' graph choice is used: the plain grid window only (skins and layouts have their own bars). */
    public static boolean graphsUsed(){int s=shape==OFF?lastShape:shape;return skinNow()==0&&!layout(s)&&s!=ROUND;}
    /** The cell counts shape s fills without a hole; the settings offer these only. */
    public static int[] counts(int s){return layout(s)?FloatLayouts.counts(s):COUNTS;}
    public static boolean fits(int s,int n){for(int v:counts(s))if(v==n)return true;return false;}
    /** n, or the next count up that shape s takes (its largest when there is none). */
    public static int fitCount(int s,int n){int[] cs=counts(s);for(int v:cs)if(v>=n)return v;return cs[cs.length-1];}
    /** Cells the shape shows of n (the short pill has room for two). */
    public static int used(int s,int n){return layout(s)?FloatLayouts.used(s,n):n;}
    public static final int[] COUNTS={1,2,4};
    public static final String[] SIZE_NAMES={"작게","보통","크게","아주 크게"};
    public static final float[] SIZES={.85f,1,1.5f,2};
    /** A cell at size 보통, px. */
    static final int CELL_W=124,CELL_H=64;
    /** A round gauge (원형 게이지) at size 보통: a disc this wide and high, laid out like the card (2 across). */
    static final int GAUGE=108;
    /** What a cell can show, in the order a tap on its settings row steps through, with the short name it shows. */
    public static final int[] CHOICES={Telemetry.BOOST,2,Telemetry.INTAKE,Telemetry.BAT_VOLTAGE,9,7,0,Telemetry.AMBIENT,Telemetry.FUEL_LEVEL,
        Telemetry.ENGINE_LOAD,10,Telemetry.CLUTCH_ODD,Telemetry.CLUTCH_EVEN,Telemetry.DPF_SOOT,Telemetry.DPF_TEMP};
    public static final String[] CHOICE_NAMES={"부스트","냉각수","흡기","배터리","페달","속도","RPM","외기온","연료","엔진 부하","브레이크","클러치 홀수","클러치 짝수","DPF 포집","DPF 온도"};
    /** -1 = automatic: boost, coolant, intake, battery; a diesel shows its DPF soot in the third. */
    public static final int[] AUTO_PETROL={Telemetry.BOOST,2,Telemetry.INTAKE,Telemetry.BAT_VOLTAGE},AUTO_DIESEL={Telemetry.BOOST,2,Telemetry.DPF_SOOT,Telemetry.BAT_VOLTAGE};
    public static volatile int shape=OFF,count=4,size=1;
    /** The shape the 켬 switch brings back (the window was switched off from it). */
    public static volatile int lastShape=CARD;
    public static volatile boolean hideButton=true;
    public static final int[] cells={-1,-1,-1,-1};
    /** Each cell's graph: a bar under the value, the last 30 s as a line, a row of lit segments (LED), or the number alone. */
    public static final int BAR=0,LINE=1,LED=2,NUM=3;
    public static final String[] GRAPH_NAMES={"막대","선 그래프","칸 막대","숫자만"};
    /** The window's corners: the theme's (N: two cut corners, hex themes: four, BMW / Rose: round), or a fixed one. */
    public static final int CORNER_THEME=0,CORNER_SQUARE=1,CORNER_ROUND=2,CORNER_CUT=3;
    public static final String[] CORNER_NAMES={"테마","직각","둥글게","사선 컷"};
    /** Colours: the theme's (accent, text, lines), the N blue whatever the theme, or white. Warnings stay amber / red. */
    public static final int COLOR_THEME=0,COLOR_BLUE=1,COLOR_WHITE=2;
    public static final String[] COLOR_NAMES={"테마","파랑","흰색"};
    public static volatile int corner=CORNER_THEME,color=COLOR_THEME;
    /** 투명도: how solid the window's body is over the screen behind (text, bars and lines stay solid). */
    public static final String[] ALPHA_NAMES={"진하게","보통","연하게"};
    static final int[] ALPHAS={0xf4,0xd8,0x80};
    public static volatile int alpha=1;
    /** 스킨: the cells drawn like a dashboard layout (FloatSkin.NAMES); 0 = the plain window with its corners and colours;
     *  AUTO (계기판 따라, the default) = the skin of the dashboard screen last shown (screen, a ClusterPicker index). */
    public static final int AUTO=-1;
    public static volatile int screen=0;
    /** The skin drawn now. */
    public static int skinNow(){int s=skin;return s<0?FloatSkin.forScreen(screen):Math.min(s,FloatSkin.NAMES.length-1);}
    public static final String[] SKIN_NAMES=FloatSkin.NAMES;
    public static volatile int skin=AUTO;
    public static int skinCount(){return FloatSkin.NAMES.length;}
    /** The round gauge shape is on: square cells, a disc each, no window body. */
    public static boolean round(){return round(shape==OFF?lastShape:shape);}
    public static boolean round(int s){return s==ROUND;}
    public static final int[] graphs={BAR,BAR,BAR,BAR};
    /** The line graph's memory: per cell the last HIST samples, one every HIST_MS (30 s); cleared when the cell's signal changes. */
    static final int HIST=60;static final long HIST_MS=500,HIST_GAP_MS=5000;
    private static final float[][] hist=new float[4][HIST];private static final int[] histHead=new int[4],histN=new int[4],histSig={-1,-1,-1,-1};
    private static final long[] histAt=new long[4];
    /** The highest reading per cell (최고값), for the readings where a peak means something (boost, rpm, speed, pedal,
     *  temperatures, G); G and yaw keep the largest size either way. It starts over when the cell's reading changes, or
     *  when the reading has been missing for PEAK_IDLE_MS (the car was off). */
    static final long PEAK_IDLE_MS=600000;
    private static final float[] peak={Float.NaN,Float.NaN,Float.NaN,Float.NaN};private static final int[] peakSig={-1,-1,-1,-1};private static final long[] peakAt=new long[4];
    public static boolean peaks(int s){
        switch(s){case 0:case 1:case 2:case 7:case 9:case 10:case 11:case 12:case Telemetry.BOOST:case Telemetry.INTAKE:case Telemetry.ENGINE_LOAD:
            case Telemetry.THROTTLE_POS:case Telemetry.FUEL_RATE:case Telemetry.YAW:case Telemetry.STEERING_SPEED:case Telemetry.CLUTCH_ODD:case Telemetry.CLUTCH_EVEN:
            case Telemetry.CAT_TEMP:case Telemetry.DPF_TEMP:case Telemetry.EGT_CAT:case Telemetry.TIRE_TEMP_FL:case Telemetry.TIRE_TEMP_FR:case Telemetry.TIRE_TEMP_RL:case Telemetry.TIRE_TEMP_RR:return true;}
        return false;
    }
    private static boolean both(int s){return centred(s);}
    /** Readings that swing both ways around 0 (left / right: lateral G, steering angle, yaw, steering torque; accelerate / brake:
     *  longitudinal G) have 0 in the middle of their scale and fill from it. Everything else starts at the scale's left end
     *  (boost: 0 bar there). Their peak is the largest swing either way. */
    static boolean centred(int sig){int s=fmt(sig);return s==11||s==12||s==Telemetry.STEERING||s==Telemetry.YAW||s==Telemetry.STEER_TORQUE;}
    /** Half the scale of a centred reading: ±1.5 G, ±540° steering, ±60 °/s yaw, ±8 N·m steering torque. */
    private static float halfSpan(int s){return s==Telemetry.STEERING?540:s==Telemetry.YAW?60:s==Telemetry.STEER_TORQUE?8:1.5f;}
    /** The reading's place on its scale (0..1, NaN for none): centred ones around .5, the rest as the tiles show them. */
    /** Boost like a Defi gauge: vacuum (-1..0 bar) squeezed into the first eighteenth of the sweep, so on a 270 degree dial (which
     *  starts at half past seven) 0 bar sits at eight o'clock and the needle rests there at idle; boost (0..2.0 bar) has the rest, so
     *  1.0 bar is near twelve o'clock as on the Defi. */
    static final float VAC=1/18f;
    static float frac(int sig,float v){int s=fmt(sig);if(s==Telemetry.BOOST)return Float.isNaN(v)?Float.NaN:v<0?VAC*(Math.max(v,-1)+1):VAC+(1-VAC)*v/2f;
        if(centred(sig))return Float.isNaN(v)?Float.NaN:.5f+v/(2*halfSpan(s));return Tiles.fraction(s,v);}
    /** Where a reading's bar starts: the middle for a centred one, the left end otherwise. */
    static float zero(int sig){return centred(sig)?.5f:fmt(sig)==Telemetry.BOOST?VAC:0;}
    /** Cell k's highest reading so far, or NaN. */
    static float peak(int k,int sig){return !still&&k>=0&&k<4&&peakSig[k]==sig?peak[k]:Float.NaN;}
    /** Drawing the settings' example window: no peaks (they belong to the real readings). */
    private static boolean still;
    /** Partial redraw: ALL draws everything, STAT only what does not move (kept as the bitmap below), DYN the rest each frame.
     *  FloatSkin and the round gauge read PASS; FloatLayouts gets it as an argument. */
    static final int ALL=FloatLayouts.ALL,STAT=FloatLayouts.STAT,DYN=FloatLayouts.DYN;static int PASS=ALL;
    private static boolean st(){return PASS!=DYN;}
    private static boolean dy(){return PASS!=STAT;}
    /** The kept bitmap of the window's unmoving part (PASS STAT) and what it was drawn for. */
    private static android.graphics.Bitmap layer;private static int layerKey;
    /** Times the kept bitmap was drawn again (for the FLOAT log); off draws every part each frame, for a side-by-side look. */
    public static int layerBuilds;public static boolean layerOn=true;
    // ---- glide: bars, arcs and the rpm bar move from the last reading to the new one over GLIDE_MS (ease-out), the numbers jump as
    // before. The car sends every 40-80 ms, so a bar stepped about 12 times a second; gliding fills the frames in between. -----------
    public static final long GLIDE_MS=80;
    /** Off: every bar jumps (FloatService turns it off while /sdcard/i30pd_float_noglide exists, for a side-by-side look). */
    public static boolean glideOn=true;
    private static final int GLIDES=9;   // cells 0..7 and the rpm bar
    private static final float[] gFrom=new float[GLIDES],gTo=new float[GLIDES];private static final long[] gAt=new long[GLIDES];private static long drawNow;
    static{java.util.Arrays.fill(gTo,Float.NaN);}
    /** The bar position (0..1) to draw for slot i now: the target itself in a still picture, when off, at the first reading or with
     *  no reading; otherwise on its way from where it was shown when the target moved. */
    static float glide(int i,float f){
        if(still||!glideOn||i<0||i>=GLIDES){return f;}
        if(Float.isNaN(f)){gTo[i]=Float.NaN;return f;}
        if(Float.isNaN(gTo[i])){gTo[i]=f;gFrom[i]=f;gAt[i]=0;return f;}
        if(Math.abs(f-gTo[i])>1e-4f){gFrom[i]=glided(i,drawNow);gTo[i]=f;gAt[i]=drawNow;}
        return glided(i,drawNow);
    }
    private static float glided(int i,long now){
        if(gAt[i]==0)return gTo[i];float t=Math.max(0,Math.min(1,(now-gAt[i])/(float)GLIDE_MS));t=1-(1-t)*(1-t);
        return gFrom[i]+(gTo[i]-gFrom[i])*t;}
    /** A bar is still on its way: the window keeps drawing every frame until it lands. */
    public static boolean gliding(long now){
        if(!glideOn)return false;for(int i=0;i<GLIDES;i++)if(gAt[i]!=0&&!Float.isNaN(gTo[i])&&now-gAt[i]<GLIDE_MS)return true;return false;}
    /** Adds the cell's value to its line graph once every HIST_MS, and to its peak every time (called on every draw). */
    static void record(int k,int sig,float v,long now){
        if(peakSig[k]!=sig||now<peakAt[k]||now-peakAt[k]>PEAK_IDLE_MS){peakSig[k]=sig;peak[k]=Float.NaN;peakAt[k]=now;}
        if(!Float.isNaN(v)&&peaks(sig)){float a=both(sig)?Math.abs(v):v;if(Float.isNaN(peak[k])||a>peak[k])peak[k]=a;peakAt[k]=now;}
        if(histSig[k]!=sig||now<histAt[k]||histN[k]>0&&now-histAt[k]>HIST_GAP_MS){histSig[k]=sig;histN[k]=0;histHead[k]=0;histAt[k]=0;}   // a new signal, or the clock went back: start over
        if(histN[k]>0&&now-histAt[k]<HIST_MS)return;
        histAt[k]=now;hist[k][histHead[k]]=v;histHead[k]=(histHead[k]+1)%HIST;if(histN[k]<HIST)histN[k]++;
    }

    /** Records the cells without drawing (the dashboard's own frame, while the window is down), so a peak reached there
     *  counts and the line graph has no hole. */
    public static void track(int n,Telemetry.Sample[] q,VehicleProfile prof,long now){
        for(int i=0;i<n;i++){int sig=signal(i,prof);record(i,sig,value(q,sig),now);}
    }
    private static int lastLook;
    /** Every frame tick: records the line graphs and says whether the window would look different from the last tick
     *  (text, colour, bar end to the pixel, a new line-graph sample), so an unchanged window is not drawn again. */
    /** The shown text of a value as an int for the change hash (Tiles.textKey), no string built. */
    private static int key(int sig,float v){long k=Tiles.textKey(fmt(sig),v);return (int)(k^(k>>>32));}
    public static boolean tick(int n,int size,Telemetry.Sample[] q,VehicleProfile prof,long now){
        int h=17;h=h*31+skinNow();h=h*31+alpha;h=h*31+color;h=h*31+corner;h=h*31+Theme.current().id.hashCode();
        for(int i=0;i<n;i++){int sig=signal(i,prof);float v=value(q,sig),f=frac(sig,v);record(i,sig,v,now);
            h=h*31+sig;h=h*31+key(sig,v);h=h*31+Tiles.level(fmt(sig),v);h=h*31+graphs[i];
            h=h*31+(Float.isNaN(f)?-1:Math.round(Math.max(0,Math.min(1,f))*cellW(CARD,size)));
            if(graphs[i]==LINE)h=h*31+histHead[i]*64+histN[i];
            float pk=peak(i,sig);if(!Float.isNaN(pk))h=h*31+key(sig,pk);
            if(quad(sig)){quadTexts(q,sig);for(int w=0;w<4;w++)h=h*31+QV[w].hashCode()+QL[w];}}
        int s=shape==OFF?lastShape:shape;
        if(s==PILL||s==GEARBOX||s==SHIFT||s==WEDGE||s==TOWER){Telemetry.Sample rm=q[0],gm=q[8];float rf=rm!=null&&rm.valid?Tiles.fraction(0,rm.value):Float.NaN;
            h=h*31+(Float.isNaN(rf)?-1:Math.round(Math.max(0,Math.min(1,rf))*120));h=h*31+(gm!=null&&gm.valid?key(8,gm.value):0);}
        h=h*31+s;boolean changed=h!=lastLook;lastLook=h;return changed;
    }

    private FloatPanel(){}
    /** Columns of a shape with n cells: a card is 2 wide from 2 cells, the strip one row, the side one column. */
    public static int cols(int shape,int n){return shape==STRIP?n:shape==SIDE?1:Math.min(2,n);}
    public static int rows(int shape,int n){return (n+cols(shape,n)-1)/cols(shape,n);}
    public static int cellW(int shape,int size){return Math.round((round(shape)?GAUGE:CELL_W)*SIZES[clampSize(size)]);}
    public static int cellH(int shape,int size){return Math.round((round(shape)?GAUGE:CELL_H)*SIZES[clampSize(size)]);}
    public static int width(int shape,int n,int size){return shape<=OFF?0:layout(shape)?Math.round(FloatLayouts.w(shape,n)*SIZES[clampSize(size)]):cols(shape,n)*cellW(shape,size);}
    public static int height(int shape,int n,int size){return shape<=OFF?0:layout(shape)?Math.round(FloatLayouts.h(shape,n)*SIZES[clampSize(size)]):rows(shape,n)*cellH(shape,size);}
    /** Each shown cell's rectangle in the window (l, t, r, b at the size): taps and the settings' outline. */
    public static float[][] cellRects(int shape,int n,int size){int u=used(shape,n);float[][] o=new float[u][];float k=SIZES[clampSize(size)];
        for(int i=0;i<u;i++){if(layout(shape)){float[] b=FloatLayouts.rect(shape,n,i);o[i]=new float[]{b[0]*k,b[1]*k,b[2]*k,b[3]*k};}
            else{int cols=cols(shape,n),cw=cellW(shape,size),ch=cellH(shape,size);o[i]=new float[]{(i%cols)*cw,(i/cols)*ch,(i%cols+1)*cw,(i/cols+1)*ch};}}
        return o;}
    private static int clampSize(int s){return Math.max(0,Math.min(SIZES.length-1,s));}
    /** Where the window starts (screen 800 × 480, below the 52 px status bar): the card and the side column top right, the strip bottom middle. */
    private static boolean bottom(int s){return s==STRIP||s==PILL||s==SHIFT||s==TICK;}
    public static int defaultX(int shape,int n,int size){return bottom(shape)?(800-width(shape,n,size))/2:800-width(shape,n,size)-8;}
    public static int defaultY(int shape,int n,int size){return bottom(shape)?480-height(shape,n,size)-10:62;}
    /** The next cell count after the current one (1, 2, 4, 1 ...). */
    public static int nextCount(int n){return n>=4?1:n>=2?4:2;}
    /** The signal cell k shows: its choice, or the automatic one when it has none or this car lacks it (a DPF value on a
     *  petrol, the DCT clutches on a 6MT). */
    public static int signal(int k,VehicleProfile p){return resolve(cells[k],k,p);}
    // ---- 타이어 4개: one cell with all four wheels (pressure or temperature) -------------------------------------------
    public static final int QUAD_P=2000,QUAD_T=2001;
    public static boolean quad(int s){return s==QUAD_P||s==QUAD_T;}
    private static String unit0(int s){String u=Telemetry.UNITS[fmt(s)];return u==null?"":u;}
    /** Wheel w (0 FL, 1 FR, 2 RL, 3 RR) of a 4-wheel cell. */
    public static int quadSig(int s,int w){return (s==QUAD_P?Telemetry.TPMS_FL:Telemetry.TIRE_TEMP_FL)+w;}
    /** The signal a cell formats and scales with: a wheel for the 4-wheel cells. */
    static int fmt(int s){return quad(s)?quadSig(s,0):s;}
    public static boolean available(int s,VehicleProfile p){return quad(s)?Tiles.available(quadSig(s,0),p):Tiles.available(s,p);}
    public static int groupOf(int s){return Tiles.groupOf(fmt(s));}
    /** A cell's value: the reading, or for 4 wheels the lowest pressure / the highest temperature. */
    static float value(Telemetry.Sample[] q,int sig){
        if(!quad(sig)){if(sig<0||sig>=q.length)return Float.NaN;Telemetry.Sample m=q[sig];return m!=null&&m.valid?m.value:Float.NaN;}
        float r=Float.NaN;for(int w=0;w<4;w++){Telemetry.Sample m=q[quadSig(sig,w)];if(m==null||!m.valid)continue;float v=m.value;r=Float.isNaN(r)?v:sig==QUAD_P?Math.min(r,v):Math.max(r,v);}
        return r;}
    static final String[] QV=new String[4];static final int[] QL=new int[4];
    /** The four wheels' texts and levels into QV / QL. */
    static void quadTexts(Telemetry.Sample[] q,int sig){for(int w=0;w<4;w++){int s=quadSig(sig,w);Telemetry.Sample m=q[s];float v=m!=null&&m.valid?m.value:Float.NaN;QV[w]=Tiles.text(s,v);QL[w]=Tiles.level(s,v);}}
    /** The four wheels on one line (FL FR RL RR), for small places. */
    public static String inline(Telemetry.Sample[] q,int sig){quadTexts(q,sig);return QV[0]+" "+QV[1]+" · "+QV[2]+" "+QV[3];}
    public static int resolve(int c,int k,VehicleProfile p){boolean diesel=p!=null&&p.fuel==VehicleProfile.Fuel.DIESEL;
        return c>=0&&(p==null||available(c,p))?c:(diesel?AUTO_DIESEL:AUTO_PETROL)[k];}
    /** The reading groups a cell can choose from on this car (Tiles.GROUPS indices, single readings only). */
    public static int[] groups(VehicleProfile p){return Tiles.groups(p,false);}
    public static int[] group(int g,VehicleProfile p){int[] base=Tiles.group(g,p,false);
        if(g!=Tiles.groupOf(Telemetry.TPMS_FL))return base;int n=0;int[] o=new int[base.length+2];   // the tyre group starts with the 4-wheel cells
        for(int s:new int[]{QUAD_P,QUAD_T})if(p==null||available(s,p))o[n++]=s;for(int s:base)o[n++]=s;
        int[] r=new int[n];System.arraycopy(o,0,r,0,n);return r;}
    /** The next choice for a cell: automatic, then CHOICES in order, then automatic again. */
    public static int next(int current){if(current<0)return CHOICES[0];for(int i=0;i<CHOICES.length;i++)if(CHOICES[i]==current)return i+1<CHOICES.length?CHOICES[i+1]:-1;return -1;}
    /** The name as the window shows it (translated). */
    private static String label(int sig){return com.veloster.i18n.UiText.t(name(sig));}
    public static String name(int sig){if(sig==QUAD_P)return "타이어 공기압";if(sig==QUAD_T)return "타이어 온도";if(sig<0||sig>=Telemetry.LABELS.length)return "—";for(int i=0;i<CHOICES.length;i++)if(CHOICES[i]==sig)return CHOICE_NAMES[i];return Telemetry.LABELS[sig];}

    private static final Paint p=new Paint(3);private static final RectF r=new RectF();
    private static final int WARM=0xffffaa3c,HOT=0xffff4b3c;
    /** This frame's colours: N palette values, which Theme.map turns into the theme's. */
    static int BG,EDGE,DIV,TXT,MUT,TRACK,ACC;
    /** A colour of the N palette in this frame's colour choice (the theme's, or as is). */
    static int themed(int c){return color==COLOR_THEME?Theme.current().map(c):c;}
    private static void colours(){
        Theme th=color==COLOR_THEME?Theme.current():null;
        BG=m(th,0xd805070a)&0xffffff|ALPHAS[Math.max(0,Math.min(ALPHAS.length-1,alpha))]<<24;EDGE=m(th,0xff3a4650);DIV=m(th,0x993a4650);TXT=m(th,0xfff2f6f8);MUT=m(th,0xff8fa0a8);TRACK=m(th,0xff2a343b);
        ACC=color==COLOR_WHITE?0xffe8eef2:m(th,0xff5bc8f5);
    }
    private static int m(Theme th,int c){return th==null?c:th.map(c);}
    /** The corner drawn: 0 square, 1 round, 2 four cut corners, 3 the N cut (top right and bottom left). */
    public static int cornerShape(){
        if(corner==CORNER_SQUARE)return 0;if(corner==CORNER_ROUND)return 1;if(corner==CORNER_CUT)return 2;
        int s=Theme.current().shape;return s==Theme.SHAPE_ROUND?1:s==Theme.SHAPE_HEX?2:3;
    }
    private static final android.graphics.Path edge=new android.graphics.Path();
    /** The window's body and its 1 px edge in the chosen corner shape. */
    static void outline(Canvas c,float w,float h,float k,int fill,int line){
        int cs=cornerShape();float a=cs==1?10*k:cs==0?2*k:11*k;
        p.setStyle(Paint.Style.FILL);p.setColor(fill);
        if(cs<=1){r.set(0,0,w,h);c.drawRoundRect(r,a,a,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(line);r.set(.5f,.5f,w-.5f,h-.5f);c.drawRoundRect(r,a,a,p);}
        else for(int pass=0;pass<2;pass++){float o=pass==0?0:.5f,l=o,t=o,rr=w-o,b=h-o;edge.reset();
            if(cs==2){edge.moveTo(l+a,t);edge.lineTo(rr-a,t);edge.lineTo(rr,t+a);edge.lineTo(rr,b-a);edge.lineTo(rr-a,b);edge.lineTo(l+a,b);edge.lineTo(l,b-a);edge.lineTo(l,t+a);}
            else{edge.moveTo(l,t);edge.lineTo(rr-a,t);edge.lineTo(rr,t+a);edge.lineTo(rr,b);edge.lineTo(l+a,b);edge.lineTo(l,b-a);}
            edge.close();if(pass==1){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(line);}c.drawPath(edge,p);}
        p.setStyle(Paint.Style.FILL);
    }
    private static final int[] SIG=new int[4],G=new int[4];
    private static final FloatSkin.Cell[] LCELLS={new FloatSkin.Cell(),new FloatSkin.Cell(),new FloatSkin.Cell(),new FloatSkin.Cell()};
    /** A cell's texts and scale positions for a skin or a layout. */
    private static void fillCell(FloatSkin.Cell d,int i,int sig,float v,int g,boolean first){float pk=peak(i,sig);
        d.name=label(sig);d.en=com.veloster.i18n.UiText.en(name(sig)).toUpperCase(java.util.Locale.US);
        d.val=Tiles.text(fmt(sig),v);String u=Telemetry.UNITS[fmt(sig)];d.unit=u==null?"":u;d.f=glide(i,frac(sig,v));d.lv=Tiles.level(fmt(sig),v);
        d.zero=zero(sig);d.pk=Float.isNaN(pk)?null:Tiles.text(fmt(sig),pk);d.pf=Float.isNaN(pk)?Float.NaN:frac(sig,pk);
        d.first=first;d.bar=g!=NUM;d.quad=null;
        if(quad(sig)){quadTexts(LAST_Q,sig);System.arraycopy(QV,0,d.q4,0,4);System.arraycopy(QL,0,d.l4,0,4);d.quad=d.q4;d.val=QV[0]+" "+QV[1]+" · "+QV[2]+" "+QV[3];d.f=Float.NaN;d.lv=0;}}
    /** Draws the window (shape, n cells, size) at 0,0 from the samples (index = signal). */
    public static void draw(Canvas c,int shape,int n,int size,Telemetry.Sample[] q,VehicleProfile prof,long now){
        for(int i=0;i<n;i++){SIG[i]=signal(i,prof);G[i]=graphs[i];}
        draw(c,shape,n,size,q,SIG,G,0,true,now);
    }
    /** The window as draw does, but without recording (the settings' preview and sheets: example values must not reach
     *  the peaks or the line graphs). */
    public static void drawStill(Canvas c,int shape,int n,int size,Telemetry.Sample[] q,VehicleProfile prof,long now){
        for(int i=0;i<n;i++){SIG[i]=signal(i,prof);G[i]=graphs[i];}
        still=true;try{draw(c,shape,n,size,q,SIG,G,0,false,now);}finally{still=false;}
    }
    private static Telemetry.Sample[] demo;
    /** Fixed example readings for the settings (a car at 3,500 rpm in 3rd, boost +0.42 bar, coolant 88 °C ...): a live
     *  preview redrew nine windows every frame and lagged. Signals without one sit at the middle of their bar. */
    public static Telemetry.Sample[] example(){
        if(demo!=null)return demo;Telemetry.Sample[] d=new Telemetry.Sample[Telemetry.LABELS.length];
        for(int i=0;i<d.length;i++){float f0=Tiles.fraction(i,0),f1=Tiles.fraction(i,1),v=Float.isNaN(f0)||Float.isNaN(f1)||f1==f0?0:(.55f-f0)/(f1-f0);
            if(i==0)v=3500;else if(i==1||i==2)v=88;else if(i==7)v=87;else if(i==8)v=3;else if(i==9)v=35;else if(i==Telemetry.BOOST)v=.42f;
            else if(i==Telemetry.INTAKE)v=34;else if(i==Telemetry.BAT_VOLTAGE)v=13.8f;else if(i==Telemetry.AMBIENT)v=14;
            d[i]=new Telemetry.Sample(v,true,false,true,false,0);}
        return demo=d;
    }
    /** One cell showing sig with graph g at 0,0 (the settings' value chooser); its line graph is cell k's memory. */
    public static void drawCell(Canvas c,int shape,int size,Telemetry.Sample[] q,int sig,int g,int k,long now){
        still=true;try{draw(c,CARD,1,size,q,new int[]{sig},new int[]{g},k,false,now);}finally{still=false;}
    }
    /** first: the cell index of the first cell drawn (its line-graph memory); rec: the real window, which feeds the line graphs. */
    private static Telemetry.Sample[] LAST_Q;
    private static void draw(Canvas c,int shape,int n,int size,Telemetry.Sample[] q,int[] sigs,int[] gs,int first,boolean rec,long now){
        colours();LAST_Q=q;drawNow=now;PASS=ALL;
        if(still||!layerOn){parts(c,shape,n,size,q,sigs,gs,first,rec,now);return;}
        // partial redraw: what does not move is drawn once into a bitmap, again when anything it shows changes: the shape, count, size,
        // look, alpha, theme, language, or a cell's reading, graph, mode (a value or none yet, a peak or none, a line with history) or warning
        int key=17;key=key*31+shape;key=key*31+n;key=key*31+size;key=key*31+skinNow();key=key*31+alpha;key=key*31+first;key=key*31+Theme.current().id.hashCode();
        for(int j=0;j<n;j++){int i=first+j,sig=sigs[j];float v=value(q,sig);key=key*31+sig;key=key*31+gs[j];key=key*31+label(sig).hashCode();
            key=key*31+(Float.isNaN(frac(sig,v))?1:0)+(Float.isNaN(peak(i,sig))?2:0)+(gs[j]==LINE&&drawn(i,sig)?4:0)+8*Tiles.level(fmt(sig),v);}
        int bw=Math.max(1,width(shape,n,size)),bh=Math.max(1,height(shape,n,size));
        if(layer==null||layerKey!=key||layer.getWidth()!=bw||layer.getHeight()!=bh){
            if(layer==null||layer.getWidth()!=bw||layer.getHeight()!=bh)layer=android.graphics.Bitmap.createBitmap(bw,bh,android.graphics.Bitmap.Config.ARGB_8888);
            else layer.eraseColor(0);
            PASS=STAT;try{parts(new Canvas(layer),shape,n,size,q,sigs,gs,first,false,now);}finally{PASS=ALL;}layerKey=key;layerBuilds++;}
        c.drawBitmap(layer,0,0,null);
        PASS=DYN;try{parts(c,shape,n,size,q,sigs,gs,first,rec,now);}finally{PASS=ALL;}
    }
    private static void parts(Canvas c,int shape,int n,int size,Telemetry.Sample[] q,int[] sigs,int[] gs,int first,boolean rec,long now){
        int cols=cols(shape,n),rows=rows(shape,n),cw=cellW(shape,size),ch=cellH(shape,size),w=cols*cw,h=rows*ch;float k=SIZES[clampSize(size)];
        if(round(shape)){   // no window body: each reading is its own disc
            for(int j=0;j<n;j++){int i=first+j,sig=sigs[j];float v=value(q,sig);if(rec)record(i,sig,v,now);
                gauge(c,(j%cols)*cw,(j/cols)*ch,cw,k,sig,v,peak(i,sig),i);}
            return;}
        int sk=skinNow();
        if(layout(shape)){   // a layout: its own inside, the skin's colours
            int u=FloatLayouts.used(shape,n);
            for(int j=0;j<u;j++){int i=first+j,sig=sigs[j];float v=value(q,sig);if(rec)record(i,sig,v,now);fillCell(LCELLS[j],i,sig,v,gs[j],j==0&&n>1);}
            Telemetry.Sample rm=q[0],gm=q[8];float rpm=glide(GLIDES-1,rm!=null&&rm.valid?Tiles.fraction(0,rm.value):Float.NaN);String gear=gm!=null&&gm.valid?Tiles.text(8,gm.value):"-";
            float af=ALPHAS[Math.max(0,Math.min(ALPHAS.length-1,alpha))]/(float)0xd8;
            FloatLayouts.draw(c,shape,n,k,sk,af,LCELLS,rpm,gear,PASS);
            return;}
        if(sk!=FloatSkin.PLAIN){   // a skin: its own body and cells
            if(st())FloatSkin.body(c,sk,w,h,k,ALPHAS[Math.max(0,Math.min(ALPHAS.length-1,alpha))]/(float)0xd8);
            for(int j=0;j<n;j++){int i=first+j,sig=sigs[j];float v=value(q,sig);if(rec)record(i,sig,v,now);
                fillCell(FloatSkin.CELL,i,sig,v,gs[j],j==0&&n>1);
                FloatSkin.cell(c,sk,(j%cols)*cw,(j/cols)*ch,cw,ch,k);}
            return;}
        if(st()){outline(c,w,h,k,BG,EDGE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);
            p.setColor(DIV);for(int i=1;i<cols;i++)c.drawLine(i*cw,8*k,i*cw,h-8*k,p);for(int i=1;i<rows;i++)c.drawLine(8*k,i*ch,w-8*k,i*ch,p);}
        p.setStyle(Paint.Style.FILL);
        for(int j=0;j<n;j++){int i=first+j,sig=sigs[j];float x=(j%cols)*cw,y=(j/cols)*ch;float v=value(q,sig);
            int lv=Tiles.level(fmt(sig),v),col=lv==2?HOT:lv==1?WARM:TXT,g=gs[j];if(rec)record(i,sig,v,now);
            if(st())text(c,label(sig),x+8*k,y+17*k,12*k,MUT,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);
            boolean barred=!(Float.isNaN(frac(sig,v))||g==NUM||g==LINE||g==LED||quad(sig));   // only the plain bar has a kept part (its track)
            if(!dy()){if(barred){float bl=x+8*k,br=x+cw-8*k,bt=y+ch-9*k,bb=bt+4*k;p.setColor(TRACK);r.set(bl,bt,br,bb);c.drawRoundRect(r,2*k,2*k,p);}continue;}
            String unit=Telemetry.UNITS[fmt(sig)],val=Tiles.text(fmt(sig),v);float f=g==LINE||g==NUM?frac(sig,v):glide(i,frac(sig,v));
            float pk=peak(i,sig),pf=Float.isNaN(pk)?Float.NaN:frac(sig,pk);
            if(quad(sig)){quadTexts(q,sig);text(c,unit0(sig),x+cw-8*k,y+16*k,10*k,MUT,Paint.Align.RIGHT,Typeface.DEFAULT);quad(c,x,y,cw,ch,k,TXT,DIV,Typeface.DEFAULT_BOLD);continue;}   // the unit where the peak would be
            if(!Float.isNaN(pk)&&g!=LINE){String ps="▲ "+Tiles.text(fmt(sig),pk);   // the peak, small, top right (left out when the name leaves no room)
                if(width(label(sig),12*k,Typeface.DEFAULT_BOLD)+width(ps,10*k,Typeface.DEFAULT)+20*k<=cw)text(c,ps,x+cw-8*k,y+16*k,10*k,MUT,Paint.Align.RIGHT,Typeface.DEFAULT);}
            if(Float.isNaN(f)||g==NUM||g==LINE&&!drawn(i,sig)){   // nothing to graph (no value yet, or the line has no history): the value alone, large, in the middle
                float vw=width(val,30*k,Typeface.DEFAULT_BOLD),uw=width(unit,12*k,Typeface.DEFAULT),x0=x+(cw-(vw+3*k+uw))/2;
                text(c,val,x0,y+ch-13*k,30*k,Float.isNaN(v)?MUT:col,Paint.Align.LEFT,Typeface.DEFAULT_BOLD);text(c,unit,x0+vw+3*k,y+ch-13*k,12*k,MUT,Paint.Align.LEFT,Typeface.DEFAULT);continue;}
            if(g==LINE){   // the value on the name's line (the unit gives way to a long name), the last 30 s below it
                float nw=width(label(sig),12*k,Typeface.DEFAULT_BOLD),vw=width(val,21*k,Typeface.DEFAULT_BOLD),uw=width(unit,10*k,Typeface.DEFAULT);
                boolean fits=nw+vw+uw+26*k<=cw;float right=x+cw-8*k;
                if(fits){text(c,unit,right,y+20*k,10*k,MUT,Paint.Align.RIGHT,Typeface.DEFAULT);right-=uw+3*k;}
                text(c,val,right,y+21*k,21*k,col,Paint.Align.RIGHT,Typeface.DEFAULT_BOLD);
                line(c,i,sig,x+8*k,y+29*k,x+cw-8*k,y+ch-7*k,k,lv);
                if(!Float.isNaN(pf)){float t2=y+29*k,b2=y+ch-7*k,py=b2-Math.max(0,Math.min(1,pf))*(b2-t2);p.setColor(0x99ffffff);   // the peak as a dotted line across the graph
                    for(float xx=x+8*k;xx<x+cw-8*k;xx+=5*k)c.drawRect(xx,py-.5f,Math.min(xx+2.5f*k,x+cw-8*k),py+.5f,p);}
                continue;}
            float uw=width(unit,11*k,Typeface.DEFAULT);
            text(c,unit,x+cw-10*k,y+ch-15*k,11*k,MUT,Paint.Align.RIGHT,Typeface.DEFAULT);
            text(c,val,x+cw-10*k-uw-3*k,y+ch-15*k,26*k,col,Paint.Align.RIGHT,Typeface.DEFAULT_BOLD);
            float bl=x+8*k,br=x+cw-8*k;
            if(g==LED){   // lit segments, the top fifth amber
                int segs=Math.max(8,Math.round((br-bl)/(8*k)));float sw=(br-bl)/segs,bt=y+ch-11*k,bb=bt+6*k;float z0=zero(sig),fc=Float.isNaN(f)?z0:Math.max(0,Math.min(1,f));int litA=Math.round(Math.min(z0,fc)*segs),lit=Math.round(Math.max(z0,fc)*segs);
                int pseg=Float.isNaN(pf)?-1:Math.min(segs-1,Math.max(0,Math.round(Math.max(0,Math.min(1,pf))*segs)-1));
                for(int s2=0;s2<segs;s2++){p.setColor(s2>=litA&&s2<lit?(s2>=segs*.8f?WARM:lv==2?HOT:ACC):s2==pseg?0x88ffffff:TRACK);c.drawRect(bl+s2*sw+k,bt,bl+(s2+1)*sw-k,bb,p);}
                continue;}
            float bt=y+ch-9*k,bb=bt+4*k;if(PASS==ALL){p.setColor(TRACK);r.set(bl,bt,br,bb);c.drawRoundRect(r,2*k,2*k,p);}
            f=Math.max(0,Math.min(1,f));
            float zero=zero(sig),a=bl+(br-bl)*Math.min(zero,f),b=bl+(br-bl)*Math.max(zero,f);
            p.setColor(lv==2?HOT:lv==1?WARM:ACC);r.set(a,bt,Math.max(b,a+2),bb);c.drawRoundRect(r,2*k,2*k,p);
            if(zero>0){p.setColor(0x99ffffff);float z=bl+(br-bl)*zero;c.drawRect(z-.5f,bt-2*k,z+.5f,bb+2*k,p);}
            if(!Float.isNaN(pf)){p.setColor(0xffffffff);float z=bl+(br-bl)*Math.max(0,Math.min(1,pf));c.drawRect(z-k,bt-2*k,z+k,bb+2*k,p);}   // the peak, a white tick
        }
    }
    /** A round gauge in the d x d square at x,y: a dark disc, a 270 degree ring open at the bottom (filled from zero, so
     *  boost below zero fills the other way), the value in the middle, its unit under it and its name in the gap. */
    private static void gauge(Canvas c,float x,float y,float d,float k,int sig,float v,float pk,int slot){
        float cx=x+d/2,cy=y+d/2,rad=d/2-2*k,sw=7*k,ar=rad-sw/2-4*k;
        if(quad(sig)){if(st()){p.setStyle(Paint.Style.FILL);p.setColor(BG);c.drawCircle(cx,cy,rad,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(EDGE);c.drawCircle(cx,cy,rad-.5f,p);p.setStyle(Paint.Style.FILL);
                p.setColor(DIV);c.drawRect(cx-.5f,cy-d*.2f,cx+.5f,cy+d*.2f,p);text(c,label(sig),cx,cy+ar*.84f,10*k,MUT,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);}
            if(!dy())return;quadTexts(LAST_Q,sig);float s2=Math.min(15*k,d*.14f);
            for(int w=0;w<4;w++)text(c,QV[w],cx+(w%2==0?-1:1)*d*.2f,cy+(w<2?-d*.06f:d*.16f),s2,QL[w]==2?HOT:QL[w]==1?WARM:TXT,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);
            return;}
        int lv=Tiles.level(fmt(sig),v),col=lv==2?HOT:lv==1?WARM:TXT;float f=glide(slot,frac(sig,v));
        r.set(cx-ar,cy-ar,cx+ar,cy+ar);
        if(st()){p.setStyle(Paint.Style.FILL);p.setColor(BG);c.drawCircle(cx,cy,rad,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(EDGE);c.drawCircle(cx,cy,rad-.5f,p);
            p.setStrokeWidth(sw);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(TRACK);c.drawArc(r,135,270,false,p);p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
            text(c,label(sig),cx,cy+ar*.84f,11*k,MUT,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);}   // in the ring's open gap, clear of the disc edge
        if(!dy())return;
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setStrokeCap(Paint.Cap.ROUND);
        if(!Float.isNaN(f)){f=Math.max(0,Math.min(1,f));float zero=zero(sig),a=Math.min(zero,f),b=Math.max(zero,f);
            p.setColor(lv==2?HOT:lv==1?WARM:ACC);c.drawArc(r,135+270*a,Math.max(1,270*(b-a)),false,p);}
        p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
        float pf=Float.isNaN(pk)?Float.NaN:frac(sig,pk);
        if(!Float.isNaN(pf)){double a=Math.toRadians(135+270*Math.max(0,Math.min(1,pf)));float o=ar+sw/2+1*k,in=ar-sw/2-1*k;   // the peak: a white tick across the ring
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*k);p.setColor(0xffffffff);
            c.drawLine(cx+(float)Math.cos(a)*in,cy+(float)Math.sin(a)*in,cx+(float)Math.cos(a)*o,cy+(float)Math.sin(a)*o,p);p.setStyle(Paint.Style.FILL);
            text(c,"▲ "+Tiles.text(fmt(sig),pk),cx,cy-ar*.42f,9*k,MUT,Paint.Align.CENTER,Typeface.DEFAULT);}
        String val=Tiles.text(fmt(sig),v),unit=Telemetry.UNITS[fmt(sig)];float size=24*k,room=ar*1.5f;
        p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(size);float vw=p.measureText(val);if(vw>room)size*=room/vw;
        text(c,val,cx,cy+size*.36f,size,Float.isNaN(v)?MUT:col,Paint.Align.CENTER,Typeface.DEFAULT_BOLD);
        if(unit!=null&&!unit.isEmpty())text(c,unit,cx,cy+size*.36f+13*k,10*k,MUT,Paint.Align.CENTER,Typeface.DEFAULT);
    }
    /** Cell k's line graph has at least two samples with a place on the scale. */
    private static boolean drawn(int k,int sig){int n=histN[k],ok=0;if(histSig[k]!=sig)return false;
        for(int j=0;j<n&&ok<2;j++)if(!Float.isNaN(frac(sig,hist[k][(histHead[k]-n+j+HIST*2)%HIST])))ok++;return ok>=2;}
    private static final android.graphics.Path path=new android.graphics.Path();
    /** Cell k's last 30 s in the box l,t-r,b: a filled area under a line, scaled to the signal's bar range, the latest value a dot at the right. */
    private static void line(Canvas c,int k,int sig,float l,float t,float r2,float b,float s,int lv){
        int n=histN[k];if(n<2)return;
        float px=(r2-l)/(HIST-1);boolean open=false;float lastX=0,lastY=0,firstX=0;path.reset();
        for(int j=0;j<n;j++){float v=hist[k][(histHead[k]-n+j+HIST*2)%HIST],f=frac(sig,v);
            if(Float.isNaN(f))continue;f=Math.max(0,Math.min(1,f));
            float xx=r2-(n-1-j)*px,yy=b-f*(b-t);if(!open){path.moveTo(xx,b);path.lineTo(xx,yy);firstX=xx;open=true;}else path.lineTo(xx,yy);lastX=xx;lastY=yy;}
        if(!open)return;
        path.lineTo(lastX,b);path.close();p.setStyle(Paint.Style.FILL);p.setColor(lv==2?0x55ff4b3c:lv==1?0x55ffaa3c:ACC&0xffffff|0x44000000);c.drawPath(path,p);
        path.reset();open=false;
        for(int j=0;j<n;j++){float v=hist[k][(histHead[k]-n+j+HIST*2)%HIST],f=frac(sig,v);if(Float.isNaN(f))continue;f=Math.max(0,Math.min(1,f));
            float xx=r2-(n-1-j)*px,yy=b-f*(b-t);if(!open){path.moveTo(xx,yy);open=true;}else path.lineTo(xx,yy);}
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f*s);p.setColor(lv==2?HOT:lv==1?WARM:ACC);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        p.setColor(0xffffffff);c.drawCircle(lastX,lastY,2.2f*s,p);
    }
    /** The four wheels (QV / QL) in the cell under its name: FL FR on top, RL RR below, a small car outline between. */
    static void quad(Canvas c,float x,float y,float cw,float ch,float k,int ink,int line,Typeface face){
        float cx=x+cw/2,t=y+24*k,b=y+ch-6*k;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,k));p.setColor(line);r.set(cx-6*k,t,cx+6*k,b);c.drawRoundRect(r,3*k,3*k,p);p.setStyle(Paint.Style.FILL);
        float s=Math.min(16*k,(ch-28*k)/2.1f);
        for(int w=0;w<4;w++){int col=QL[w]==2?HOT:QL[w]==1?WARM:ink;float yy=w<2?t+s*.95f:b-2*k;
            if(w%2==0)text(c,QV[w],cx-10*k,yy,s,col,Paint.Align.RIGHT,face);else text(c,QV[w],cx+10*k,yy,s,col,Paint.Align.LEFT,face);}
    }
    private static void text(Canvas c,String s,float x,float y,float size,int color,Paint.Align a,Typeface face){
        p.setTypeface(face);p.setTextSize(size);p.setTextAlign(a);p.setColor(color);c.drawText(s,x,y,p);}
    private static float width(String s,float size,Typeface face){p.setTypeface(face);p.setTextSize(size);return p.measureText(s);}
}
