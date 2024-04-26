package uk.co.weand.androidvideobgremover
import android.content.Context
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.weand.androidvideobgremover.ui.theme.AndroidVideoBgRemoverTheme
import com.arthenica.ffmpegkit.FFmpegKitConfig;
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
      //  FFmpegKitConfig.init(this)
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
            Button(onClick = { remove_background_sync() }) {
                Text("Remove background sync")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { remove_background_async() }) {
                Text("Remove background async")
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between Greeting and Button
            VideoPlayerScreen()

        }
    }
    fun getFileFromRawResource(context: Context, resourceId: Int, fileName: String): String {
        val destinationFile = File(context.filesDir, fileName)

        context.resources.openRawResource(resourceId).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return destinationFile.absolutePath
    }

    fun deleteOutputFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            println("output exists, deleting ${filePath}");

            file.delete()
        } else {
            println("output not existing yet");

            false // File does not exist, so deletion was not necessary
        }
    }
    fun getVideoPathFromRaw(context: Context, resourceId: Int, fileName: String): String {
        val destinationFile = File(context.filesDir, fileName)

        context.resources.openRawResource(resourceId).use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return destinationFile.absolutePath
    }
    fun getCommand() :String{

        //   val videoPath = "android.resource://uk.co.weand.androidvideobgremover/${R.raw.dogvideo3}"
        val videoPath = getVideoPathFromRaw(this, R.raw.dogvideo3, "dogvideo3.webm")
        val outputPath = getFileFromRawResource(this, R.raw.dogvideo3, "output.mp4")

        // Example FFmpeg command to replace green background with black
        val ffmpegCommand = "-y -i "+videoPath+"  -filter_complex " +
                "[0:v]chromakey=0x00FF00:0.1:0.2,format=yuv420p[ckout];" +
                "[ckout]colorkey=color=black:similarity=0.1:blend=0.0[out]" +
                " -map [out] "+outputPath;
        println(ffmpegCommand);
        return ffmpegCommand;
    }

    fun remove_background_sync() {
        val outputPath = getFileFromRawResource(this, R.raw.dogvideo3, "output.mp4")
        deleteOutputFile(outputPath)
        println("------------------------------------------------------------");
        val ffmpegCommand = getCommand();
        println(ffmpegCommand);
        println(" * execute");

        FFmpegKit.execute(ffmpegCommand);
        println(" * done")


        val outputSize=getFileSize(outputPath)
        println(" * done output size=${outputSize}")
    }

    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) {
            file.length()
        } else {
            -1 // File does not exist
        }
    }

        fun remove_background_async(){
            println("------------------------------------------------------------");
            val ffmpegCommand = getCommand();
            println(ffmpegCommand);
            println(" * execute");

            println(" * executeAsync");
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