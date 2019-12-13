

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

# Aggiungere findBalls e findLines alle App

1) Copiare i file `Ball.java`, `BallFinder.java` e `LineFinder.java` all'interno della cartella dei sorgenti del progetto, dove è presente la vostra Activity (`/app/src/main/[nome_package]`)

2) All'interno del file di layout della vostra Activity (`activity_nomeactivity.xml`), dentro i tag che racchiudono il layout principale, aggiungere il seguente codice:

```xml
...
    <org.opencv.android.JavaCameraView
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:visibility="gone"
            android:id="@+id/OpenCvView"
            opencv:show_fps="true"
        opencv:camera_id="any" />
...
```

3) All'interno del codice della vostra Activity aggiungere il seguente codice:

```java
public class MainActivity extends AppCompatActivity {
    private CameraBridgeViewBase mOpenCvCameraView;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
...
        if (!OpenCVLoader.initDebug()) {
            Log.e("AndroidIngSwOpenCV", "Unable to load OpenCV");
        } else {
            Log.d("AndroidIngSwOpenCV", "OpenCV loaded");
        }
...
        mOpenCvCameraView = findViewById(R.id.OpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();
                
                return frame;
            }
        });
        
        mOpenCvCameraView.enableView();
    }
}
```

# Usare findBalls

La classe BallFinder prevede un costruttore, vari setter e una funzione findBalls.

Il costruttore prende come parametri:
- `Mat frame`: il frame ottenuto dalla fotocamera in formato rgba
- `boolean debug`: parametro opzionale, per mostrare all'interno del frame il funzionamento della classe (attenzione: se è uguale a `true` scrive nel frame).
Nella modalità debug, i contorni degli oggetti rilevati sono mostrati da una linea rossa fine, e le mine sono descritte da un cerchio del colore rilevato (nero se non è possibile capire il colore)

I setter più importanti sono:
- `setOrientation(String orientation)`: prende una stringa che può essere **"portrait"** (telefono in verticale), oppure **"landscape"** (telefono in orizzontale). L'orientamento di default è portrait.
Se l'orientamento è settato a portrait la funzione `findBalls()` effettuerà i calcoli asserendo che il telefono è tenuto in verticale, se invece è landscape il comportamento di `findBalls()` rimarrà invariato (questo perché OpenCV è stato ideato solo per funzionare in modalità landscape, e non è possibile girare la visualizzazione della fotocamera).
Se volete evitare di usare la modalità portrait e tenere comunque il telefono in verticale è necessario ruotare la matrice contenente il frame prima di passarlo al costruttore
- `setMinArea(int min_area)`: imposta l'area minima di un oggetto affiché venga rilevato come possibile mina. Se è troppo grande non rileverà le palline più distanti, se è troppo piccolo è possibile che rilevi falsi positivi
- `setViewRatio(float view_ratio)`: imposta l'altezza, partendo dall'alto del frame, sopra il quale ignorare gli oggetti rilevati. Se la modalità di debug è attiva, è visibile sotto forma di una linea azzurra all'interno del frame. Prende un numero decimale compreso tra 0 e 1, dove 0 è la parte più alta del frame, 0.5 è metà e 1 è la parte più bassa del frame

All'interno della classe `BallFinder` sono presenti altri setter che permettono di regolare le soglie dei colori (nel formato HSV) e della saturazione (da 0 a 255) per la rilevazione degli oggetti.

I parametri di default sono calibrati per funzionare nel campo da gioco dichiarato dalle specifiche (edificio Zeta, secondo piano, rombo con le mattonelle rosa) in condizione di luce ottimale, **e non altrove**.

La funzione principale della classe è `findBalls()`, e restituisce un ArrayList di oggetti `Ball`. Un oggetto `Ball` è composto nel seguente modo:

- `double center.x`: coordinate in pixel all'interno del frame della coordinata x del centro della mina trovata
- `double center.y`: coordinate in pixel all'interno del frame della coordinata y del centro della mina trovata
- `float radius`: raggio della mina trovata
- `String color`: colore della mina trovata, può essere "red", "blue", "yellow" o "unknown"

Se la funzione `findBalls()` viene utilizzata al di fuori del campo da gioco verranno rilevati falsi positivi, quindi per verificarne il corretto funzionamento è necessario utilizzarla nel campo da gioco.

Esempio di utilizzo di findBalls:

```java
...
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();

                BallFinder ballFinder = new BallFinder(frame, true);
                ballFinder.setViewRatio(0.33f);
                ballFinder.setMinArea(200);
                ballFinder.setOrientation("landscape");
                ArrayList<Ball> balls = ballFinder.findBalls();

                for (Ball ball : balls) {
                    Log.e("Ball x", String.valueOf(ball.center.x));
                    Log.e("Ball y", String.valueOf(ball.center.y));
                    Log.e("Ball radius", String.valueOf(ball.radius));
                    Log.e("Ball color", ball.color);
                }

                return frame;
            }
...
```

# Usare findLine

La classe LineFinder prevede un costruttore, due setter e la funzione findLine.

Il costruttore prende come parametri:
- `Mat frame`: il frame ottenuto dalla fotocamera in formato rgba
- `boolean debug`: parametro opzionale, per mostrare all'interno del frame il funzionamento della classe (attenzione: se è uguale a `true` scrive nel frame). Nella modalità debug, è possibile vedere l'area in cui viene rilevata la fuga (delimitata da due  linee verdi) e la fuga, che sarà evidenziata in rosso

I setter sono:
- `setOrientation(String orientation)`: in modo analogo allo stesso metodo presente in `BallFinder`, prende una stringa che può essere **"portrait"** (telefono in verticale), oppure **"landscape"** (telefono in orizzontale). L'orientamento di default è portrait.
Se l'orientamento è settato a portrait la funzione `findLines()` effettuerà i calcoli asserendo che il telefono è tenuto in verticale, se invece è landscape il comportamento di `findLines()` rimarrà invariato (questo perché OpenCV è stato ideato solo per funzionare in modalità landscape, e non è possibile girare la visualizzazione della fotocamera)
Se volete evitare di usare la modalità portrait e tenere comunque il telefono in verticale è necessario ruotare la matrice contenente il frame prima di passarlo al costruttore.
- `setThreshold(int center, int size)`: il primo parametro indica dove viene centrata l'area di rilevazione delle fughe all'interno del frame in pixel, mentre il secondo indica quanto larga è l'area, sempre in pixel. Ad esempio `setThreshold(300, 20)` permetterà di rilevare tutte le fughe presenti all'interno del frame che vanno da y = 290 a y = 310

La funzione principale della classe è `findLine()`, e restituisce un double che indica l'inclinazione della retta trovata in gradi sessadecimali (attenzione: può restituire valori differenti in base all'orientamento del telefono), oppure `Double.NaN` se non ne ha trovata nessuna.

Esempio di utilizzo di findLine:

```java
...
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();

                LineFinder lineFinder = new LineFinder(frame, true);
                lineFinder.setThreshold(300, 20);;
                lineFinder.setOrientation("landscape");
                double inclination = lineFinder.findLine()

                if (!Double.isNaN(inclination)) {
                    Log.e("Line inclination", String.valueOf(inclination));
                }

                return frame;
            }
...
```

È possibile utilizzare sia BallFinder che LineFinder sullo stesso frame senza che avvengano conflitti, facendo attenzione che se la modalità debug è attiva e `findLine()` viene chiamata prima di `findBalls()` la rilevazione delle mine potrebbe risultare imprecisa perché `findBalls()` non sta operando sul frame originale.
