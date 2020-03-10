package com.hmproductions.facewatch.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.adapter.PersonRecyclerAdapter
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import com.hmproductions.facewatch.data.Person
import com.hmproductions.facewatch.utils.getActualPath
import kotlinx.android.synthetic.main.fragment_admin.*
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

class AdminFragment : Fragment(), PersonRecyclerAdapter.PersonClickListener {

    @Inject
    lateinit var client: FaceWatchClient

    private lateinit var model: FaceWatchViewModel
    private var personRecyclerAdapter: PersonRecyclerAdapter? = null
    private var loadingDialog: AlertDialog? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personRecyclerAdapter = PersonRecyclerAdapter(context, null, this)
        model = activity?.run { ViewModelProvider(this).get(FaceWatchViewModel::class.java) }
            ?: throw Exception("Invalid activity")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        personRecyclerView.adapter = personRecyclerAdapter
        personRecyclerView.layoutManager = LinearLayoutManager(context)
        personRecyclerView.setHasFixedSize(false)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        galleryButton.setOnClickListener { pickImageFromGallery() }
        captureButton.setOnClickListener { dispatchTakePictureIntent() }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putStringArrayListExtra(Intent.EXTRA_MIME_TYPES, arrayListOf("image/jpeg", "image/png"))
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
                    identifyPeopleFromFaces(getActualPath(context!!, fileUri))
            }

            REQUEST_IMAGE_CAPTURE -> {
                identifyPeopleFromFaces(currentPhotoPath)
            }
        }
    }

    private fun identifyPeopleFromFaces(filePath: String?) = lifecycleScope.launch {

        if (filePath == null) return@launch
        val file = File(filePath)

        Log.v(LOG_TAG, "Sending photo from $filePath")

        val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        loadingDialog?.show()
        val personList = withContext(Dispatchers.IO) { model.identifyFace(client, image) }
        loadingDialog?.dismiss()

        personRecyclerAdapter?.swapData(personList)
        noFacesToRecognizeTextView.visibility = if (personList.size > 0) View.GONE else View.VISIBLE
    }

    override fun onPersonClicked(person: Person) {
        context?.toast("Clicked on ${person.rollNumber}")
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
        findNavController().navigate(R.id.logout_from_admin_action)
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 101
        private const val REQUEST_IMAGE_CAPTURE = 117
        private const val LOG_TAG = ":::"
    }
}