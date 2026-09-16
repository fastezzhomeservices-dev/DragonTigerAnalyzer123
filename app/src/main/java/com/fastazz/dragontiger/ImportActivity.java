package com.fastazz.dragontiger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ImportActivity extends Activity {
    static final int PICK_FILE = 1001;
    TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(28, 36, 28, 28);
        TextView title = new TextView(this); title.setText("BULK DATA UPLOAD"); title.setTextSize(24); title.setGravity(17); box.addView(title);
        TextView help = new TextView(this);
        help.setText("Excel (.xlsx), CSV or TXT\n\nColumns: Round | Dragon | Tiger | Result\nExample: 506 | 7 | 5 | Dragon\n507 | A | K | Tiger\n508 | 9 | 9 | Tie\n\nNew data is added to existing history.");
        help.setTextSize(16); help.setPadding(0,24,0,24); box.addView(help);
        Button pick = new Button(this); pick.setText("SELECT EXCEL / CSV FILE"); box.addView(pick);
        status = new TextView(this); status.setTextSize(16); status.setPadding(0,24,0,0); box.addView(status);
        setContentView(box);
        pick.setOnClickListener(v -> { Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*"); i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","text/csv","text/plain","application/vnd.ms-excel"}); startActivityForResult(i,PICK_FILE); });
    }

    @Override protected void onActivityResult(int q,int code,Intent data) {
        super.onActivityResult(q,code,data); if(q!=PICK_FILE||code!=RESULT_OK||data==null||data.getData()==null)return;
        try {
            Uri uri=data.getData(); String p=uri.toString().toLowerCase(Locale.US);
            ArrayList<String> rows=p.endsWith(".xlsx")?parseXlsx(uri):parseText(read(uri));
            if(rows.isEmpty()){status.setText("No valid rows found. Use Round, Dragon, Tiger, Result columns.");return;}
            android.content.SharedPreferences pref=getSharedPreferences("MainActivity_preferences",MODE_PRIVATE);
            String old=pref.getString("rounds",""); StringBuilder merged=new StringBuilder(old);
            for(String r:rows){if(merged.length()>0)merged.append(';'); merged.append(r);}
            pref.edit().putString("rounds",merged.toString()).putBoolean("initialized",true).apply();
            status.setText("SUCCESS: "+rows.size()+" rows imported. Opening dashboard...");
            new android.os.Handler().postDelayed(()->{Intent x=new Intent(this,MainActivity.class);x.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(x);},500);
        } catch(Exception e){status.setText("Import error: "+e.getMessage());}
    }

    String read(Uri u)throws Exception{BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(u),"UTF-8"));StringBuilder s=new StringBuilder();String l;while((l=r.readLine())!=null)s.append(l).append('\n');r.close();return s.toString();}

    ArrayList<String> parseText(String text){
        ArrayList<String> out=new ArrayList<>();
        for(String raw:text.replace('\r','\n').split("[\\n;]+")){
            String[] a=raw.trim().split("[,\\t|]"); if(a.length<3)continue;
            int st=(a.length>=4&&a[0].trim().matches("\\d+"))?1:0; if(a.length-st<3)continue;
            String d=card(a[st]),t=card(a[st+1]),r=result(a[st+2]); if(d!=null&&t!=null&&r!=null)out.add(d+","+t+","+r);
        } return out;
    }

    ArrayList<String> parseXlsx(Uri u)throws Exception{
        ZipInputStream z=new ZipInputStream(getContentResolver().openInputStream(u)); Map<String,String> files=new HashMap<>(); ZipEntry e; byte[] buf=new byte[8192];
        while((e=z.getNextEntry())!=null){if(!e.isDirectory()){ByteArrayOutputStream b=new ByteArrayOutputStream();int n;while((n=z.read(buf))>0)b.write(buf,0,n);files.put(e.getName(),b.toString("UTF-8"));}} z.close();
        ArrayList<String> shared=new ArrayList<>(); String ss=files.get("xl/sharedStrings.xml");
        if(ss!=null){Document d=xml(ss);NodeList ts=d.getElementsByTagName("t");for(int i=0;i<ts.getLength();i++)shared.add(ts.item(i).getTextContent());}
        String sheet=files.get("xl/worksheets/sheet1.xml");if(sheet==null)throw new Exception("First worksheet not found"); Document doc=xml(sheet);NodeList cells=doc.getElementsByTagName("c");TreeMap<Integer,TreeMap<Integer,String>> rows=new TreeMap<>();
        for(int i=0;i<cells.getLength();i++){Element c=(Element)cells.item(i);String ref=c.getAttribute("r");String letters=ref.replaceAll("[0-9]","");String digits=ref.replaceAll("[^0-9]","");if(digits.isEmpty())continue;int col=0;for(char ch:letters.toCharArray())col=col*26+(Character.toUpperCase(ch)-'A'+1);int row=Integer.parseInt(digits);NodeList vs=c.getElementsByTagName("v");String val=vs.getLength()>0?vs.item(0).getTextContent():"";if("s".equals(c.getAttribute("t"))&&val.matches("\\d+")){int ix=Integer.parseInt(val);if(ix<shared.size())val=shared.get(ix);}rows.computeIfAbsent(row,k->new TreeMap<>()).put(col,val);}
        ArrayList<String> out=new ArrayList<>(); for(TreeMap<Integer,String> rr:rows.values()){ArrayList<String>a=new ArrayList<>(rr.values());if(a.size()<3)continue;int st=(a.size()>=4&&a.get(0).trim().matches("\\d+"))?1:0;if(a.size()-st<3)continue;String d=card(a.get(st)),t=card(a.get(st+1)),r=result(a.get(st+2));if(d!=null&&t!=null&&r!=null)out.add(d+","+t+","+r);}return out;
    }
    Document xml(String s)throws Exception{return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));}
    String card(String s){s=s.trim().toUpperCase(Locale.US);for(String c:MainActivity.CARDS)if(c.equals(s))return c;return null;}
    String result(String s){s=s.trim().toUpperCase(Locale.US);if(s.equals("D")||s.equals("DRAGON"))return "D";if(s.equals("T")||s.equals("TIGER"))return "T";if(s.equals("S")||s.equals("SAME")||s.equals("TIE"))return "SAME";return null;}
}
