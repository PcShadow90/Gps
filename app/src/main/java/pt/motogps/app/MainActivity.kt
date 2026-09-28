package pt.motogps.app
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class MainActivity: ComponentActivity(){
 private val location by lazy{LocationServices.getFusedLocationProviderClient(this)}
 private var web:WebView?=null
 private lateinit var tts:TextToSpeech
 private val permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
 override fun onCreate(b:Bundle?){super.onCreate(b);tts=TextToSpeech(this){tts.language=Locale("pt","PT")};permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION));setContent{App()}}
 @SuppressLint("SetJavaScriptEnabled")
 @Composable fun App(){
  var dest by remember{mutableStateOf("")};var voice by remember{mutableStateOf(true)}
  Column(Modifier.fillMaxSize()){
   TopAppBar(title={Text("MotoGPS")})
   Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    OutlinedTextField(dest,{dest=it},Modifier.weight(1f),singleLine=true,label={Text("Destino")})
    Button(onClick={web?.evaluateJavascript("searchDestination('"+dest.replace("'","\\'")+"');",null)}){Text("Ir")}
   }
   Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceEvenly){
    FilterChip(selected=true,onClick={},label={Text("Moto")})
    FilterChip(selected=false,onClick={web?.evaluateJavascript("setMode('curves');",null)},label={Text("Curvas")})
    FilterChip(selected=false,onClick={web?.evaluateJavascript("setMode('no_tolls');",null)},label={Text("Sem portagens")})
    Switch(checked=voice,onCheckedChange={voice=it})
   }
   AndroidView(factory={c->WebView(c).apply{web=this;settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=WebViewClient();addJavascriptInterface(object{@JavascriptInterface fun speak(s:String){if(voice)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"nav")}},"AndroidVoice");loadUrl("file:///android_asset/map.html")}},Modifier.weight(1f))
  }
  LaunchedEffect(Unit){startLocation()}
 }
 @SuppressLint("MissingPermission") private fun startLocation(){location.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener{l->if(l!=null)web?.evaluateJavascript("setUser("+l.latitude+","+l.longitude+");",null)}}
 override fun onDestroy(){tts.shutdown();super.onDestroy()}
}