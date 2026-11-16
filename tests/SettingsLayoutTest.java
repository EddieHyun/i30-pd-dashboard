import com.veloster.vehicle.CarPaint;
import com.veloster.vehicle.ClusterPicker;
import com.veloster.vehicle.SettingsLayout;
import com.veloster.vehicle.Theme;

/** Settings: categories on the left, every category's choices fit the right pane and hit themselves. */
public final class SettingsLayoutTest {
    private static int checks;
    private static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        int n=SettingsLayout.CATS.length;
        check(SettingsLayout.CATS[SettingsLayout.DONE].equals("완료")&&SettingsLayout.DONE==n-1,"완료 last");
        check(SettingsLayout.catTop(n-1)+SettingsLayout.CAT_H<=SettingsLayout.BOTTOM,"categories fit the height");
        for(int i=0;i<n;i++)check(SettingsLayout.cat(SettingsLayout.CAT_L+10,SettingsLayout.catTop(i)+10)==i,"category "+i+" hits itself");
        check(SettingsLayout.cat(SettingsLayout.PANE_L+10,SettingsLayout.TOP+10)==-1&&SettingsLayout.cat(SettingsLayout.CAT_L+10,SettingsLayout.catTop(1)-2)==-1,"pane and gaps are no category");
        check(SettingsLayout.THEME_NAMES.length==Theme.IDS.length,"a name per theme");
        check(SettingsLayout.count(SettingsLayout.SCREENS)==ClusterPicker.COUNT&&SettingsLayout.count(SettingsLayout.PAINT)==CarPaint.IDS.length+1,"every screen and paint listed, then the 8-bit sky");
        check(SettingsLayout.count(SettingsLayout.DONE)==0&&SettingsLayout.item(SettingsLayout.DONE,400,200)==-1,"완료 has no pane");
        check(SettingsLayout.count(SettingsLayout.MODE)==SettingsLayout.MODE_FIRST+ClusterPicker.MODES.length,"a row per mode after the two switches");
        check(SettingsLayout.count(SettingsLayout.PICK)==ClusterPicker.COUNT+1&&SettingsLayout.top(SettingsLayout.PICK,0)>=SettingsLayout.TOP+SettingsLayout.TITLE_H,"chooser: every screen and 원래 화면, under its title");
        for(int cat=0;cat<=SettingsLayout.PICK;cat++){
            if(cat==SettingsLayout.SOUND)continue;   // scrollable pane: checked below
            for(int i=0;i<SettingsLayout.count(cat);i++){
                float l=SettingsLayout.left(cat,i),t=SettingsLayout.top(cat,i),w=SettingsLayout.itemW(cat),h=SettingsLayout.itemH(cat);
                check(l>=SettingsLayout.PANE_L&&l+w<=SettingsLayout.PANE_R+0.01f&&t>=SettingsLayout.TOP&&t+h<=SettingsLayout.BOTTOM+0.01f,"cat "+cat+" item "+i+" inside the pane");
                check(SettingsLayout.item(cat,l+w/2,t+h/2)==i,"cat "+cat+" item "+i+" hits itself");
                check(h>=40,"cat "+cat+" items tall enough to tap");
            }
        }
        // The 사운드 pane keeps a tappable row and scrolls: every row is reachable when scrolled into the viewport.
        check(SettingsLayout.itemH(SettingsLayout.SOUND)>=40,"sound rows tall enough to tap");
        check(SettingsLayout.maxPaneScroll(SettingsLayout.SOUND)>0,"the sound pane scrolls (its rows overflow)");
        float mid=SettingsLayout.TOP+(SettingsLayout.BOTTOM-SettingsLayout.TOP)/2,max=SettingsLayout.maxPaneScroll(SettingsLayout.SOUND);
        for(int i=0;i<SettingsLayout.count(SettingsLayout.SOUND);i++){
            float ih=SettingsLayout.itemH(SettingsLayout.SOUND),t=SettingsLayout.top(SettingsLayout.SOUND,i);
            float scroll=Math.max(0,Math.min(max,t+ih/2-mid));   // bring this row's centre into the viewport
            float cx=SettingsLayout.left(SettingsLayout.SOUND,i)+SettingsLayout.itemW(SettingsLayout.SOUND)/2,cy=t-scroll+ih/2;
            check(cy>=SettingsLayout.TOP&&cy<=SettingsLayout.BOTTOM,"sound item "+i+" scrolls into view");
            check(SettingsLayout.item(SettingsLayout.SOUND,cx,cy,scroll)==i,"sound item "+i+" hits itself when scrolled in");
            check(SettingsLayout.left(SettingsLayout.SOUND,i)>=SettingsLayout.PANE_L&&SettingsLayout.left(SettingsLayout.SOUND,i)+SettingsLayout.itemW(SettingsLayout.SOUND)<=SettingsLayout.PANE_R+0.01f,"sound item "+i+" inside the pane width");
        }
        check(SettingsLayout.item(SettingsLayout.THEME,SettingsLayout.CAT_L+10,SettingsLayout.TOP+10)==-1,"the category column is not a choice");
        check(SettingsLayout.downScreen(0,ClusterPicker.MANUAL)&&SettingsLayout.downScreen(0,ClusterPicker.TACHO)&&!SettingsLayout.downScreen(0,ClusterPicker.F1)&&SettingsLayout.downScreen(1,ClusterPicker.F1+2)&&!SettingsLayout.downScreen(1,ClusterPicker.CLASSIC)&&SettingsLayout.downScreen(2,ClusterPicker.CLASSIC),"downshift-sound screens: 람다 and RPM, then +F1/퍼포먼스/레이스, then all");
        // 플로팅 pane: every button is big enough to hit in a car, inside the pane, clear of the others and of the preview
        int[] ft=SettingsLayout.F_TARGETS;
        for(int a=0;a<ft.length;a++){float[] b=SettingsLayout.floatBox(ft[a]);
            check(b[3]-b[1]>=36&&b[2]-b[0]>=52,"float button "+ft[a]+" at least 52 x 36");
            check(b[0]>=SettingsLayout.PANE_L&&b[2]<=SettingsLayout.PANE_R&&b[1]>=SettingsLayout.TOP&&b[3]<=SettingsLayout.BOTTOM-14,"float button "+ft[a]+" inside the pane, above the hint");
            check(b[0]>=SettingsLayout.FW_R||b[3]<=SettingsLayout.FW_B||b[1]>=SettingsLayout.FW_B,"float button "+ft[a]+" clear of the preview");
            for(int c=a+1;c<ft.length;c++){float[] o=SettingsLayout.floatBox(ft[c]);
                if(ft[a]==SettingsLayout.F_EDIT&&ft[c]>SettingsLayout.F_EDIT||ft[c]==SettingsLayout.F_EDIT)continue;
                check(b[2]<=o[0]||o[2]<=b[0]||b[3]<=o[1]||o[3]<=b[1],"float buttons "+ft[a]+" and "+ft[c]+" do not overlap");}
            float cx=(b[0]+b[2])/2,cy=(b[1]+b[3])/2;
            check(SettingsLayout.floatHit(cx,cy,2,2,124,64)==ft[a],"a tap in the middle of "+ft[a]+" hits it");}
        {float w=2*124,h=2*64,k=SettingsLayout.floatPreviewScale(w,h),x=SettingsLayout.floatPreviewX(w,h),y=SettingsLayout.floatPreviewY(w,h);
            check(k==1,"a 2 x 2 card at 보통 fits the preview at full size");
            check(SettingsLayout.floatHit(x+124+30,y+64+30,2,2,124,64)==SettingsLayout.F_CELL+3,"the bottom right preview cell is cell 4");
            float ws=4*124*1.5f,ks=SettingsLayout.floatPreviewScale(ws,64*1.5f);check(ks<1&&SettingsLayout.floatPreviewX(ws,96)>=SettingsLayout.FW_L,"a wide strip is scaled down to fit");}
        // the chooser sheet: tabs (자동 + 9 groups) on the left, up to 15 readings, graph buttons under them, 취소 / 완료
        for(int t=0;t<10;t++){float[] b=SettingsLayout.sheetTab(t);check(b[3]<=SettingsLayout.SH_B-10&&b[2]<SettingsLayout.S_GX,"tab "+t+" on the sheet, left of the readings");
            check(SettingsLayout.sheetHit((b[0]+b[2])/2,(b[1]+b[3])/2,10,15,4)==SettingsLayout.S_TAB+t,"tab "+t+" hits");}
        for(int i=0;i<SettingsLayout.S_MAX_VALUES;i++){float[] b=SettingsLayout.sheetValue(i);check(b[3]<SettingsLayout.S_GRAPH_Y-6&&b[1]>SettingsLayout.S_CELL_Y+64,"reading "+i+" between the cell preview and the graph buttons");
            check(b[2]-b[0]>=140&&b[3]-b[1]>=40,"reading "+i+" at least 140 x 40");
            check(SettingsLayout.sheetHit((b[0]+b[2])/2,(b[1]+b[3])/2,10,15,4)==SettingsLayout.S_VAL+i,"reading "+i+" hits");}
        for(int g=0;g<4;g++){float[] b=SettingsLayout.sheetGraph(g);check(b[3]-b[1]>=50&&b[2]-b[0]>=100&&b[2]<=SettingsLayout.SH_R-10&&b[3]<SettingsLayout.S_DONE_BOX[1],"graph button "+g+" big and on the sheet");
            check(SettingsLayout.sheetHit((b[0]+b[2])/2,(b[1]+b[3])/2,10,15,4)==SettingsLayout.S_GRAPH+g,"graph button "+g+" hits");}
        check(SettingsLayout.sheetHit(500,430,10,15,4)==SettingsLayout.S_CANCEL&&SettingsLayout.sheetHit(620,430,10,15,4)==SettingsLayout.S_DONE,"취소 and 완료");
        check(SettingsLayout.sheetHit(30,30,10,15,4)==-1&&SettingsLayout.sheetHit(300,440,10,15,4)==SettingsLayout.S_IN,"outside / empty sheet");
        check(SettingsLayout.sheetHit(300,124,10,2,4)==SettingsLayout.S_VAL&&SettingsLayout.sheetHit(300,170,10,2,4)==SettingsLayout.S_IN,"only the readings there are hit (2 readings: row 2 is empty)");
        // the skin sheet: 9 tiles on the sheet, each big enough and hit by its middle; outside the sheet closes
        for(int i=0;i<9;i++){float[] b=SettingsLayout.skinTile(i);check(b[2]<=SettingsLayout.SH_R-10&&b[3]<=SettingsLayout.SH_B-10&&b[2]-b[0]>=150&&b[3]-b[1]>=90,"skin tile "+i+" big and on the sheet");
            check(SettingsLayout.skinHit((b[0]+b[2])/2,(b[1]+b[3])/2,9)==i,"skin tile "+i+" hits");}
        check(SettingsLayout.skinHit(30,30,9)==-2&&SettingsLayout.skinHit(SettingsLayout.SH_L+4,SettingsLayout.SH_T+10,9)==-1,"outside closes, the sheet's edge does nothing");
        System.out.println("PASS "+checks+" settings layout checks");
    }
}
