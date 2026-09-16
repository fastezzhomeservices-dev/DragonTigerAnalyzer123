package com.fastazz.dragontiger;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CardMainActivity extends Activity {
    static final String PREF="rounds";
    static final String[] RANKS={"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
    static final String[] SUITS={"♠","♥","♣","♦"};
    ArrayList<Round> rounds=new ArrayList<>();
    Button dragonBtn,tigerBtn;
    TextView prediction,stats,lastResults,patternText,patternAnalysis,transitionText,countText;
    String dragonCard="A♠", tigerCard="A♥";
    String selectedDragonRank="A", selectedTigerRank="A";

    static class Round { String d,t,r; Round(String d,String t,String r){this.d=d;this.t=t;this.r=r;} }

    @Override public void onCreate(Bundle b){super.onCreate(b);load();ui();render();}

    TextView text(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setPadding(10,8,10,8);return v;}
    GradientDrawable bg(int c,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(18);g.setStroke(2,stroke);return g;}
    LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(12,12,12,12);p.setBackground(bg(Color.rgb(6,40,68),Color.CYAN));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,8,0,0);p.setLayoutParams(lp);return p;}

    void ui(){
        ScrollView sv=new ScrollView(this);sv.setBackgroundColor(Color.rgb(3,18,32));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(10,10,10,20);sv.addView(root);
        TextView title=text("DRAGON TIGER\nPREDICTION",27);title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);title.setTextColor(Color.CYAN);root.addView(title);
        countText=text("HISTORICAL ROUNDS: "+rounds.size(),14);countText.setGravity(Gravity.CENTER);root.addView(countText);

        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels={"DRAGON","TIE","TIGER","PAIR"};int[] colors={Color.CYAN,Color.LTGRAY,Color.rgb(255,179,0),Color.MAGENTA};
        for(int i=0;i<4;i++){TextView v=text(labels[i]+"\n0",14);v.setGravity(Gravity.CENTER);v.setTypeface(null,Typeface.BOLD);v.setBackground(bg(Color.rgb(8,55,90),colors[i]));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,78,1);lp.setMargins(2,2,2,2);top.addView(v,lp);}
        root.addView(top);

        LinearLayout pp=panel();pp.addView(text("NEXT RESULT PREDICTION",19));prediction=text("CALCULATING...",22);prediction.setGravity(Gravity.CENTER);prediction.setTypeface(null,Typeface.BOLD);pp.addView(prediction);root.addView(pp);

        LinearLayout cp=panel();cp.addView(text("CARD REFERENCE • A TO K • ALL 52 CARDS",19));cp.addView(text("Tap DRAGON or TIGER to select any playing card",13));
        LinearLayout sides=new LinearLayout(this);sides.setOrientation(LinearLayout.HORIZONTAL);
        dragonBtn=side("DRAGON\n"+dragonCard,Color.CYAN);tigerBtn=side("TIGER\n"+tigerCard,Color.rgb(255,179,0));
        sides.addView(dragonBtn,new LinearLayout.LayoutParams(0,110,1));LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(0,110,1);tlp.setMargins(8,0,0,0);sides.addView(tigerBtn,tlp);cp.addView(sides);
        dragonBtn.setOnClickListener(v->picker(true));tigerBtn.setOnClickListener(v->picker(false));root.addView(cp);

        LinearLayout rp=panel();rp.addView(text("ADD NEW RESULT",18));
        LinearLayout rr=new LinearLayout(this);rr.setGravity(Gravity.CENTER);
        String[] res={"DRAGON","TIE","TIGER"};
        for(String x:res){Button b=new Button(this);b.setText(x);b.setTextSize(14);b.setAllCaps(false);rr.addView(b,new LinearLayout.LayoutParams(0,58,1));b.setOnClickListener(v->addResult(x));}rp.addView(rr);root.addView(rp);

        LinearLayout lp=panel();lp.addView(text("LAST RESULTS • NEWEST ON LEFT",18));lastResults=text("",15);lastResults.setGravity(Gravity.CENTER);lp.addView(lastResults);root.addView(lp);

        LinearLayout pat=panel();pat.addView(text("3-RESULT PATTERN • SAME REFERENCE",18));patternText=text("",18);patternText.setTextColor(Color.CYAN);patternText.setGravity(Gravity.CENTER);patternText.setTypeface(null,Typeface.BOLD);pat.addView(patternText);patternAnalysis=text("",14);pat.addView(patternAnalysis);root.addView(pat);

        LinearLayout sp=panel();sp.addView(text("STATISTICS",18));stats=text("",14);sp.addView(stats);root.addView(sp);
        LinearLayout tp=panel();tp.addView(text("NUMBER / CARD TRANSITION ANALYSIS",18));transitionText=text("",14);tp.addView(transitionText);root.addView(tp);
        LinearLayout ap=panel();ap.addView(text("DATA CONTROL",18));Button clear=new Button(this);clear.setText("CLEAR SAVED ROUNDS");ap.addView(clear);clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Clear all rounds?").setMessage("All saved results will be deleted.").setPositiveButton("CLEAR",(d,w)->{rounds.clear();save();render();}).setNegativeButton("CANCEL",null).show());Button old=new Button(this);old.setText("OPEN BULK EXCEL / CSV IMPORT");ap.addView(old);old.setOnClickListener(v->startActivity(new Intent(this,ImportActivity.class)));root.addView(ap);
        TextView note=text("Historical analysis only. It does not guarantee the next result.",12);note.setGravity(Gravity.CENTER);root.addView(note);
        setContentView(sv);
    }

    Button side(String s,int c){Button b=new Button(this);b.setText(s);b.setTextSize(18);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(null,Typeface.BOLD);b.setBackground(bg(Color.rgb(10,70,112),c));return b;}

    void picker(boolean isDragon){
        ScrollView sv=new ScrollView(this);GridLayout g=new GridLayout(this);g.setColumnCount(4);g.setPadding(5,5,5,5);g.setBackgroundColor(Color.rgb(3,18,30));sv.addView(g);
        AlertDialog box=new AlertDialog.Builder(this).setTitle((isDragon?"DRAGON":"TIGER")+" • SELECT CARD").setView(sv).create();
        for(String r:RANKS) for(String s:SUITS){String card=r+s;Button b=new Button(this);b.setText(r+"\n"+s);b.setTextSize(15);b.setAllCaps(false);boolean red=s.equals("♥")||s.equals("♦");b.setTextColor(red?Color.rgb(210,25,35):Color.BLACK);b.setBackground(bg(Color.WHITE,red?Color.RED:Color.DKGRAY));GridLayout.LayoutParams q=new GridLayout.LayoutParams();q.width=76;q.height=82;q.setMargins(3,3,3,3);g.addView(b,q);b.setOnClickListener(v->{if(isDragon){dragonCard=card;selectedDragonRank=r;dragonBtn.setText("DRAGON\n"+card);}else{tigerCard=card;selectedTigerRank=r;tigerBtn.setText("TIGER\n"+card);}box.dismiss();});}
        box.show();
    }

    void addResult(String result){rounds.add(new Round(selectedDragonRank,selectedTigerRank,result.equals("DRAGON")?"D":result.equals("TIGER")?"T":"SAME"));save();render();}

    void load(){
        String s=getSharedPreferences("MainActivity_preferences",MODE_PRIVATE).getString(PREF,"");
        if(s.isEmpty()){return;}
        for(String x:s.split(";")){String[] a=x.split(",");if(a.length==4)rounds.add(new Round(a[1],a[2],a[3]));else if(a.length==3)rounds.add(new Round(a[0],a[1],a[2]));}
    }
    void save(){StringBuilder s=new StringBuilder();int n=1;for(Round x:rounds){if(s.length()>0)s.append(';');s.append(n++).append(',').append(x.d).append(',').append(x.t).append(',').append(x.r);}getSharedPreferences("MainActivity_preferences",MODE_PRIVATE).edit().putString(PREF,s.toString()).putBoolean("initialized",true).apply();}

    String label(String r){return "SAME".equals(r)?"TIE":r;}
    String shortR(String r){return "D".equals(r)?"D":"T".equals(r)?"T":"E";}
    String pct(int n,int total){return total==0?"0.0":String.format(Locale.US,"%.1f",n*100.0/total);}

    void render(){
        int total=rounds.size();countText.setText("HISTORICAL ROUNDS: "+total);
        int d=0,t=0,e=0,pair=0;for(Round x:rounds){if("D".equals(x.r))d++;else if("T".equals(x.r))t++;else if("SAME".equals(x.r))e++;if(x.d.equals(x.t))pair++;}
        if(total==0){prediction.setText("WAITING FOR DATA");stats.setText("Dragon 0.0% | Tie 0.0% | Tiger 0.0%\nNo historical rounds loaded.");lastResults.setText("NO RESULTS");patternText.setText("NO PATTERN");patternAnalysis.setText("Add historical data to analyze patterns.");transitionText.setText("No transition data.");return;}
        prediction.setText("HISTORICAL FREQUENCY\n"+(d>=t?"DRAGON":"TIGER")+"\nDragon "+pct(d,total)+"%  |  Tie "+pct(e,total)+"%  |  Tiger "+pct(t,total)+"%");
        stats.setText("Dragon: "+d+" ("+pct(d,total)+"%)\nTie: "+e+" ("+pct(e,total)+"%)\nTiger: "+t+" ("+pct(t,total)+"%)\nPair: "+pair+" ("+pct(pair,total)+"%)");
        StringBuilder lr=new StringBuilder();int max=Math.min(10,total);for(int k=0;k<max;k++){Round x=rounds.get(total-1-k);if(k>0)lr.append("   |   ");lr.append(total-k).append(" ").append(label(x.r));}lastResults.setText(lr.toString());

        StringBuilder pat=new StringBuilder();int nPat=Math.min(3,total);for(int k=0;k<nPat;k++){if(k>0)pat.append("  →  ");pat.append(shortR(rounds.get(total-1-k).r));}patternText.setText("NEWEST → OLDEST:  "+pat);
        if(nPat==3){
            String target="";for(int k=0;k<3;k++)target+=shortR(rounds.get(total-1-k).r);
            int matches=0,cd=0,ct=0,ce=0;
            for(int idx=total-1-3;idx>=2;idx--){String q=shortR(rounds.get(idx).r)+shortR(rounds.get(idx-1).r)+shortR(rounds.get(idx-2).r);if(q.equals(target)){matches++;String next=rounds.get(idx-3).r;if("D".equals(next))cd++;else if("T".equals(next))ct++;else ce++;}}
            if(matches>0)patternAnalysis.setText("Historical matches: "+matches+"\nAfter this pattern: Dragon "+pct(cd,matches)+"% | Tie "+pct(ce,matches)+"% | Tiger "+pct(ct,matches)+"%\nReference: newest result first, then older results.");
            else patternAnalysis.setText("Historical matches: 0\nThis exact 3-result pattern was not found in the available history.");
        }else patternAnalysis.setText("Need 3 results for pattern analysis.");

        String newest=rounds.get(total-1).d;int nextD=0,nextT=0,nextE=0,trans=0;
        for(int i=total-1;i>0;i--){if(rounds.get(i).d.equals(newest)){trans++;String r=rounds.get(i-1).r;if("D".equals(r))nextD++;else if("T".equals(r))nextT++;else nextE++;}}
        transitionText.setText("Latest Dragon card rank: "+newest+"\nHistorical older-result frequency after this card: "+(trans==0?"No data":("Dragon "+pct(nextD,trans)+"% | Tie "+pct(nextE,trans)+"% | Tiger "+pct(nextT,trans)+"%")));
    }

    @Override protected void onResume(){super.onResume();if(lastResults!=null){rounds.clear();load();render();}}
}
