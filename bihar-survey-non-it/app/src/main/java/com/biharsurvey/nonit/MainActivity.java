package com.biharsurvey.nonit;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import android.util.Base64;

public class MainActivity extends AppCompatActivity {
    static final String APP="BIHAR SURVEY NON IT";
    static final LinkedHashMap<String,String> USERS=new LinkedHashMap<>();
    static { for(int i=1;i<=20;i++) USERS.put("ADMIN"+i,"India@"+(char)('a'+i-1)+"123"); }

    LinearLayout root,body; TextView gps,countText; double lat,lon;
    String activeCategory="Building", suggestedType="";
    static final int MAX_BATCH=500;
    static final String PREF_THEME="theme";
    static final String PREF_LOCATIONS="survey_locations";
    static final String PREF_SECTIONS_PREFIX="survey_sections_";
    ArrayList<Entry> batch=new ArrayList<>();
    SurveyDbHelper db; ExecutorService io=Executors.newSingleThreadExecutor();
    Uri pendingPhotoUri; String pendingPhotoPath="";
    ActivityResultLauncher<Uri> takePhoto;
    final String[] categories={"Building","Office Land","Office Equipment","Office Furniture","Residential Colony","Vehicle","Plant & Machinery","Store Inventory","Edit/Rejected Entry"};
    final Map<String,String[]> HEADERS=new LinkedHashMap<>();
    final HashMap<String,View> fieldViews=new HashMap<>();
    ArrayList<String[]> hierarchy=new ArrayList<>();

    static class Entry {
        String category,photoPath="";
        LinkedHashMap<String,String> values=new LinkedHashMap<>();
        double lat,lon; long time;
    }

    final ActivityResultLauncher<String[]> perms=registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),r->updateGps());

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        takePhoto=registerForActivityResult(new ActivityResultContracts.TakePicture(),ok->{if(ok&&pendingPhotoUri!=null)runVision(pendingPhotoUri);else deletePendingPhoto();});
        buildHeaders(); db=new SurveyDbHelper(this); loadHierarchy();
        if(!loggedIn()){login();return;} home(); requestPerms();
    }

    void buildHeaders(){
        HEADERS.put("Building",new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchylevel","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Building Type","Building Name","Building Type Other","Year Constructed","Owenership","Office Name","Colony Name","No of Quarter","No of Households per Floor","No of Floor","Area Unit","Builtup Area","Condititon","Address","Fire Safety","Power Connection","Lift Available","Occupancy","Scheme Name","Registration Date Available","Registry No","Registration Date","In-Operational Date","Verified Date","Status","Remarks"});
        HEADERS.put("Office Land",new String[]{"Unique Id","ENTRYDATE","Surveyor","HierarchyLevel","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Sectio Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pin Code","Land Type","Land Ownership","Office Land Type Other","Land Uses","Record Id","Plot No","Area Unit","Area Acres","Survey Number","Land North","Land South","LAND East","Land West","Document Refrence","Condition","Status","Encriachment","Legal Status","Remarks","Scheme Name","Is Registration Date","Registry No","Registration Date","Doc Reference","Address"});
        HEADERS.put("Office Equipment",new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Store","Building Name","Status","Room Section","Asset Lifecycle Status","Item Type","Equipment Type","Fan Type","Motor Hub","Cooler Type","Size","Capacity","Working condition","Filter condition","Heater Type","Wattage","Installed Capacity DC","Rated AC Power","Module Technology","Mounting Technology","Number of Modules","Number of Inverters","Inverter Rated Power","Commissioning Date","Grid Connection Type","Effective Roof Area","Installation Type","Solar Condition","Office Name","Condition","Scheme Name","Floor","Manufacturing Date","Model Name","Remarks","Warranty Date","Warranty Expiry Date","Serial Number","Warranty","Purchase Order Number","Purchase Order Date","AMC","Manufacturing Name","feature Code","Census Code","AMC Expiry Date","Date of Commissioning","Residual Value","In-operational Date","Building","Address","Storage","Equipment Number","Working Usable","Schema Name","Ram","Graphics Card","Designation","Processor","AC Type","Used by Person Name","AC Capacity","Model No","Printer Size","Ac Type Other","Equipment Type Other","Starting Method","Temperature Rise","Degree Protection","Battery Quantity","Rated Voltage","Generator Model","Excitation Type","No of Phases","Charger Output","Governor Type","Battery Type","Insulation Class","Neutral Formation","Fuel Type","Frequency HZ","Overload Capacity","Terminal Box","Engine Type","Rated RPM","Battery Voltage","Aspiration Type","Charger Type","Fuel Pump Type","Cooling Type","Generator Type","Power Factor","Generator Capacity KVA","Battery Capacity","Fuel Injection"," Fuel Tank Capacity","Location","Cable Size","LOA Number","Noise Level","Acoustic Material","Lubricanting System","Mounting Type","Loa Date","Cylinder Pressure Bar","Hose Temperature Range","Lancing Distance","Face Mask Type","Fire Rating","SCBA Available","Cylinder Material","Extinguising Medium","Nozzle Type","Cylinder Type","Foam Type","Pressure Gauge","fire Capacity","Totalweight KG"," Working Duration Min","Fire Extinguisher Type","Back Plate Type","Warning Wristle","Maximum Operating Temperature","Minimum Operating Temperature","Pressure Reducer","Laboratory Test"});
        HEADERS.put("Office Furniture",new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Building","Office","Store","Floor","Room Section","Model","Equipment Type","Manufacturer Name","Condition","Assetstatus","Manufacturingdate","Quantity","Material","Eam","Feature Code","Census","Status","Remarks","In-Operational Date","Approved By","Amc Date","Warranty","Purchase Order No","Purshase Order Date","Warranty Date","Amc","Residual Value","Date Commission","Item Type","Bed Size","Bed Material","Bed Conditon","Working Usable","Building Name","Address","Designation","Used by","Schema Name","New Address","Table Type","Seater","Size","Other Item Type"});
        HEADERS.put("Residential Colony",new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Feature Code","Address","Store Name","Serial Number","Gis Unique Id","Area Acres","Land Type","Residual Value","Condition","Legal Status","Status","Encroachment","Commissioning Date","In-Operational Date","Scheme Name","Registration Date","Purchase Order Date","Remarks","Purchase Number","House Type","Parking","No of House/Flat","Colony Name","Fitness Valid Date","Registry No","Is Registration Date Available","Colony Type","Colony Type Other","House Type Other"});
    }

    void loadHierarchy(){
        try{
            InputStream raw=getAssets().open("discom_hierarchy.csv.gz.b64");
            StringBuilder sb=new StringBuilder(); BufferedReader br=new BufferedReader(new InputStreamReader(raw));
            String line; while((line=br.readLine())!=null) sb.append(line.trim()); br.close();
            byte[] gz=Base64.decode(sb.toString(),Base64.DEFAULT);
            GZIPInputStream gin=new GZIPInputStream(new ByteArrayInputStream(gz));
            BufferedReader csv=new BufferedReader(new InputStreamReader(gin));
            csv.readLine();
            while((line=csv.readLine())!=null){String[] p=line.split(",",-1);if(p.length>=6)hierarchy.add(p);}
            csv.close();
        }catch(Exception ignored){}
    }

    boolean loggedIn(){return getPreferences(0).getBoolean("login",false);}
    String currentUser(){return getPreferences(0).getString("user_id","");}

    void login(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(24),dp(45),dp(24),dp(24)); root.setBackgroundColor(bg());
        ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.ic_app_logo); logo.setPadding(dp(12),dp(12),dp(12),dp(12)); root.addView(logo,new LinearLayout.LayoutParams(-1,dp(110)));
        TextView h=title(APP); h.setGravity(Gravity.CENTER); root.addView(h); root.addView(title("Secure Common Login"));
        EditText u=input("Login ID"),p=input("Password"); p.setInputType(129); root.addView(cardWrap(u)); root.addView(cardWrap(p));
        Button b=primary("LOGIN"); root.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));
        b.setOnClickListener(v->{String uid=u.getText().toString().trim().toUpperCase(Locale.US);String pw=p.getText().toString();if(USERS.containsKey(uid)&&USERS.get(uid).equals(pw)){getPreferences(0).edit().putBoolean("login",true).putString("user_id",uid).apply();home();requestPerms();}else Toast.makeText(this,"Invalid common login",Toast.LENGTH_SHORT).show();});
        setContentView(root);
    }

    void home(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg());
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(8),dp(7),dp(8),dp(7)); header.setBackground(round(themeHeader(),0));
        TextView menu=label("☰"); menu.setTextColor(Color.WHITE); menu.setTextSize(28); menu.setGravity(Gravity.CENTER); header.addView(menu,new LinearLayout.LayoutParams(dp(48),dp(58))); menu.setOnClickListener(v->showSidebar());
        ImageView mark=new ImageView(this); mark.setImageResource(R.drawable.ic_app_logo); mark.setPadding(dp(8),dp(8),dp(8),dp(8)); header.addView(mark,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);TextView app=label(APP);app.setTextColor(Color.WHITE);app.setTextSize(18);app.setTypeface(null,1);ht.addView(app);TextView sub=label("Bihar State Power • Asset Survey");sub.setTextColor(0xffe8f7ff);sub.setTextSize(12);ht.addView(sub);header.addView(ht,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(header);
        LinearLayout info=sectionCard(); LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        TextView usr=label("USER\n"+currentUser());usr.setTextColor(primaryText());row.addView(usr,new LinearLayout.LayoutParams(0,-2,1));
        TextView gpsHome=label("GPS\n"+(Math.abs(lat)>0.000001?String.format(Locale.US,"%.5f, %.5f",lat,lon):"Waiting…"));gpsHome.setTextColor(0xff23633a);row.addView(gpsHome,new LinearLayout.LayoutParams(0,-2,1));
        TextView saved=label("SAVED\n"+db.count());saved.setTextColor(primaryText());saved.setGravity(Gravity.RIGHT);row.addView(saved,new LinearLayout.LayoutParams(0,-2,1));info.addView(row);root.addView(info);
        countText=label("SELECT SURVEY CATEGORY");countText.setTextColor(primaryText());root.addView(countText);
        ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),dp(2),dp(14),dp(24));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout bottom=new LinearLayout(this);bottom.setPadding(dp(14),dp(6),dp(14),dp(10));
        Button ex=smallButton("EXPORT EXCEL");bottom.addView(ex,new LinearLayout.LayoutParams(0,dp(46),1));ex.setOnClickListener(v->exportExcel());
        Button logout=smallButton("LOGOUT");LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(dp(8),0,0,0);bottom.addView(logout,lp);logout.setOnClickListener(v->{getPreferences(0).edit().clear().apply();login();});root.addView(bottom);
        setContentView(root);
        for(int i=0;i<categories.length;i++){String c=categories[i];Button x=categoryButton(c);x.setText((i+1)+"   "+c);body.addView(x,new LinearLayout.LayoutParams(-1,dp(54)));x.setOnClickListener(v->{if(c.equals("Edit/Rejected Entry"))showSaved();else if(HEADERS.containsKey(c))form(c);else Toast.makeText(this,"Exact field list for "+c+" is not present in the supplied Excel.",Toast.LENGTH_LONG).show();});}
    }

    void showSidebar(){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(18),dp(22),dp(18),dp(18));panel.setBackgroundColor(surface());
        TextView h=title("THEME");panel.addView(h);TextView info=label("Choose interface theme");panel.addView(info);
        Button a=smallButton("1 • Light Blue");Button b=smallButton("2 • Clean White");Button c=smallButton("3 • Dark");panel.addView(a,new LinearLayout.LayoutParams(-1,dp(50)));panel.addView(b,new LinearLayout.LayoutParams(-1,dp(50)));panel.addView(c,new LinearLayout.LayoutParams(-1,dp(50)));
        TextView app=label("\n"+APP+"\nUser: "+currentUser());app.setTextColor(primaryText());panel.addView(app);
        PopupWindow pw=new PopupWindow(panel,Math.round(getResources().getDisplayMetrics().widthPixels*0.82f),-1,true);pw.setBackgroundDrawable(round(Color.WHITE,0));pw.setOutsideTouchable(true);pw.setElevation(dp(10));
        a.setOnClickListener(v->{setTheme(0);pw.dismiss();home();});b.setOnClickListener(v->{setTheme(1);pw.dismiss();home();});c.setOnClickListener(v->{setTheme(2);pw.dismiss();home();});pw.showAtLocation(root,Gravity.START|Gravity.TOP,0,0);
    }

    void setTheme(int t){getPreferences(0).edit().putInt(PREF_THEME,t).apply();}
    int theme(){return getPreferences(0).getInt(PREF_THEME,0);}
    int bg(){return theme()==2?0xff101820:(theme()==1?0xfffafafa:0xfff3f7fb);}
    int surface(){return theme()==2?0xff1d2730:0xffffffff;}
    int primaryText(){return theme()==2?Color.WHITE:0xff14283a;}
    int secondaryText(){return theme()==2?0xffd6e0e8:0xff354d61;}
    int themeHeader(){return theme()==2?0xff18343f:(theme()==1?0xff0b6672:0xff0756b5);}

    void form(String cat){
        activeCategory=cat;batch.clear();suggestedType="";fieldViews.clear();body.removeAllViews();body.addView(title(cat+"  •  Survey"));
        TextView note=label("Organization Hierarchy • Asset Details & Identification • Lifecycle Maintenance & Verification");note.setTextColor(secondaryText());body.addView(note);
        addHierarchyControls();
        LinearLayout formBox=sectionCard();body.addView(formBox);
        String[] hs=HEADERS.get(cat);for(String h:hs){
            if(isHierarchyField(h)||h.equals("Unique Id")||h.equals("ENTRYDATE"))continue;
            formBox.addView(fieldLabel(h,isMandatory(cat,h)));
            View v=createField(h);fieldViews.put(h,v);formBox.addView(v);
        }
        LinearLayout actions=sectionCard();body.addView(actions);
        Button cam=primary("TAKE PHOTO + OCR / IDENTIFY");actions.addView(cam,new LinearLayout.LayoutParams(-1,dp(54)));cam.setOnClickListener(v->startCamera());
        Button review=primary("DATA REVIEW • BEFORE SAVE");actions.addView(review,new LinearLayout.LayoutParams(-1,dp(54)));review.setOnClickListener(v->reviewBeforeSave());
        TextView cap=label("Batch: "+batch.size()+" / "+MAX_BATCH+" • Photo is mandatory • GPS is mandatory");cap.setTextColor(secondaryText());actions.addView(cap);countText=cap;
        Button back=smallButton("BACK HOME");actions.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));back.setOnClickListener(v->home());
    }

    void addHierarchyControls(){
        LinearLayout box=sectionCard();body.addView(box);box.addView(fieldLabel("Discom / Organization Name",true));
        AutoCompleteTextView discom=dropdownInput("Select Discom");fieldViews.put("Discom/ Organization Name",discom);box.addView(discom);
        bind(discom,new ArrayList<>(Arrays.asList("NBPDCL","SBPDCL")));
        box.addView(fieldLabel("Zone Name",true));AutoCompleteTextView zone=dropdownInput("Select Zone");fieldViews.put("Zone Name",zone);box.addView(zone);
        box.addView(fieldLabel("Circle Name",true));AutoCompleteTextView circle=dropdownInput("Select Circle");fieldViews.put("Circle Name",circle);box.addView(circle);
        box.addView(fieldLabel("Division Name",true));AutoCompleteTextView div=dropdownInput("Select Division");fieldViews.put("Division Name",div);box.addView(div);
        box.addView(fieldLabel("Subdivision Name",true));AutoCompleteTextView sub=dropdownInput("Select Sub Division");fieldViews.put("Subdivision Name",sub);box.addView(sub);
        box.addView(fieldLabel("Section Name",true));AutoCompleteTextView sec=dropdownInput("Select Section");fieldViews.put("Section Name",sec);box.addView(sec);
        box.addView(fieldLabel("Substation Name",false));AutoCompleteTextView st=dropdownInput("Select Substation");fieldViews.put("Substation Name",st);box.addView(st);
        AdapterView.OnItemClickListener refresh=(p,v,pos,id)->refreshHierarchy();
        discom.setOnItemClickListener(refresh);zone.setOnItemClickListener(refresh);circle.setOnItemClickListener(refresh);div.setOnItemClickListener(refresh);sub.setOnItemClickListener(refresh);sec.setOnItemClickListener(refresh);
        refreshHierarchy();
        box.addView(fieldLabel("State",true));EditText state=input("State");state.setText("Bihar");state.setEnabled(false);fieldViews.put("State",state);box.addView(state);
        box.addView(fieldLabel("District Name",true));AutoCompleteTextView district=dropdownInput("Select District");fieldViews.put("District Name",district);box.addView(district);
        box.addView(fieldLabel("Block Name",true));EditText block=input("Block Name");fieldViews.put("Block Name",block);box.addView(block);
        box.addView(fieldLabel("Gram Panchyat Name",true));EditText gp=input("Gram Panchayat / Municipality");fieldViews.put("Gram Panchyat Name",gp);box.addView(gp);
        box.addView(fieldLabel("Village Name",true));EditText village=input("Village / Ward");fieldViews.put("Village Name",village);box.addView(village);
        box.addView(fieldLabel("Pincode",true));EditText pin=input("Pincode");pin.setInputType(2);fieldViews.put("Pincode",pin);box.addView(pin);
    }

    void refreshHierarchy(){
        String dis=get("Discom/ Organization Name"),z=get("Zone Name"),c=get("Circle Name"),d=get("Division Name"),s=get("Subdivision Name"),sec=get("Section Name");
        if(dis.equals("SBPDCL")){Toast.makeText(this,"SBPDCL division data is not present in the supplied Excel.",Toast.LENGTH_LONG).show();return;}
        setOptions("Zone Name",filter(0,dis,""));
        setOptions("Circle Name",filter(1,dis,z));
        setOptions("Division Name",filter(2,dis,z,c));
        setOptions("Subdivision Name",filter(3,dis,z,c,d));
        setOptions("Section Name",filter(4,dis,z,c,d,s));
        setOptions("Substation Name",filter(5,dis,z,c,d,s,sec));
    }

    ArrayList<String> filter(int col,String dis,String... parents){
        LinkedHashSet<String> out=new LinkedHashSet<>();for(String[] r:hierarchy){if(!r[0].equalsIgnoreCase(dis))continue;boolean ok=true;for(int i=0;i<parents.length;i++){String p=parents[i];if(p==null||p.isEmpty())continue;if(!r[i+1].equalsIgnoreCase(p)){ok=false;break;}}if(ok)out.add(r[col]);}return new ArrayList<>(out);
    }

    void setOptions(String key,ArrayList<String> list){View v=fieldViews.get(key);if(v instanceof AutoCompleteTextView)bind((AutoCompleteTextView)v,list);}
    boolean isHierarchyField(String h){String x=h.toLowerCase(Locale.US);return x.contains("discom")||x.contains("zone name")||x.contains("circle name")||x.contains("division name")||x.contains("subdivision name")||x.contains("section name")||x.contains("state")||x.contains("district name")||x.contains("block name")||x.contains("gram panch")||x.contains("village name")||x.contains("pincode");}

    View createField(String h){
        String x=h.toLowerCase(Locale.US);
        if(x.contains("date")||x.contains("commissioning")||x.contains("in-operational")||x.contains("expiry")||x.contains("warranty date")){EditText e=input(h);e.setFocusable(false);e.setOnClickListener(v->datePick(e));return e;}
        if(x.equals("working condition")||x.equals("working usable")||x.equals("warranty expiry date")||x.equals("registration date available")||x.equals("is registration date"))return dropdownInputWithOptions(new String[]{"--Select--","Yes","No"},h);
        if(x.contains("condition"))return dropdownInputWithOptions(new String[]{"--Select--","Good","Fair","Poor","Not Working"},h);
        if(x.contains("asset lifecycle"))return dropdownInputWithOptions(new String[]{"--Select--","In Use","Active","Under Maintenance","Idle","Disposed"},h);
        if(x.equals("floor")||x.equals("size")||x.equals("capacity"))return dropdownInputWithOptions(new String[]{"--Select--","Ground Floor","1st Floor","2nd Floor","3rd Floor","4th Floor","5th Floor","Other"},h);
        if(x.equals("equipment type")||x.equals("item type"))return dropdownInputWithOptions(new String[]{"--Select--","Computer","Printer","AC","Cooler","Fan","Furniture","Generator","Solar","Other"},h);
        if(x.equals("amc")||x.equals("warranty"))return dropdownInputWithOptions(new String[]{"--Select--","Yes","No","Not Available"},h);
        if(x.equals("status"))return dropdownInputWithOptions(new String[]{"--Select--","Active","Inactive","Working","Not Working","Disposed"},h);
        if(x.equals("remarks")){EditText e=input(h);e.setMinLines(3);e.setGravity(Gravity.TOP);return e;}
        return input(h);
    }

    boolean isMandatory(String cat,String h){
        if(!cat.equals("Office Equipment"))return false;
        String x=h.toLowerCase(Locale.US).trim();
        String[] m={"room section","designation","used by person name","schema name","building name","office name","working usable","item type","warranty date","asset lifecycle status","equipment type","floor","manufacturing date","model name","remarks","warranty expiry date","serial number","purchase order number","manufacturing name","in-operational date","equipment number","graphics card","processor"};
        for(String q:m)if(x.equals(q)||x.contains(q))return true;
        return false;
    }

    void reviewBeforeSave(){
        String missing=validateMandatory();if(!missing.isEmpty()){Toast.makeText(this,"Mandatory fields missing: "+missing,Toast.LENGTH_LONG).show();return;}
        final Entry e=new Entry();e.category=activeCategory;e.time=System.currentTimeMillis();e.lat=lat;e.lon=lon;e.photoPath=pendingPhotoPath;
        for(String h:HEADERS.get(activeCategory))e.values.put(h,get(h));
        e.values.put("ENTRYDATE",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(e.time)));e.values.put("Unique Id",String.valueOf(e.time));
        final ScrollView sv=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(8),dp(20),dp(8));
        box.addView(title("Data Review"));StringBuilder sb=new StringBuilder();for(Map.Entry<String,String> en:e.values.entrySet())if(!en.getValue().isEmpty())sb.append(en.getKey()).append(" : ").append(en.getValue()).append("\n");sb.append("\nGPS : ").append(String.format(Locale.US,"%.6f, %.6f",lat,lon)).append("\nPhoto : ").append(e.photoPath.isEmpty()?"MISSING":"READY");TextView d=label(sb.toString());d.setTextColor(primaryText());box.addView(d);sv.addView(box);
        new AlertDialog.Builder(this).setView(sv).setNegativeButton("EDIT",null).setPositiveButton("SAVE",(dialog,which)->{batch.add(e);saveBatch();}).show();
    }

    String validateMandatory(){for(String h:HEADERS.get(activeCategory))if(isMandatory(activeCategory,h)){String v=get(h);if(v.trim().isEmpty())return h;}if(pendingPhotoPath.isEmpty()||!new File(pendingPhotoPath).exists())return "Photo";if(Math.abs(lat)<0.000001&&Math.abs(lon)<0.000001)return "GPS";return "";}

    String get(String h){View v=fieldViews.get(h);if(v instanceof TextView)return ((TextView)v).getText().toString().trim();return "";}
    void put(String h,String v){View x=fieldViews.get(h);if(x instanceof TextView)((TextView)x).setText(v);}

    void startCamera(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPerms();return;}
        try{String id=String.valueOf(System.currentTimeMillis());File dir=new File(getExternalFilesDir(null),"survey_photos");if(!dir.exists())dir.mkdirs();File f=new File(dir,"ITEM_"+id+".jpg");pendingPhotoPath=f.getAbsolutePath();pendingPhotoUri=FileProvider.getUriForFile(this,"com.biharsurvey.nonit.fileprovider",f);takePhoto.launch(pendingPhotoUri);}catch(Exception e){Toast.makeText(this,"Camera start failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

    void runVision(Uri uri){
        try{InputImage img=InputImage.fromFilePath(this,uri);TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(img).addOnSuccessListener(r->{String t=r.getText()==null?"":r.getText().trim();suggestedType=t.length()>80?t.substring(0,80):t;ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS).process(img).addOnSuccessListener(labels->{if(suggestedType.isEmpty()&&!labels.isEmpty())suggestedType=labels.get(0).getText();if(fieldViews.containsKey("Equipment Type")&&get("Equipment Type").isEmpty())put("Equipment Type",suggestedType);Toast.makeText(this,"Detected: "+(suggestedType.isEmpty()?"Review manually":suggestedType),Toast.LENGTH_LONG).show();}).addOnFailureListener(e->Toast.makeText(this,"Photo saved. Image identify unavailable.",Toast.LENGTH_SHORT).show());}).addOnFailureListener(e->Toast.makeText(this,"Photo saved. OCR unavailable.",Toast.LENGTH_SHORT).show());}catch(Exception e){Toast.makeText(this,"Photo saved. OCR error.",Toast.LENGTH_SHORT).show();}
    }

    void saveBatch(){
        if(batch.isEmpty())return;final ArrayList<Entry> copy=new ArrayList<>(batch);
        io.execute(()->{for(Entry e:copy){try{e.values.put("Latitude",String.valueOf(e.lat));e.values.put("Longitude",String.valueOf(e.lon));}catch(Exception ignored){}try{db.insertFull(e.category,e.values.get("Unique Id"),e.values.get("Office Name"),e.values.get("Section Name"),e.values.get("Surveyor"),e.values.get("Item Type"),e.photoPath,e.lat,e.lon,e.time,"SAVED",new JSONObject(e.values).toString());}catch(Exception ignored){}}runOnUiThread(()->{batch.clear();pendingPhotoPath="";Toast.makeText(this,"Saved successfully: "+copy.size()+" item(s).",Toast.LENGTH_LONG).show();home();});});
    }

    void showSaved(){
        ArrayList<SurveyDbHelper.EntryRow> rows=db.all();body.removeAllViews();body.addView(title("SAVED / REJECTED ENTRIES"));body.addView(label("Total records: "+rows.size()));
        for(SurveyDbHelper.EntryRow e:rows){LinearLayout c=sectionCard();c.addView(label(e.category+" • "+e.itemType));TextView d=label(e.office+" | "+e.section+"\n"+e.uniqueId+"\nGPS: "+String.format(Locale.US,"%.6f, %.6f",e.lat,e.lon));d.setTextColor(secondaryText());c.addView(d);Button del=smallButton("DELETE");c.addView(del);del.setOnClickListener(v->{db.delete(e.id);deletePhoto(e.photoPath);showSaved();});body.addView(c);}
        Button back=smallButton("BACK HOME");body.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));back.setOnClickListener(v->home());
    }

    void exportExcel(){
        if(db.count()==0){Toast.makeText(this,"No saved data.",Toast.LENGTH_SHORT).show();return;}final ProgressDialog p=ProgressDialog.show(this,"Exporting","Please wait…",true,false);
        io.execute(()->{try{Workbook wb=new XSSFWorkbook();Map<String,Sheet>m=new HashMap<>();for(String c:HEADERS.keySet()){Sheet ws=wb.createSheet(c);m.put(c,ws);Row h=ws.createRow(0);String[] hs=HEADERS.get(c);for(int i=0;i<hs.length;i++)h.createCell(i).setCellValue(hs[i]);int n=hs.length;h.createCell(n).setCellValue("Latitude");h.createCell(n+1).setCellValue("Longitude");h.createCell(n+2).setCellValue("Photo Link");h.createCell(n+3).setCellValue("Item Identity");h.createCell(n+4).setCellValue("Survey Date");h.createCell(n+5).setCellValue("Survey Time");}
            for(SurveyDbHelper.EntryRow e:db.all()){Sheet ws=m.get(e.category);if(ws==null)continue;Row r=ws.createRow(ws.getLastRowNum()+1);try{JSONObject j=new JSONObject(e.payload==null?"{}":e.payload);String[] hs=HEADERS.get(e.category);for(int i=0;i<hs.length;i++)r.createCell(i).setCellValue(j.optString(hs[i],""));int n=hs.length;r.createCell(n).setCellValue(e.lat);r.createCell(n+1).setCellValue(e.lon);r.createCell(n+2).setCellValue(e.photoPath);r.createCell(n+3).setCellValue(e.category+" - "+e.itemType+" - "+e.uniqueId);String dt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(e.time));r.createCell(n+4).setCellValue(dt.substring(0,10));r.createCell(n+5).setCellValue(dt.substring(11));}catch(Exception ignored){}}
            File out=new File(getExternalFilesDir(null),"BIHAR_SURVEY_NON_IT.xlsx");FileOutputStream f=new FileOutputStream(out);wb.write(f);f.close();wb.close();runOnUiThread(()->{p.dismiss();Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");i.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(this,"com.biharsurvey.nonit.fileprovider",out));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share Excel"));});}catch(Exception e){runOnUiThread(()->{p.dismiss();Toast.makeText(this,"Excel export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();});}});
    }

    void datePick(EditText target){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->target.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    AutoCompleteTextView dropdownInput(String hint){AutoCompleteTextView v=new AutoCompleteTextView(this);v.setHint(hint);v.setTextSize(15);v.setSingleLine(true);v.setThreshold(0);styleInput(v);return v;}
    AutoCompleteTextView dropdownInputWithOptions(String[] opts,String hint){AutoCompleteTextView v=dropdownInput(hint);bind(v,new ArrayList<>(Arrays.asList(opts)));return v;}
    void bind(AutoCompleteTextView v,ArrayList<String> items){v.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,items));v.setOnClickListener(x->v.showDropDown());v.setOnFocusChangeListener((x,f)->{if(f)v.showDropDown();});}
    void styleInput(View v){v.setPadding(dp(12),0,dp(12),0);v.setBackground(round(surface(),10));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(50));p.setMargins(0,dp(3),0,dp(8));v.setLayoutParams(p);}

    EditText input(String h){EditText e=new EditText(this);e.setHint(h);e.setTextSize(16);e.setTextColor(primaryText());e.setHintTextColor(theme()==2?0xffaebbc6:0xff566777);e.setPadding(dp(12),dp(8),dp(12),dp(8));styleInput(e);return e;}
    TextView fieldLabel(String s,boolean req){TextView t=label(s+(req?" *":""));t.setTextColor(primaryText());t.setTextSize(14);return t;}
    TextView title(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(primaryText());t.setTextSize(22);t.setTypeface(null,1);t.setPadding(dp(4),dp(10),dp(4),dp(10));return t;}
    TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(14);t.setTextColor(secondaryText());t.setPadding(dp(4),dp(7),dp(4),dp(7));return t;}
    View cardWrap(View v){LinearLayout c=sectionCard();c.addView(v);return c;}
    LinearLayout sectionCard(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(12),dp(10),dp(12),dp(10));c.setBackground(round(surface(),14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(6),0,dp(6));c.setLayoutParams(p);return c;}
    Button primary(String s){Button b=btn(s);b.setTextColor(Color.WHITE);b.setBackground(round(0xff1769aa,14));return b;}
    Button categoryButton(String s){Button b=btn(s);b.setTextColor(primaryText());b.setGravity(Gravity.CENTER);b.setAllCaps(false);b.setBackground(round(theme()==2?0xff26343f:(theme()==1?0xffeaf3f7:0xffc7eaf3),16));return b;}
    Button smallButton(String s){Button b=btn(s);b.setTextSize(12);b.setTextColor(theme()==2?0xffd9f4ff:0xff1769aa);b.setBackground(round(surface(),12));return b;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(13);b.setAllCaps(false);b.setPadding(dp(8),0,dp(8),0);return b;}
    GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));g.setStroke(dp(1),theme()==2?0xff42525e:0xffcbdbe7);return g;}

    void requestPerms(){perms.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CAMERA});}
    void updateGps(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){if(gps!=null)gps.setText("GPS permission required");return;}LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location l=null;try{l=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(l==null)l=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}catch(SecurityException ignored){}if(l!=null){lat=l.getLatitude();lon=l.getLongitude();if(gps!=null)gps.setText(String.format(Locale.US,"GPS: %.6f, %.6f ✓",lat,lon));}}
    void deletePendingPhoto(){if(!pendingPhotoPath.isEmpty())deletePhoto(pendingPhotoPath);pendingPhotoPath="";pendingPhotoUri=null;}
    void deletePhoto(String path){try{if(path!=null&&!path.isEmpty())new File(path).delete();}catch(Exception ignored){}}
    int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override protected void onDestroy(){io.shutdownNow();super.onDestroy();}
}