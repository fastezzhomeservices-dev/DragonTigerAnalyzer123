package com.biharsurvey.nonit;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
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
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
 static final String APP="BIHAR SURVEY NON IT", LOGIN_ID="ADMIN", LOGIN_PASSWORD="ADMIN";
 LinearLayout root,body; EditText office,section,surveyor; TextView gps,countText; double lat,lon; String activeCategory="Building",suggestedType="";
 static final int MAX_BATCH=500;
 ArrayList<Entry> batch=new ArrayList<>();
 SurveyDbHelper db; ExecutorService io=Executors.newSingleThreadExecutor();
 Uri pendingPhotoUri; String pendingPhotoPath="";
 ActivityResultLauncher<Uri> takePhoto;
 final String[] categories={"Building","Office Land","Office Equipment","Office Furniture","Residential Colony","Vehicle","Plant & Machinery","Store Inventory","Edit/Rejected Entry"};
 final Map<String,String[]> HEADERS=new LinkedHashMap<>();
 static class Entry { String category,photoPath=""; LinkedHashMap<String,String> values=new LinkedHashMap<>(); double lat,lon; long time; }
 takePhoto=registerForActivityResult(new ActivityResultContracts.TakePicture(),ok->{if(ok&&pendingPhotoUri!=null){runVision(pendingPhotoUri);}else{deletePendingPhoto();}});
 final ActivityResultLauncher<String[]> perms=registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),r->updateGps());

 @Override public void onCreate(Bundle b){super.onCreate(b);buildHeaders();db=new SurveyDbHelper(this);if(!loggedIn()){login();return;}home();requestPerms();}
 void buildHeaders(){
  HEADERS.put("Building", new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchylevel","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Building Type","Building Name","Building Type Other","Year Constructed","Owenership","Office Name","Colony Name","No of Quarter","No of Households per Floor","No of Floor","Area Unit","Builtup Area","Condititon","Address","Fire Safety","Power Connection","Lift Available","Occupancy","Scheme Name","Registration Date Available","Registry No","Registration Date","In-Operational Date","Verified Date","Status","Remarks","Name1","Value1","Name2","Value2","Name3","Value3","Name4","Value4","Name5","Value5"});
  HEADERS.put("Office Land", new String[]{"Unique Id","ENTRYDATE","Surveyor","HierarchyLevel","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Sectio Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pin Code","Land Type","Land Ownership","Office Land Type Other","Land Uses","Record Id","Plot No","Area Unit","Area Acres","Survey Number","Land North","Land South","LAND East","Land West","Document Refrence","Condition","Status","Encriachment","Legal Status","Remarks","Scheme Name","Is Registration Date","Registry No","Registration Date","Doc Reference","Address","Name1","Value1","Name2","Value2","Name3","Value3","Name4","Value4","Name5","Value5"});
  HEADERS.put("Office Equipment", new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Store","Building Name","Status","Room Section","Asset Lifecycle Status","Equipment Type","Fan Type","Motor Hub","Cooler Type","Size","Capacity","Working condition","Filter condition","Heater Type","Wattage","Installed Capacity DC","Rated AC Power","Module Technology","Mounting Technology","Number of Modules","Number of Inverters","Inverter Rated Power","Commissioning Date","Grid Connection Type","Effective Roof Area","Installation Type","Solar Condition","Office Name","Condition","Scheme Name","Floor","Manufacturing Date","Model Name","Remarks","Warranty Expiry Date","Serial Number","Warranty","Purchase Order Number","Purchase Order Date","AMC","Manufacturing Name","feature Code","Census Code","AMC Expiry Date","Date of Commissioning","Residual Value","In-operational Date","Building","Address","Storage","Equipment Number","Ram","Graphics Card","Designation","Processor","AC Type","Used by Person Name","AC Capacity","Model No","Printer Size","Ac Type Other","Equipment Type Other","Name1","Value1","Name2","Value2","Name3","Value3","Name4","Value4","Name5","Value5","Starting Method","Temperature Rise","Degree Protection","Battery Quantity","Rated Voltage","Generator Model","Excitation Type","No of Phases","Charger Output","Governor Type","Battery Type","Insulation Class","Neutral Formation","Fuel Type","Frequency HZ","Overload Capacity","Terminal Box","Engine Type","Rated RPM","Battery Voltage","Aspiration Type","Charger Type","Fuel Pump Type","Cooling Type","Generator Type","Power Factor","Generator Capacity KVA","Battery Capacity","Fuel Injection"," Fuel Tank Capacity","Location","Cable Size","LOA Number","Noise Level","Acoustic Material","Lubricanting System","Mounting Type","Loa Date","Cylinder Pressure Bar","Hose Temperature Range","Lancing Distance","Face Mask Type","Fire Rating","SCBA Available","Cylinder Material","Extinguising Medium","Nozzle Type","Cylinder Type","Foam Type","Pressure Gauge","fire Capacity","Totalweight KG"," Working Duration Min","Fire Extinguisher Type","Back Plate Type","Warning Wristle","Maximum Operating Temperature","Minimum Operating Temperature","Pressure Reducer","Laboratory Test"});
  HEADERS.put("Office Furniture", new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Building","Office","Store","Floor","Room Section","Model","Equipment Type","Manufacturer Name","Condition","Assetstatus","Manufacturingdate","Quantity","Material","Eam","Feature Code","Census","Status","Remarks","In-Operational Date","Approved By","Amc Date","Warranty","Purchase Order No","Purshase Order Date","Warranty Date","Amc","Residual Value","Date Commission","Item Type","Bed Size","Bed Material","Bed Conditon","Working Usable","Building Name","Address","Designation","Used by","Schema Name","New Address","Table Type","Seater","Size","Other Item Type","Name1","Value1","Name2","Value2","Name3","Value3","Name4","Value4","Name5","Value5"});
  HEADERS.put("Residential Colony", new String[]{"Unique Id","ENTRYDATE","Surveyor","Hierarchy","Discom/ Organization Name","Zone Name","Circle Name","Division Name","Subdivision Name","Section Name","State","District Name","Block Name","Gram Panchyat Name","Village Name","Pincode","Feature Code","Address","Store Name","Serial Number","Gis Unique Id","Area Acres","Land Type","Residual Value","Condition","Legal Status","Status","Encroachment","Commissioning Date","In-Operational Date","Scheme Name","Registration Date","Purchase Order Date","Remarks","Purchase Number","House Type","Parking","No of House/Flat","Colony Name","Fitness Valid Date","Registry No","Is Registration Date Available","Colony Type","Colony Type Other","House Type Other","Name1","Value1","Name2","Value2","Name3","Value3","Name4","Value4","Name5","Value5"});
 }
 boolean loggedIn(){return getPreferences(0).getBoolean("login",false);}

 void login(){
  root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(24),dp(55),dp(24),dp(24)); root.setBackgroundColor(0xfff3f7fb);
  TextView logo=title(APP); logo.setTextSize(25); root.addView(logo); root.addView(title("Secure Common Login"));
  EditText u=input("Login ID"); EditText p=input("Password"); p.setInputType(129); root.addView(cardWrap(u)); root.addView(cardWrap(p));
  Button b=primary("LOGIN"); root.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));
  b.setOnClickListener(v->{if(LOGIN_ID.equals(u.getText().toString().trim())&&LOGIN_PASSWORD.equals(p.getText().toString())){getPreferences(0).edit().putBoolean("login",true).apply();home();requestPerms();}else Toast.makeText(this,"Invalid common login",Toast.LENGTH_SHORT).show();});
  setContentView(root);
 }

 void home(){
  root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff3f7fb);
  LinearLayout bar=new LinearLayout(this); bar.setPadding(dp(16),dp(14),dp(16),dp(8)); bar.setGravity(Gravity.CENTER_VERTICAL);
  TextView t=title(APP); t.setTextSize(20); bar.addView(t,new LinearLayout.LayoutParams(0,-2,1));
  Button ex=smallButton("EXPORT EXCEL"); bar.addView(ex); ex.setOnClickListener(v->exportExcel());
  root.addView(bar);
  countText=new TextView(this); countText.setText("Saved entries: "+db.count()); countText.setTextColor(0xff35546f); countText.setTextSize(14); countText.setPadding(dp(18),0,dp(18),dp(8)); root.addView(countText);
  ScrollView sv=new ScrollView(this); body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(2),dp(14),dp(30)); sv.addView(body);
  root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
  for(String c:categories){
   Button x=categoryButton(c); body.addView(x,new LinearLayout.LayoutParams(-1,dp(58))); x.setOnClickListener(v->{if(c.equals("Edit/Rejected Entry"))showSaved();else if(HEADERS.containsKey(c))form(c);else Toast.makeText(this,"Fields for this category are not supplied yet.",Toast.LENGTH_LONG).show();});
  }
 }

 void form(String cat){
  activeCategory=cat; batch.clear(); suggestedType=""; body.removeAllViews(); body.addView(title(cat+"  •  Survey"));
  LinearLayout info=sectionCard(); body.addView(info);
  office=input("Office / Location"); section=input("Section"); surveyor=input("Surveyor name");
  info.addView(label("OFFICE / LOCATION")); info.addView(office); info.addView(label("SECTION")); info.addView(section); info.addView(label("SURVEYOR")); info.addView(surveyor);
  gps=new TextView(this); gps.setText("GPS: waiting for location…"); gps.setTextColor(0xff2d5d3d); gps.setPadding(dp(10),dp(10),dp(10),dp(10)); info.addView(gps);
  LinearLayout actions=sectionCard(); body.addView(actions);
  Button cam=primary("TAKE PHOTO + OCR / IDENTIFY"); actions.addView(cam,new LinearLayout.LayoutParams(-1,dp(54))); cam.setOnClickListener(v->startCamera());
  Button add=primary("ADD ITEM TO BATCH"); actions.addView(add,new LinearLayout.LayoutParams(-1,dp(54))); add.setOnClickListener(v->addItem(cat));
  TextView cap=new TextView(this); cap.setText("Batch capacity: up to "+MAX_BATCH+" photos/items. Photos are saved immediately to disk, not kept as large Bitmaps in RAM."); cap.setTextSize(12); cap.setTextColor(0xff536a7d); cap.setPadding(dp(6),dp(8),dp(6),dp(8)); actions.addView(cap);
  TextView bc=new TextView(this); bc.setText("Current batch: 0 / "+MAX_BATCH); bc.setTextSize(16); bc.setTypeface(null,1); bc.setTextColor(0xff17324d); actions.addView(bc); countText=bc;
  Button preview=primary("BATCH OVERVIEW / SAVE"); actions.addView(preview,new LinearLayout.LayoutParams(-1,dp(54))); preview.setOnClickListener(v->preview());
  Button back=smallButton("BACK HOME"); actions.addView(back,new LinearLayout.LayoutParams(-1,dp(48))); back.setOnClickListener(v->home());
 }

 void startCamera(){
  if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPerms();return;}
  try{String id=String.valueOf(System.currentTimeMillis());File dir=new File(getExternalFilesDir(null),"survey_photos");if(!dir.exists())dir.mkdirs();File f=new File(dir,"ITEM_"+id+".jpg");pendingPhotoPath=f.getAbsolutePath();pendingPhotoUri=FileProvider.getUriForFile(this,"com.biharsurvey.nonit.fileprovider",f);takePhoto.launch(pendingPhotoUri);}catch(Exception e){Toast.makeText(this,"Camera start failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }

 void addItem(String cat){
  if(batch.size()>=MAX_BATCH){Toast.makeText(this,"Batch limit reached: "+MAX_BATCH,Toast.LENGTH_LONG).show();return;}
  if(pendingPhotoPath==null||pendingPhotoPath.isEmpty()||!new File(pendingPhotoPath).exists()){Toast.makeText(this,"Photo mandatory. Take photo first.",Toast.LENGTH_SHORT).show();return;}
  if(Math.abs(lat)<0.000001&&Math.abs(lon)<0.000001){Toast.makeText(this,"GPS mandatory. Wait for location.",Toast.LENGTH_SHORT).show();return;}
  Entry e=new Entry();e.category=cat;e.lat=lat;e.lon=lon;e.time=System.currentTimeMillis();e.photoPath=pendingPhotoPath;
  for(String k:HEADERS.get(cat))e.values.put(k,"");
  String sec=section.getText().toString().trim(),loc=office.getText().toString().trim(),sv=surveyor.getText().toString().trim();
  e.values.put("Surveyor",sv);e.values.put("Section Name",sec);e.values.put("Room Section",sec);e.values.put("Address",loc);e.values.put("Building Name",loc);e.values.put("Office",loc);e.values.put("ENTRYDATE",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(e.time)));e.values.put("Item Type",suggestedType);e.values.put("Equipment Type",suggestedType);e.values.put("Unique Id",String.valueOf(e.time));
  batch.add(e);pendingPhotoPath="";pendingPhotoUri=null;suggestedType="";if(countText!=null)countText.setText("Current batch: "+batch.size()+" / "+MAX_BATCH);Toast.makeText(this,"Item added. Total: "+batch.size(),Toast.LENGTH_SHORT).show();
 }

 void runVision(Uri uri){
  try{InputImage img=InputImage.fromFilePath(this,uri);TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(img).addOnSuccessListener(r->{String t=r.getText()==null?"":r.getText().trim();suggestedType=t.length()>80?t.substring(0,80):t;ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS).process(img).addOnSuccessListener(labels->{if(suggestedType.isEmpty()&&!labels.isEmpty())suggestedType=labels.get(0).getText();Toast.makeText(this,"Detected: "+(suggestedType.isEmpty()?"Review manually":suggestedType),Toast.LENGTH_LONG).show();}).addOnFailureListener(e->Toast.makeText(this,"Photo saved. Image identify unavailable.",Toast.LENGTH_SHORT).show());}).addOnFailureListener(e->Toast.makeText(this,"Photo saved. OCR unavailable.",Toast.LENGTH_SHORT).show());}catch(Exception e){Toast.makeText(this,"Photo saved. OCR error.",Toast.LENGTH_SHORT).show();}
 }

 void preview(){
  if(batch.isEmpty()){Toast.makeText(this,"No item added.",Toast.LENGTH_SHORT).show();return;}
  body.removeAllViews();body.addView(title("BATCH OVERVIEW  •  "+batch.size()+" ITEMS"));TextView info=new TextView(this);info.setText("Office: "+office.getText()+"\nSection: "+section.getText()+"\nSurveyor: "+surveyor.getText()+"\n\nCheck each item. Delete an incorrect item before saving.");info.setTextColor(0xff35546f);info.setPadding(dp(6),0,dp(6),dp(10));body.addView(info);
  for(int i=0;i<batch.size();i++)addOverviewCard(i);
  Button save=primary("CONFIRM & SAVE ALL");body.addView(save,new LinearLayout.LayoutParams(-1,dp(56)));save.setOnClickListener(v->saveBatch());Button back=smallButton("BACK TO ENTRY");body.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));back.setOnClickListener(v->form(activeCategory));
 }

 void addOverviewCard(final int index){
  Entry e=batch.get(index);LinearLayout card=sectionCard();TextView tx=label((index+1)+". "+(e.values.get("Item Type").isEmpty()?"Item":e.values.get("Item Type"))+"  •  "+e.values.get("Unique Id"));tx.setTextSize(16);card.addView(tx);
  TextView d=new TextView(this);d.setText("Office: "+office.getText()+"\nSection: "+section.getText()+"\nGPS: "+String.format(Locale.US,"%.6f, %.6f",e.lat,e.lon)+"\nPhoto: saved");d.setTextColor(0xff4d6476);d.setPadding(dp(6),0,dp(6),dp(6));card.addView(d);
  ImageView im=new ImageView(this);im.setScaleType(ImageView.ScaleType.CENTER_CROP);im.setImageBitmap(scaledBitmap(e.photoPath,96,96));card.addView(im,new LinearLayout.LayoutParams(-1,dp(110)));
  Button del=smallButton("DELETE THIS ITEM");card.addView(del,new LinearLayout.LayoutParams(-1,dp(44)));del.setOnClickListener(v->{deletePhoto(e.photoPath);batch.remove(index);preview();});body.addView(card);
 }

 Bitmap scaledBitmap(String path,int w,int h){try{BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(path,o);int s=1;while(o.outWidth/s>w*2||o.outHeight/s>h*2)s*=2;o.inJustDecodeBounds=false;o.inSampleSize=s;return BitmapFactory.decodeFile(path,o);}catch(Exception e){return null;}}

 void saveBatch(){
  if(batch.isEmpty())return;final ArrayList<Entry> copy=new ArrayList<>(batch);final String officeText=office.getText().toString().trim(),sectionText=section.getText().toString().trim(),surveyorText=surveyor.getText().toString().trim();
  io.execute(()->{for(Entry e:copy)db.insert(e.category,e.values.get("Unique Id"),officeText,sectionText,surveyorText,e.values.get("Item Type"),e.photoPath,e.lat,e.lon,e.time,"SAVED");runOnUiThread(()->{batch.clear();pendingPhotoPath="";Toast.makeText(this,"Batch saved successfully: "+copy.size()+" items.",Toast.LENGTH_LONG).show();home();});});
 }

 void showSaved(){
  ArrayList<SurveyDbHelper.EntryRow> rows=db.all();body.removeAllViews();body.addView(title("SAVED / REJECTED ENTRIES"));TextView total=new TextView(this);total.setText("Total records: "+rows.size());total.setTextColor(0xff35546f);total.setPadding(dp(6),0,dp(6),dp(10));body.addView(total);
  for(SurveyDbHelper.EntryRow e:rows){LinearLayout c=sectionCard();TextView t=label(e.category+"  •  "+e.itemType);c.addView(t);TextView d=new TextView(this);d.setText(e.office+" | "+e.section+"\n"+e.uniqueId+"\nGPS: "+String.format(Locale.US,"%.6f, %.6f",e.lat,e.lon));d.setTextColor(0xff4d6476);c.addView(d);Button del=smallButton("DELETE");c.addView(del);del.setOnClickListener(v->{db.delete(e.id);deletePhoto(e.photoPath);showSaved();});body.addView(c);}
  Button back=smallButton("BACK HOME");body.addView(back,new LinearLayout.LayoutParams(-1,dp(48)));back.setOnClickListener(v->home());
 }

 void exportExcel(){
  if(db.count()==0){Toast.makeText(this,"No saved data.",Toast.LENGTH_SHORT).show();return;}final ProgressDialog p=ProgressDialog.show(this,"Exporting","Please wait…",true,false);
  io.execute(()->{try{Workbook wb=new XSSFWorkbook();Map<String,Sheet>m=new HashMap<>();for(String c:HEADERS.keySet()){Sheet ws=wb.createSheet(c);m.put(c,ws);Row h=ws.createRow(0);int i=0;for(String x:HEADERS.get(c))h.createCell(i++).setCellValue(x);for(String x:new String[]{"Latitude","Longitude","Photo Link","Item Identity","Survey Date","Survey Time"})h.createCell(i++).setCellValue(x);}
    for(SurveyDbHelper.EntryRow e:db.all()){Sheet ws=m.get(e.category);if(ws==null)continue;Row r=ws.createRow(ws.getLastRowNum()+1);String[] hs=HEADERS.get(e.category);for(int i=0;i<hs.length;i++)r.createCell(i).setCellValue(valueForHeader(hs[i],e));int n=hs.length;r.createCell(n).setCellValue(e.lat);r.createCell(n+1).setCellValue(e.lon);r.createCell(n+2).setCellValue(e.photoPath);r.createCell(n+3).setCellValue(e.category+" - "+e.itemType+" - "+e.uniqueId);String dt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(e.time));r.createCell(n+4).setCellValue(dt.substring(0,10));r.createCell(n+5).setCellValue(dt.substring(11));}
    File out=new File(getExternalFilesDir(null),"BIHAR_SURVEY_NON_IT.xlsx");FileOutputStream f=new FileOutputStream(out);wb.write(f);f.close();wb.close();runOnUiThread(()->{p.dismiss();Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");i.putExtra(Intent.EXTRA_STREAM,FileProvider.getUriForFile(this,"com.biharsurvey.nonit.fileprovider",out));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share Excel"));});}
   catch(Exception e){runOnUiThread(()->{p.dismiss();Toast.makeText(this,"Excel export failed: "+e.getMessage(),Toast.LENGTH_LONG).show();});}});
 }

 String valueForHeader(String h,SurveyDbHelper.EntryRow e){if("Unique Id".equalsIgnoreCase(h))return e.uniqueId;if("ENTRYDATE".equalsIgnoreCase(h))return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(e.time));if("Surveyor".equalsIgnoreCase(h))return e.surveyor;if("Section Name".equalsIgnoreCase(h)||"Sectio Name".equalsIgnoreCase(h)||"Room Section".equalsIgnoreCase(h))return e.section;if("Address".equalsIgnoreCase(h)||"New Address".equalsIgnoreCase(h)||"Building Name".equalsIgnoreCase(h)||"Office".equalsIgnoreCase(h))return e.office;if("Item Type".equalsIgnoreCase(h)||"Equipment Type".equalsIgnoreCase(h))return e.itemType;return "";}

 void requestPerms(){perms.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.CAMERA});}
 void updateGps(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){if(gps!=null)gps.setText("GPS permission required");return;}LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location l=null;try{l=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(l==null)l=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);}catch(SecurityException ignored){}if(l!=null){lat=l.getLatitude();lon=l.getLongitude();if(gps!=null)gps.setText(String.format(Locale.US,"GPS: %.6f, %.6f  ✓",lat,lon));}else if(gps!=null)gps.setText("GPS: waiting…");}

 void deletePendingPhoto(){if(pendingPhotoPath!=null&&!pendingPhotoPath.isEmpty())deletePhoto(pendingPhotoPath);pendingPhotoPath="";pendingPhotoUri=null;}
 void deletePhoto(String path){try{if(path!=null&&!path.isEmpty())new File(path).delete();}catch(Exception ignored){}}
 int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
 TextView title(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(0xff17324d);t.setTextSize(22);t.setTypeface(null,1);t.setPadding(dp(4),dp(10),dp(4),dp(10));return t;}
 TextView label(String s){TextView t=title(s);t.setTextSize(14);t.setTextColor(0xff1e5275);return t;}
 EditText input(String h){EditText e=new EditText(this);e.setHint(h);e.setTextSize(16);e.setPadding(dp(12),dp(8),dp(12),dp(8));return e;}
 View cardWrap(View v){LinearLayout c=sectionCard();c.addView(v);return c;}
 LinearLayout sectionCard(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(12),dp(10),dp(12),dp(10));GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setCornerRadius(dp(14));g.setStroke(dp(1),0xffd7e4ee);c.setBackground(g);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(6),0,dp(6));c.setLayoutParams(p);return c;}
 Button primary(String s){Button b=btn(s);b.setTextColor(Color.WHITE);b.setBackground(round(0xff1769aa,14));return b;}
 Button categoryButton(String s){Button b=btn(s);b.setTextColor(0xff17324d);b.setGravity(Gravity.CENTER_VERTICAL);b.setAllCaps(false);b.setText("  "+s);b.setBackground(round(s.equals("Edit/Rejected Entry")?0xfffff0e8:0xffffffff,16));return b;}
 Button smallButton(String s){Button b=btn(s);b.setTextSize(12);b.setTextColor(0xff1769aa);b.setBackground(round(0xffffffff,12));return b;}
 Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(13);b.setAllCaps(false);b.setPadding(dp(8),0,dp(8),0);return b;}
 GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));g.setStroke(dp(1),0xffcbdbe7);return g;}
 @Override protected void onDestroy(){io.shutdownNow();super.onDestroy();}

}