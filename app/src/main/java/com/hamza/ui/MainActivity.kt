package com.hamza.ui

import android.R.attr.button
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import com.hamza.kidneycancerdetector.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import java.io.IOException
import java.util.Locale


class MainActivity : AppCompatActivity() {
    lateinit var labeler: FirebaseVisionImageLabeler
    lateinit var optionBuilder: FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder
    lateinit var conditions: FirebaseModelDownloadConditions
    lateinit var image: FirebaseVisionImage
    lateinit var localModel: FirebaseAutoMLLocalModel
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding?.root)
       binding.btnSelectPhoo.setOnClickListener {
           CropImage.activity().start(this@MainActivity)

       }
    }

    fun setLabelFromLocalModel(uri: Uri) {
        localModel = FirebaseAutoMLLocalModel.Builder()
            .setAssetFilePath("model/manifest.json")
            .build()
        try {
            val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.0f)
                .build()
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)
            image = FirebaseVisionImage.fromFilePath(this@MainActivity, uri)
            processImageLabeler(labeler, image)
        } catch (e: FirebaseMLException) {
            // ...
        } catch (e: IOException) {
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    val uri: Uri = result.getUri() //path of image in phone
                    binding.imgView.setImageURI(uri) //set image in imageview
                    binding.txtResult.text = "" //so that previous text don't get append with new one
                    setLabelFromLocalModel(uri)
                    // setLabelerFromRemoteLabel(uri);

                   binding.lin.visibility = View.VISIBLE
                }
            }
        }
    }

    //Process image
    private fun processImageLabeler(
        labeler: FirebaseVisionImageLabeler,
        image: FirebaseVisionImage
    ) {
        labeler.processImage(image).addOnCompleteListener { task ->

            for (label in task.result) {
                val eachlabel = label.text.uppercase(Locale.getDefault())
                val confidence = label.confidence
                binding.txtResult.append(
                    "$eachlabel - " + ("" + confidence * 100).subSequence(
                        0,
                        4
                    ) + "%" + "\n\n"
                )
            }
            //
        }.addOnFailureListener { e ->
            Log.e("OnFail", "" + e)
            Toast.makeText(this@MainActivity, "Something went wrong! $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}