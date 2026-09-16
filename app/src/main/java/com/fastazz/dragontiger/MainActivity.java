package com.fastazz.dragontiger;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String[] CARDS={"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
    static final String[] RESULTS={"D","T","SAME"};
    static final String PREF="rounds";
    ArrayList<Round> rounds=new ArrayList<>();
    LinearLayout patternRow; TextView count, patternText, summary, patternAnalysis, numberAnalysis;
    Spinner ds,ts,rs;

    static class Round { String d,t,r; Round(String d,String t,String r){this.d=d;this.t=t;this.r=r;} }

    @Override public void onCreate(Bundle b) { super.onCreate(b); setContentView(R.layout.activity_main);
        count=findViewById(R.id.roundCount); patternRow=findViewById(R.id.patternRow); patternText=findViewById(R.id.patternText);
        summary=findViewById(R.id.summary); patternAnalysis=findViewById(R.id.patternAnalysis); numberAnalysis=findViewById(R.id.numberAnalysis);
        ds=findViewById(R.id.dragonSpinner); ts=findViewById(R.id.tigerSpinner); rs=findViewById(R.id.resultSpinner);
        ds.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,CARDS));
        ts.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,CARDS));
        rs.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,RESULTS));
        load(); render();
        findViewById(R.id.addButton).setOnClickListener(v->{ rounds.add(new Round(ds.getSelectedItem().toString(),ts.getSelectedItem().toString(),rs.getSelectedItem().toString())); save(); render(); });
        findViewById(R.id.clearButton).setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Clear all rounds?").setMessage("All saved results will be deleted.").setPositiveButton("CLEAR",(d,w)->{rounds.clear(); getPreferences(0).edit().putString(PREF,"").putBoolean("initialized",true).apply(); render();}).setNegativeButton("CANCEL",null).show());
    }

    void load() { String s=getPreferences(0).getString(PREF,""); boolean initialized=getPreferences(0).getBoolean("initialized",false); if(!initialized) { // preload the 505 rounds from the supplied game record
            String initial="1,10,10,D;2,2,6,T;3,Q,3,D;4,10,Q,T;5,Q,2,D;6,A,2,T;7,4,K,T;8,7,2,D;9,A,2,T;10,6,10,T;11,K,4,D;12,7,5,D;13,6,5,D;14,2,9,T;15,9,K,T;16,A,6,T;17,10,A,D;18,2,J,T;19,A,10,T;20,K,Q,D;21,K,2,D;22,9,A,D;23,10,K,T;24,7,9,T;25,6,3,D;26,7,9,T;27,K,5,D;28,5,7,T;29,J,J,T;30,J,4,D;31,9,J,T;32,7,9,T;33,J,10,D;34,K,6,D;35,9,A,D;36,5,7,T;37,Q,Q,SAME;38,3,K,T;39,A,A,D;40,Q,6,D;41,K,3,D;42,5,9,T;43,K,Q,D;44,J,8,D;45,6,K,T;46,6,10,T;47,7,K,T;48,4,Q,T;49,4,4,D;50,Q,3,D;51,J,Q,T;52,9,7,D;53,7,9,T;54,2,2,D;55,8,5,D;56,4,3,D;57,3,3,D;58,2,8,T;59,9,3,D;60,A,3,T;61,2,8,T;62,A,6,T;63,7,K,T;64,8,J,T;65,6,J,T;66,Q,J,D;67,3,4,T;68,2,2,T;69,3,K,T;70,6,J,T;71,9,2,D;72,8,6,D;73,4,9,T;74,10,7,D;75,Q,10,D;76,4,2,D;77,A,A,T;78,8,6,D;79,K,A,D;80,J,8,D;81,8,A,D;82,7,5,D;83,K,K,D;84,8,2,D;85,4,A,D;86,3,2,D;87,2,8,T;88,Q,J,D;89,9,8,D;90,9,Q,T;91,A,Q,T;92,8,K,T;93,A,A,SAME;94,6,Q,T;95,3,2,D;96,K,4,D;97,K,10,D;98,5,10,T;99,8,7,D;100,6,K,T;101,K,6,D;102,4,6,T;103,9,9,T;104,3,J,T;105,K,J,D;106,4,7,T;107,K,7,D;108,A,7,T;109,K,J,D;110,3,8,T;111,Q,2,D;112,3,4,T;113,8,7,D;114,9,4,D;115,5,4,D;116,3,J,T;117,K,Q,D;118,8,8,T;119,9,6,D;120,A,8,T;121,4,A,D;122,4,6,T;123,9,10,T;124,A,K,T;125,K,A,D;126,4,3,D;127,A,2,T;128,3,7,T;129,K,9,D;130,A,2,T;131,7,A,D;132,10,6,D;133,7,7,T;134,5,5,T;135,4,2,D;136,9,3,D;137,10,K,T;138,5,2,D;139,6,9,T;140,J,7,D;141,5,Q,T;142,6,5,D;143,2,2,D;144,K,7,D;145,8,K,T;146,7,3,D;147,8,J,T;148,J,10,D;149,9,9,T;150,5,7,T;151,9,6,D;152,A,K,T;153,Q,8,D;154,6,K,T;155,3,8,T;156,5,8,T;157,7,8,T;158,K,J,D;159,J,2,D;160,7,8,T;161,8,6,D;162,J,4,D;163,3,K,T;164,10,K,T;165,J,7,D;166,5,5,SAME;167,3,J,T;168,A,4,T;169,2,J,T;170,A,7,T;171,Q,10,D;172,2,A,D;173,7,J,T;174,10,Q,T;175,7,K,T;176,J,6,D;177,Q,4,D;178,J,7,D;179,9,K,T;180,4,2,D;181,2,A,D;182,Q,7,D;183,8,2,D;184,Q,5,D;185,3,10,T;186,7,J,T;187,2,8,T;188,3,K,T;189,3,2,D;190,6,4,D;191,2,10,T;192,9,2,D;193,K,K,SAME;194,5,2,D;195,K,6,D;196,3,6,T;197,5,6,T;198,9,K,T;199,6,9,T;200,6,10,T;201,Q,5,D;202,10,7,D;203,A,7,T;204,4,5,T;205,J,9,D;206,2,3,T;207,K,3,D;208,5,5,D;209,Q,4,D;210,8,6,D;211,2,A,D;212,K,K,SAME;213,Q,J,D;214,6,K,T;215,K,9,D;216,7,9,T;217,8,8,SAME;218,5,J,T;219,9,A,D;220,6,A,D;221,K,4,D;222,9,2,D;223,9,J,T;224,J,8,D;225,10,2,D;226,10,7,D;227,J,5,D;228,J,5,D;229,Q,2,D;230,4,8,T;231,9,7,D;232,10,J,T;233,2,3,T;234,7,6,D;235,2,9,T;236,10,J,T;237,J,8,D;238,A,9,T;239,5,2,D;240,Q,A,D;241,10,A,D;242,4,Q,T;243,8,7,D;244,6,K,T;245,4,10,T;246,5,5,T;247,6,K,T;248,6,Q,T;249,J,A,D;250,7,8,T;251,2,Q,T;252,4,J,T;253,7,9,T;254,J,7,D;255,4,3,D;256,2,3,T;257,5,7,T;258,9,9,D;259,6,8,T;260,J,2,D;261,K,J,D;262,6,7,T;263,K,9,D;264,6,Q,T;265,A,10,T;266,7,3,D;267,6,2,D;268,K,9,D;269,9,4,D;270,Q,5,D;271,7,3,D;272,9,A,D;273,K,3,D;274,A,7,T;275,9,A,D;276,10,2,D;277,4,10,T;278,3,10,T;279,5,3,D;280,7,2,D;281,K,Q,D;282,9,2,D;283,9,4,D;284,K,10,D;285,6,K,T;286,6,A,D;287,A,J,T;288,6,Q,T;289,7,10,T;290,7,10,T;291,9,5,D;292,8,Q,T;293,9,4,D;294,8,10,T;295,8,2,D;296,10,8,D;297,4,2,D;298,3,4,T;299,3,7,T;300,A,10,T;301,6,6,T;302,J,8,D;303,J,4,D;304,6,Q,T;305,4,8,T;306,7,A,D;307,10,K,T;308,J,3,D;309,6,6,T;310,3,J,T;311,Q,J,D;312,J,9,D;313,4,5,T;314,7,9,T;315,A,6,T;316,7,9,T;317,A,7,T;318,3,Q,T;319,A,10,T;320,Q,J,D;321,2,J,T;322,J,5,D;323,9,2,D;324,5,3,D;325,5,J,T;326,J,A,D;327,Q,Q,SAME;328,4,10,T;329,8,5,D;330,5,3,D;331,A,8,T;332,4,8,T;333,8,10,T;334,10,10,T;335,8,8,T;336,6,10,T;337,7,9,T;338,4,K,T;339,4,Q,T;340,6,7,T;341,J,J,D;342,10,J,T;343,2,9,T;344,2,J,T;345,5,8,T;346,A,Q,T;347,7,4,D;348,4,6,T;349,Q,9,D;350,J,10,D;351,Q,3,D;352,Q,J,D;353,A,8,T;354,9,J,T;355,5,K,T;356,9,6,D;357,9,3,D;358,7,8,T;359,3,4,T;360,8,6,D;361,Q,7,D;362,K,9,D;363,J,Q,T;364,J,5,D;365,2,A,D;366,3,5,T;367,4,9,T;368,K,8,D;369,A,2,T;370,Q,A,D;371,2,K,T;372,6,10,T;373,6,A,D;374,A,4,T;375,6,8,T;376,10,K,T;377,Q,5,D;378,K,8,D;379,4,J,T;380,7,7,D;381,6,J,T;382,2,J,T;383,6,7,T;384,8,9,T;385,8,Q,T;386,9,6,D;387,6,2,D;388,8,J,T;389,5,Q,T;390,7,2,D;391,9,7,D;392,6,J,T;393,K,8,D;394,Q,4,D;395,3,3,D;396,J,Q,T;397,Q,Q,D;398,Q,7,D;399,10,J,T;400,10,4,D;401,2,5,T;402,3,3,T;403,9,10,T;404,A,Q,T;405,K,3,D;406,7,A,D;407,9,A,D;408,4,5,T;409,8,J,T;410,A,2,T;411,5,3,D;412,9,5,D;413,10,K,T;414,8,6,D;415,A,J,T;416,5,A,D;417,9,7,D;418,A,3,T;419,2,8,T;420,7,7,D;421,8,5,D;422,3,3,SAME;423,2,8,T;424,5,2,D;425,J,5,D;426,A,A,T;427,10,Q,T;428,5,7,T;429,2,A,D;430,K,8,D;431,9,J,T;432,10,2,D;433,10,J,T;434,3,K,T;435,4,6,T;436,A,5,T;437,Q,7,D;438,8,7,D;439,2,4,T;440,A,A,T;441,J,2,D;442,10,6,D;443,6,A,D;444,2,10,T;445,10,3,D;446,6,A,D;447,9,2,D;448,K,Q,D;449,5,9,T;450,K,8,D;451,6,3,D;452,10,7,D;453,9,Q,T;454,10,9,D;455,6,2,D;456,8,3,D;457,9,8,D;458,5,A,D;459,9,3,D;460,K,J,D;461,8,Q,T;462,10,7,D;463,8,8,T;464,A,4,T;465,3,J,T;466,7,A,D;467,2,A,D;468,A,7,T;469,5,10,T;470,Q,A,D;471,2,7,T;472,4,3,D;473,9,K,T;474,4,8,T;475,4,2,D;476,A,7,T;477,2,8,T;478,3,7,T;479,A,Q,T;480,7,5,D;481,Q,K,T;482,6,9,T;483,A,8,T;484,A,K,T;485,Q,4,D;486,7,3,D;487,7,2,D;488,5,3,D;489,4,4,SAME;490,4,5,T;491,9,8,D;492,10,4,D;493,5,6,T;494,K,7,D;495,K,8,D;496,4,2,D;497,4,A,D;498,10,9,D;499,9,7,D;500,6,10,T;501,10,6,D;502,2,3,T;503,Q,K,T;504,J,A,D;505,7,6,D"; s=initial; getPreferences(0).edit().putBoolean("initialized",true).apply(); }
        if(!s.isEmpty()) for(String x:s.split(";")) { String[] a=x.split(","); if(a.length==3) rounds.add(new Round(a[0],a[1],a[2])); }
    }
    void save() { StringBuilder s=new StringBuilder(); for(Round x:rounds){if(s.length()>0)s.append(';');s.append(x.d).append(',').append(x.t).append(',').append(x.r);} getPreferences(0).edit().putString(PREF,s.toString()).apply(); }

    int idx(String c){return Arrays.asList(CARDS).indexOf(c);}
    String pct(int a,int n){return n==0?"0.0%":String.format(Locale.US,"%.1f%%",100.0*a/n);}
    void render() {
        count.setText("Rounds: "+rounds.size()); patternRow.removeAllViews();
        int start=Math.max(0,rounds.size()-10); StringBuilder last=new StringBuilder();
        for(int i=start;i<rounds.size();i++){ Round x=rounds.get(i); String v=x.r; last.append(v.equals("SAME")?"S":v);
            TextView c=new TextView(this); c.setText(v.equals("SAME")?"=":v); c.setTextColor(Color.WHITE); c.setTextSize(16); c.setGravity(17); c.setTypeface(null,1); c.setPadding(12,10,12,10);
            c.setBackgroundColor(v.equals("D")?Color.rgb(46,125,50):v.equals("T")?Color.rgb(239,108,0):Color.rgb(84,110,122));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,48,1); lp.setMargins(3,3,3,3); patternRow.addView(c,lp); }
        patternText.setText("Last 10: "+(last.length()==0?"-":last.toString())+"\nCurrent 3-pattern: "+currentPattern());
        renderSummary(); renderPatternAnalysis(); renderNumberAnalysis();
    }
    String currentPattern(){ if(rounds.size()<3)return "-"; StringBuilder s=new StringBuilder(); for(int i=rounds.size()-3;i<rounds.size();i++)s.append(rounds.get(i).r.equals("SAME")?"S":rounds.get(i).r); return s.toString(); }

    void renderSummary(){
        if(rounds.isEmpty()){summary.setText("No rounds entered.");return;}
        int d=0,t=0,s=0,sw=0; for(Round x:rounds){if(x.r.equals("D"))d++;else if(x.r.equals("T"))t++;else s++;}
        for(int i=1;i<rounds.size();i++)if(!rounds.get(i).r.equals(rounds.get(i-1).r))sw++;
        Round last=rounds.get(rounds.size()-1); summary.setText("Latest: Dragon "+last.d+" | Tiger "+last.t+" | Result "+last.r+"\nD: "+d+" ("+pct(d,rounds.size())+")   T: "+t+" ("+pct(t,rounds.size())+")   SAME: "+s+" ("+pct(s,rounds.size())+")\nResult changes: "+sw);
    }

    void renderPatternAnalysis(){
        if(rounds.size()<4){patternAnalysis.setText("कम से कम 4 rounds से pattern के बाद का result calculate होगा. 10+ rounds recommended.");return;}
        HashMap<String,int[]> map=new HashMap<>();
        for(int i=2;i<rounds.size()-1;i++){String p=patAt(i); String nr=rounds.get(i+1).r; int[] a=map.computeIfAbsent(p,k->new int[3]); a[nr.equals("D")?0:nr.equals("T")?1:2]++;}
        String cp=currentPattern(); int[] a=map.get(cp); StringBuilder out=new StringBuilder("Current pattern: "+cp+"\n");
        if(a==null) out.append("इस exact pattern का historical next-result record नहीं मिला."); else {int n=a[0]+a[1]+a[2];out.append("Occurrences with next result: ").append(n).append("\nD: ").append(a[0]).append(" ("+pct(a[0],n)+")   T: ").append(a[1]).append(" ("+pct(a[1],n)+")   SAME: ").append(a[2]).append(" ("+pct(a[2],n)+")");}
        out.append("\n\nAll 3-patterns:\n"); String[] pats={"DDD","DDT","DTD","DTT","TDD","TDT","TTD","TTT"}; for(String p:pats){int[] z=map.get(p); if(z==null)out.append(p+": no data\n");else{int n=z[0]+z[1]+z[2];out.append(p+": D "+pct(z[0],n)+" | T "+pct(z[1],n)+" | S "+pct(z[2],n)+" (n="+n+")\n");}} patternAnalysis.setText(out.toString());
    }
    String patAt(int i){StringBuilder s=new StringBuilder();for(int j=i-2;j<=i;j++)s.append(rounds.get(j).r.equals("D")?"D":rounds.get(j).r.equals("T")?"T":"S");return s.toString();}

    void renderNumberAnalysis(){
        if(rounds.size()<2){numberAnalysis.setText("2+ rounds needed.");return;}
        Round last=rounds.get(rounds.size()-1); StringBuilder o=new StringBuilder();
        o.append("After current Dragon card "+last.d+":\n"); appendCardNext(o,true,last.d);
        o.append("\nAfter current Tiger card "+last.t+":\n"); appendCardNext(o,false,last.t);
        numberAnalysis.setText(o.toString());
    }
    void appendCardNext(StringBuilder o,boolean dragon,String card){
        HashMap<String,Integer> next=new HashMap<>(); int d=0,t=0,s=0,total=0;
        for(int i=0;i<rounds.size()-1;i++){ Round cur=rounds.get(i); if((dragon?cur.d:cur.t).equals(card)){Round nx=rounds.get(i+1); String nc=dragon?nx.d:nx.t; next.put(nc,next.getOrDefault(nc,0)+1); if(nx.r.equals("D"))d++;else if(nx.r.equals("T"))t++;else s++;total++;}}
        ArrayList<Map.Entry<String,Integer>> list=new ArrayList<>(next.entrySet()); list.sort((a,b)->b.getValue()-a.getValue()); o.append("Most common next card: "); if(list.isEmpty())o.append("no data"); else {int show=Math.min(4,list.size());for(int i=0;i<show;i++){if(i>0)o.append(", ");o.append(list.get(i).getKey()).append(" (").append(list.get(i).getValue()).append(")");}}
        o.append("\nNext result: D ").append(pct(d,total)).append(" | T ").append(pct(t,total)).append(" | SAME ").append(pct(s,total)).append(" (n=").append(total).append(")\n");
    }
}
