

# Aggiunta di OpenCV alle App

1) Andare sul menu Module Settings, premendo tasto destro sull'app

![image-20191020001300062](docs_01.png)



2) Andare sulla tab Dependencies e aggiungere una Library Dependency premendo il tasto +

![image-20191020001349018](docs_02.png)



3) Inserire `com.quickbirdstudios:opencv:4.1.0` sulla casella di ricerca, premere Search, selezionare il primo risultato

![image-20191020001536949](docs_03.png)



4) Premere Ok e Ok sulle finestre aperte

5) Aggiungere la logica di caricamento della libreria sull'evento onCreate della vostra Activity

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.e("AndroidIngSwOpenCV", "Unable to load OpenCV");
        } else {
            Log.d("AndroidIngSwOpenCV", "OpenCV loaded");
        }
    }
}
```

6) Inserire i permessi per accedere alla fotocamera nell'Android Manifest (manifests/AndroidManifest.xml)

```xml
...

    <uses-permission android:name="android.permission.CAMERA"/>

    <uses-feature android:name="android.hardware.camera" android:required="false"/>
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false"/>
    <uses-feature android:name="android.hardware.camera.front" android:required="false"/>
    <uses-feature android:name="android.hardware.camera.front.autofocus" android:required="false"/>

</manifest>
```



Ora potrete usare la libreria OpenCV, come mostrato nell'esempio.