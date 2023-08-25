package com.example.ocr
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var selectImageButton: Button
    private lateinit var processButton: Button
    private lateinit var cameraButton: Button

    private val IMAGE_PICK_CODE = 100
    private val PERMISSION_CODE = 101
    private val CAMERA_REQUEST_CODE = 200
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        selectImageButton = findViewById(R.id.selectImageButton)
        processButton = findViewById(R.id.processButton)

        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        processButton.setOnClickListener {
            processImage()
        }

    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            imageView.setImageURI(selectedImageUri)
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            selectedImageUri = getImageUri(imageBitmap)
            imageView.setImageBitmap(imageBitmap)
        }
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val tempDir = File(cacheDir, "tempImages")
        tempDir.mkdirs()
        val tempFile = File(tempDir, "tempImage.png")
        val fos = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        return FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            tempFile
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun processImage() {
        selectedImageUri?.let { uri ->
            val image = InputImage.fromFilePath(this, uri)

            val recognizer = TextRecognition.getClient()

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text
                    textView.text = text
                }
                .addOnFailureListener { e ->
                    textView.text = "Error: ${e.message}"
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            }
        }
    }


}
