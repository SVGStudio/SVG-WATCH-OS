package com.svgstudios.svgwatch;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView clock, status, weather;
    Handler handler = new Handler(Looper.getMainLooper());

    int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    TextView tv(String s, float size) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }
    Button btn(String label) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(13); return b;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        showHome();
        requestLocation();
        updateClock();
    }

    void base(String title) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8), dp(8), dp(8), dp(8)); root.setBackgroundColor(Color.rgb(15,15,26));
        setContentView(root);
        TextView head = tv(title, 19); head.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(head, new LinearLayout.LayoutParams(-1, dp(45)));
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    void showHome() {
        base("SVG WATCH OS");
        clock = tv("--:--", 40); content.addView(clock, new LinearLayout.LayoutParams(-1, dp(75)));
        TextView date = tv(new SimpleDateFormat("EEEE d MMMM", Locale.FRANCE).format(new Date()), 14);
        content.addView(date);
        status = tv(connectionText(), 12); content.addView(status);
        weather = tv("🌤  Météo : chargement…", 15); content.addView(weather, new LinearLayout.LayoutParams(-1, dp(55)));

        LinearLayout row1 = new LinearLayout(this); row1.setGravity(Gravity.CENTER);
        add(row1, btn("🌤 Météo"), v -> showWeather());
        add(row1, btn("🤖 SVG AI"), v -> showAI());
        add(row1, btn("⚙ Paramètres"), v -> showSettings());
        content.addView(row1);

        LinearLayout row2 = new LinearLayout(this); row2.setGravity(Gravity.CENTER);
        add(row2, btn("📱 Apps"), v -> showApps());
        add(row2, btn("🛠 Diagnostic"), v -> showDiagnostic());
        add(row2, btn("🔄 Actualiser"), v -> { status.setText(connectionText()); fetchWeather(); });
        content.addView(row2);

        fetchWeather();
    }

    void add(LinearLayout p, Button b, View.OnClickListener l) {
        b.setOnClickListener(l); p.addView(b, new LinearLayout.LayoutParams(0, dp(58), 1));
    }

    String connectionText() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() ? "● Internet connecté" : "○ Hors connexion";
    }

    void updateClock() {
        if (clock != null) clock.setText(new SimpleDateFormat("HH:mm", Locale.FRANCE).format(new Date()));
        handler.postDelayed(this::updateClock, 1000);
    }

    void fetchWeather() {
        if (!connectionText().contains("connecté")) { weather.setText("☁ Météo indisponible hors connexion"); return; }
        new Thread(() -> {
            try {
                // Paris fallback: works without exposing precise location.
                URL u = new URL("https://api.open-meteo.com/v1/forecast?latitude=48.8566&longitude=2.3522&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=Europe%2FParis");
                HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setConnectTimeout(5000); c.setReadTimeout(5000);
                BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder s=new StringBuilder(); String line; while((line=r.readLine())!=null)s.append(line); r.close();
                String j=s.toString();
                String temp=val(j,"temperature_2m"); String hum=val(j,"relative_humidity_2m"); String wind=val(j,"wind_speed_10m");
                runOnUiThread(() -> weather.setText("🌤 Paris  " + temp + "°C   💧" + hum + "%   💨" + wind + " km/h"));
            } catch(Exception e) { runOnUiThread(() -> weather.setText("🌤 Météo : erreur réseau")); }
        }).start();
    }

    String val(String j, String key) {
        int p=j.indexOf("\""+key+"\":"); if(p<0)return "?";
        p += key.length()+3; int e=p; while(e<j.length() && "0123456789.-".indexOf(j.charAt(e))>=0)e++;
        return j.substring(p,e);
    }

    void showWeather() {
        base("🌤 MÉTÉO");
        TextView w=tv("Chargement…",20); content.addView(w);
        Button back=btn("← Accueil"); content.addView(back); back.setOnClickListener(v->showHome());
        fetchWeather();
        new Thread(() -> {
            try {
                URL u=new URL("https://api.open-meteo.com/v1/forecast?latitude=48.8566&longitude=2.3522&current=temperature_2m,relative_humidity_2m,wind_speed_10m,pressure_msl&hourly=temperature_2m,precipitation_probability&forecast_days=2&timezone=Europe%2FParis");
                HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setConnectTimeout(5000); c.setReadTimeout(5000);
                BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream())); StringBuilder s=new StringBuilder(); String l; while((l=r.readLine())!=null)s.append(l); r.close();
                String j=s.toString();
                runOnUiThread(()->w.setText("Paris\n\n🌡 "+val(j,"temperature_2m")+" °C\n💧 "+val(j,"relative_humidity_2m")+" %\n💨 "+val(j,"wind_speed_10m")+" km/h\n🌬 Pression : "+val(j,"pressure_msl")+" hPa"));
            }catch(Exception e){runOnUiThread(()->w.setText("Impossible de récupérer la météo."));}
        }).start();
    }

    void showAI() {
        base("🤖 SVG AI");
        EditText q=new EditText(this); q.setHint("Écris une question…"); q.setTextColor(Color.WHITE); q.setHintTextColor(Color.GRAY);
        content.addView(q);
        TextView answer=tv("IA locale : prête.\n\nCommandes rapides :\n• heure\n• météo\n• batterie\n• ouvrir paramètres",14);
        content.addView(answer, new LinearLayout.LayoutParams(-1,0,1));
        Button ask=btn("Demander"); content.addView(ask);
        Button back=btn("← Accueil"); content.addView(back);
        ask.setOnClickListener(v -> {
            String x=q.getText().toString().toLowerCase(Locale.ROOT);
            if(x.contains("heure")) answer.setText("Il est "+new SimpleDateFormat("HH:mm",Locale.FRANCE).format(new Date())+".");
            else if(x.contains("météo")||x.contains("temps")) answer.setText("Je peux afficher la météo avec le module Météo.");
            else if(x.contains("batterie")) answer.setText("Batterie : "+battery()+"%.");
            else answer.setText("Je suis l'assistant local de SVG Watch OS. Une API IA distante pourra être ajoutée dans une prochaine version.");
        });
        back.setOnClickListener(v->showHome());
    }

    int battery() {
        Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return i==null? -1 : (int)(100f*i.getIntExtra("level",0)/i.getIntExtra("scale",100));
    }

    void showSettings() {
        base("⚙ PARAMÈTRES");
        addSetting("🌙 Thème", "Clair / sombre");
        addSetting("🔊 Son", "Réglages Android");
        addSetting("📶 Wi-Fi", "Ouvrir les paramètres Wi-Fi", v->{startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));});
        addSetting("🔵 Bluetooth", "Ouvrir Bluetooth", v->{startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));});
        addSetting("📱 Android", "Informations système", v->{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));});
        Button back=btn("← Accueil"); content.addView(back); back.setOnClickListener(v->showHome());
    }

    void addSetting(String a,String b){addSetting(a,b,null);}
    void addSetting(String a,String b,View.OnClickListener l){
        Button x=btn(a+"\n"+b); if(l!=null)x.setOnClickListener(l); content.addView(x,new LinearLayout.LayoutParams(-1,dp(60)));
    }

    void showApps() {
        base("📱 APPLICATIONS");
        PackageManager pm=getPackageManager();
        Intent main=new Intent(Intent.ACTION_MAIN,null); main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> apps=pm.queryIntentActivities(main,0);
        Collections.sort(apps,(x,y)->x.loadLabel(pm).toString().compareToIgnoreCase(y.loadLabel(pm).toString()));
        for(android.content.pm.ResolveInfo r:apps) {
            Button b=btn("▶ "+r.loadLabel(pm));
            b.setOnClickListener(v->{try{startActivity(new Intent().setClassName(r.activityInfo.packageName,r.activityInfo.name));}catch(Exception ignored){}});
            content.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));
        }
        Button back=btn("← Accueil"); content.addView(back); back.setOnClickListener(v->showHome());
    }

    void showDiagnostic() {
        base("🛠 DIAGNOSTIC");
        TextView d=tv("SVG WATCH OS\n\nAndroid API : "+Build.VERSION.SDK_INT+
            "\nModèle : "+Build.MODEL+
            "\nBatterie : "+battery()+"%"+
            "\nInternet : "+connectionText()+
            "\nÉcran : "+getResources().getDisplayMetrics().widthPixels+" × "+getResources().getDisplayMetrics().heightPixels+
            "\n\nGPS : permission "+(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED?"OK":"à demander")+
            "\n\nCette page sert à vérifier les capacités réelles de la montre.",14);
        content.addView(d,new LinearLayout.LayoutParams(-1,0,1));
        Button back=btn("← Accueil"); content.addView(back); back.setOnClickListener(v->showHome());
    }

    void requestLocation() {
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},42);
    }
}
