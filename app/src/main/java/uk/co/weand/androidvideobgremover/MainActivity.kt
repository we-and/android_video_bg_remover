package uk.co.weand.androidvideobgremover

import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.weand.androidvideobgremover.ui.theme.AndroidVideoBgRemoverTheme

import android.net.Uri
import android.widget.MediaController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidVideoBgRemoverTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                   VideoPlayerScreen()
                }
            }
        }


    }
}

@Composable
fun VideoPlayerScreen() {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            val videoView = VideoView(ctx)
            println("LOAD");
            //if read from directory
            //  val videoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            //val videoFile = File(videoDirectory, "raw/dogvideo.mpeg")
            // videoView.setVideoURI(Uri.fromFile(videoFile))

            //if read from assets
            //            val assetFileDescriptor = context.assets.openFd("raw/dogvideo.mpeg")
            //          videoView.setVideoURI(Uri.parse("file:///android_asset/dogvideo.mpeg"))

            // read from resources
            val videoPath = "android.resource://uk.co.weand.androidvideobgremover/${R.raw.dogvideo3}"
            videoView.setVideoURI(Uri.parse(videoPath))

            val mediaController = MediaController(ctx)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.start()
            videoView
        },
        modifier = Modifier.fillMaxSize()
    )
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "BG Background remover $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidVideoBgRemoverTheme {
        Greeting("Android")
    }
}