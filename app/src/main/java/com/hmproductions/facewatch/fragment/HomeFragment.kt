package com.hmproductions.facewatch.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import com.hmproductions.facewatch.utils.getActualPath
import com.hmproductions.facewatch.utils.isSuccessful
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var client: FaceWatchClient

    private lateinit var model: FaceWatchViewModel
    private var loadingDialog: AlertDialog? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = activity?.run { ViewModelProvider(this).get(FaceWatchViewModel::class.java) }
            ?: throw Exception("Invalid activity")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.uploading_image)
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        galleryButton.setOnClickListener { pickImageFromGallery() }
        captureButton.setOnClickListener { dispatchTakePictureIntent() }
        emailTextView.text = model.email
        photosCountTextView.text = model.currentPhotosCount.toString()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/* video/*"
        intent.putStringArrayListExtra(Intent.EXTRA_MIME_TYPES, arrayListOf("image/jpeg", "image/png", "video/mp4"))
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(context!!, "com.hmproductions.facewatch", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                val fileUri = data?.data
                if (fileUri != null)
                    uploadImageUsingFilePath(getActualPath(context!!, fileUri))
            }

            REQUEST_IMAGE_CAPTURE -> {
                uploadImageUsingFilePath(currentPhotoPath)
            }
        }
    }

    private fun uploadImageUsingFilePath(filePath: String?) = lifecycleScope.launch {

        if (filePath == null) return@launch
        val file = File(filePath)

        Log.v(LOG_TAG, "Sending photo from $filePath")

        val requestFile = RequestBody.create(MediaType.parse("image/* video/*"), file)
       // val image = MultipartBody.Part.createFormData("photo", file.name, requestFile)
        val image = MultipartBody.Part.createFormData("video", file.name, requestFile)


        loadingDialog?.show()
        val videoResponse = withContext(Dispatchers.IO) { model.uploadVideo(client, image) }
        loadingDialog?.dismiss()

        if (videoResponse.statusCode.isSuccessful()) {
            val numberOfPicsAdded = videoResponse.numberOfSavedImages
            val message = "Number of Images Saved : $numberOfPicsAdded"
            context?.toast(message)

            model.currentPhotosCount = model.currentPhotosCount+numberOfPicsAdded
            photosCountTextView.text = model.currentPhotosCount.toString()
        } else {
            context?.toast(videoResponse.statusMessage)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout_action) {
            logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        model.token = null
        model.currentPhotosCount = 0
        model.email = null
        findNavController().navigate(R.id.logout_from_normal_action)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
        private const val REQUEST_IMAGE_CAPTURE = 117
        private const val LOG_TAG = ":::"
    }
}