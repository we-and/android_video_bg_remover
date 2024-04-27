package uk.co.weand.androidvideobgremover
import android.content.Context
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.File
import java.io.FileOutputStream

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.co.weand.androidvideobgremover.ui.theme.AndroidVideoBgRemoverTheme
import android.net.Uri
import android.widget.MediaController
import androidx.compose.material3.Button
import androidx.compose.ui.viewinterop.AndroidView
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import android.widget.Toast

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    fun BackgroundRemovalStrategySelector() {
        var expanded by remember { mutableStateOf(false) }
        var selectedStrategy by remember { mutableStateOf("Select a strategy") }
        var selectedSyncMde by remember { mutableStateOf("Select a strategy") }
        var selectedVideo by remember { mutableStateOf("Select a strategy") }


        Column {
            ClickableText(
                text = AnnotatedString(selectedStrategy),
                onClick = { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val strategies = listOf("FFmpeg Chroma Key", "Python OpenCV","AI-Based", "Manual Selection")
                strategies.forEach { strategy ->
                    DropdownMenuItem(
                        text = { Text(strategy) },
                        onClick = {
                            selectedStrategy = strategy
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SyncModeSelector() {
        var expanded by remember { mutableStateOf(false) }
        var selectedStrategy by remember { mutableStateOf("Select a mode") }

        Column {
            ClickableText(
                text = AnnotatedString(selectedStrategy),
                onClick = { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val strategies = listOf("Sync", "Async")
                strategies.forEach { strategy ->
                    DropdownMenuItem(
                        text = { Text(strategy) },
                        onClick = {
                            selectedStrategy = strategy
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun VideoSelector(selectedStr:String, onOptionSelected: (String) -> Unit,) {
        var expanded by remember { mutableStateOf(false) }
//        var selectedStrategy by remember { mutableStateOf("Select a video") }

        Column {
            ClickableText(
                text = AnnotatedString(selectedStr),
                onClick = { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val strategies = listOf("Green bg cat", "Grey background dog")
                strategies.forEach { strategy ->
                    DropdownMenuItem(
                        text = { Text(strategy) },
                        onClick = {
                            println("Strategy:"+strategy);
                            onOptionSelected(strategy)
                           // selectedStrategy = strategy
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    @Composable
    fun MainScreen() {
        val coroutineScope = rememberCoroutineScope()
        var showVideoPopup by remember { mutableStateOf(false) } // State to control the visibility of the popup
        var selectedVideo by remember { mutableStateOf("Select a video") }
        val outputPath = getFileFromRawResource(this, getInputResourceByKey(selectedVideo), getOutputFilenameByKey(selectedVideo))
        val context = LocalContext.current
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(context));
        }


        // Using Column to stack the greeting and the video player vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(name = "Android")
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()  // This makes the row fill the maximum width available
                    .padding(horizontal = 16.dp),  // Adds padding on both sides of the Row
                horizontalArrangement = Arrangement.SpaceBetween  // This spaces children evenly along the row

            ) {
                Text(
                    text = "Video",
                   // modifier = modifier
                )
                VideoSelector(selectedVideo, onOptionSelected = { option ->
                    selectedVideo = option
                },)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()  // This makes the row fill the maximum width available
                    .padding(horizontal = 16.dp),  // Adds padding on both sides of the Row
                horizontalArrangement = Arrangement.SpaceBetween  // This spaces children evenly along the row

            ) {
                Text(
                    text = "Removal strategy",
                    // modifier = modifier
                )
                BackgroundRemovalStrategySelector()
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between VideoPlayer and Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()  // This makes the row fill the maximum width available
                    .padding(horizontal = 16.dp),  // Adds padding on both sides of the Row
                horizontalArrangement = Arrangement.SpaceBetween  // This spaces children evenly along the row

            ) {
                Text(
                    text = "Run mode",
                    // modifier = modifier
                )
                SyncModeSelector()
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between VideoPlayer and Button

            // Button to perform an action
            Button(onClick = { runPython(selectedVideo,context) }) {
                Text("Run on \""+selectedVideo+"\"")
            }

            Spacer(modifier = Modifier.height(20.dp)) // Space between Greeting and Button
            Button(onClick = {
                showVideoPopup = true  // Set the state to true when button is clicked
            }) {
                Text("Popup result")
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between Greeting and Button

            VideoPlayerScreen(selectedVideo)


            // Conditionally show the VideoPopup based on the state
            if (showVideoPopup) {
                VideoPopup(outputPath) {
                    showVideoPopup = false  // Pass a lambda to handle closing
                }
            }

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
        println("DELETE ${filePath}");

        return if (file.exists()) {
            println("output exists, deleting");

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
    fun getCommand(selectedVideo: String) :String{

        val videoPath = getVideoPathFromRaw(this, getInputResourceByKey(selectedVideo), getInputFilenameByKey(selectedVideo))
        println("PATH: "+videoPath);
        val outputPath = getFileFromRawResource(this, getInputResourceByKey(selectedVideo), getOutputFilenameByKey(selectedVideo))

//        val outputPath = getFileFromRawResource(this, R.raw.dogvideo3, "output.mp4")
        println("OUTPUT PATH: "+outputPath);
        val ffmpegCommand = "-y -i "+videoPath+"  -filter_complex " +
                "[0:v]chromakey=0x00FF00:0.1:0.2,format=yuv420p[ckout];" +
                "[ckout]colorkey=color=black:similarity=0.1:blend=0.0[out]" +
                " -map [out] "+outputPath;
        return ffmpegCommand;
    }
    @Composable
    fun VideoPopup(videoPath: String,onClose: () -> Unit) {
        var showDialog by remember { mutableStateOf(true) }  // State to show or hide the dialog

        if (showDialog) {
            AlertDialog(
                onDismissRequest = onClose,  // Use the onClose lambda to close the dialog

//                onDismissRequest = { showDialog = false },  // Handle dismiss
                properties = DialogProperties(usePlatformDefaultWidth = false),
                title = { Text("Video Playback") },
                text = {
                    // VideoView setup in Compose
                    AndroidView(factory = { context ->
                        VideoView(context).apply {
                            setVideoURI(Uri.parse(videoPath))
                            val mediaController = MediaController(context)
                            setMediaController(mediaController)
                            mediaController.setAnchorView(this)
                            start()  // Auto-play the video
                        }
                    })
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
    @Composable
    private fun ShowVideoPopupComp(videoPath: String) {
        // Create a Dialog state
        val showDialog = remember { mutableStateOf(true) }

        if (showDialog.value) {
            // Create a Dialog using Jetpack Compose
            Dialog(
                onDismissRequest = {
                    showDialog.value = false
//                    onDismiss()
                }
            ) {
                // Create a Box to hold the VideoView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f) // Adjust the aspect ratio as needed
                ) {
                    // Create a VideoView using AndroidView
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoPath(videoPath)
                                setMediaController(MediaController(context))
                                start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }


    fun runPython(selectedVideo: String,localContext: Context){
        val videoPath = getVideoPathFromRaw(this, getInputResourceByKey(selectedVideo), getInputFilenameByKey(selectedVideo))
        println("PATH: "+videoPath);
        val outputPath = getFileFromRawResource(this, getInputResourceByKey(selectedVideo), getOutputFilenameByKey(selectedVideo))


        val python = Python.getInstance()
        val pythonModule = python.getModule("background_removal")
        println(" * execute");
        // Call the function from Python script
        val result = pythonModule.callAttr("remove_background", "/path/to/input.jpg", "/path/to/output.jpg")
        println(" * done")
        Toast.makeText(localContext, "Done", Toast.LENGTH_SHORT).show()
    }

    fun runFFMpeg_sync(selectedVideo: String, localContext: Context) {
        println("------------------------------------------------------------");

        val outputPath = getFileFromRawResource(this, getInputResourceByKey(selectedVideo), getOutputFilenameByKey(selectedVideo))

        deleteOutputFile(outputPath)
        println("SELECTED VIDEO: "+selectedVideo)

        val ffmpegCommand = getCommand(selectedVideo );
        println("COMMAND"+ffmpegCommand);
        println(" * execute");

        FFmpegKit.execute(ffmpegCommand);
        println(" * done")


        val outputSize=getFileSize(outputPath)
        println(" * done output size=${outputSize}")
        Toast.makeText(localContext, "Done", Toast.LENGTH_SHORT).show()
    }

    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) {
            file.length()
        } else {
            -1 // File does not exist
        }
    }

        fun remove_background_async(selectedVideo:String){
            println("------------------------------------------------------------");
            val ffmpegCommand = getCommand(selectedVideo );
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

fun getVideoPathByStr(name:String):String{

        return    "android.resource://uk.co.weand.androidvideobgremover/${getInputResourceByKey(name)}";
}

fun getInputResourceByKey
            (name:String):Int{
    if(name== "Grey background dog"){
        return  R.raw.dogvideo3;

    }else if(name=="Green bg cat"){
        return  R.raw.greenbgcat;
    }else {
        return    R.raw.dogvideo3;

    }
}
fun getInputFilenameByKey(name:String):String{
    if(name== "Grey background dog"){
        return "dogvideo3.webm";

    }else if(name=="Green bg cat"){
        return  "greenbgcat.webm";
    }else {
        return   "dogvideo3.webm";

    }
}
fun getOutputFilenameByKey(name:String):String{
    if(name== "Grey background dog"){
        return "output-dogvideo3.mpeg";

    }else if(name=="Green bg cat"){
        return  "output-greenbgcat.mpeg";
    }else {
        return   "output-dogvideo3.mpeg";

    }
}


fun getStrategyByStr(name:String):String{
return name;
}
@Composable
fun VideoPlayerScreen(selectedVideo:String) {
println("VideoPlayerScreen selectedVideo="+selectedVideo)
    AndroidView(
        factory = { ctx ->
            val videoView = VideoView(ctx)
            println("LOAD");

            // read from resources
            val videoPath = getVideoPathByStr(selectedVideo)//"android.resource://uk.co.weand.androidvideobgremover/${R.raw.dogvideo3}"
            println(videoPath)
            videoView.setVideoURI(Uri.parse(videoPath))

            val mediaController = MediaController(ctx)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)
            videoView.start()
            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
            }
            videoView
        },
        modifier = Modifier.fillMaxSize()
         ,
        update = { videoView ->
            println("Updating video URI for videoView with selectedVideo=$selectedVideo")

            // Generate the path based on the new video selection
            val videoPath = getVideoPathByStr(selectedVideo)
            videoView.setVideoURI(Uri.parse(videoPath))
            videoView.start()

            // Set MediaController every time the video updates
            val mediaController = MediaController(videoView.context)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
            }
        }
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