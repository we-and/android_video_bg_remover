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
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import android.widget.Toast

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

//import androidx.appcompat.app.AppCompatActivity
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidVideoBgRemoverTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()

                }
            }
        }


    }
    @Composable
    fun MainScreen() {
        // Using Column to stack the greeting and the video player vertically
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(name = "Android")
            Spacer(modifier = Modifier.height(20.dp)) // Space between VideoPlayer and Button
            // Button to perform an action
            Button(onClick = { remove_background() }) {
                Text("Remove background")
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between Greeting and Button
            VideoPlayerScreen()

        }
    }

    fun remove_background(){
        println("------------------------------------------------------------");

        // Example FFmpeg command to replace green background with black
        val ffmpegCommand = "-i /path/to/input.mp4 -filter_complex " +
                "[0:v]chromakey=0x00FF00:0.1:0.2,format=yuv420p[ckout];" +
                "[ckout]colorkey=color=black:similarity=0.1:blend=0.0[out]" +
                " -map [out] /path/to/output.mp4"
println(ffmpegCommand);
        // Execute the command
        FFmpegKit.executeAsync(ffmpegCommand, { session ->
            println("session");

            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                // Command execution completed successfully.
                runOnUiThread {
                    Toast.makeText(this, "Video processing completed successfully.", Toast.LENGTH_LONG).show()
                }
            } else if (ReturnCode.isCancel(returnCode)) {
                // Command execution cancelled by user.
                runOnUiThread {
                    Toast.makeText(this, "Video processing was cancelled.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Command execution failed.
                runOnUiThread {
                    Toast.makeText(this, "Failed to process video.", Toast.LENGTH_LONG).show()
                }
            }
        }, null)
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
        text = "BG Background remover",
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