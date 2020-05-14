package com.hmproductions.facewatch.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.TrainModelWorker
import com.hmproductions.facewatch.adapter.PersonRecyclerAdapter
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import com.hmproductions.facewatch.data.Person
import com.hmproductions.facewatch.data.Student
import com.hmproductions.facewatch.utils.Constants.TOKEN_KEY
import com.hmproductions.facewatch.utils.getActualPath
import com.hmproductions.facewatch.utils.getDateInISOFormat
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdminFragment : Fragment(), PersonRecyclerAdapter.PersonClickListener {

    @Inject
    lateinit var client: FaceWatchClient

    @Inject
    lateinit var preferences: SharedPreferences

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
        trainButton.setOnClickListener { sendTrainModelRequest() }
        saveFab.setOnClickListener { onSaveButtonClick() }
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

    private fun sendTrainModelRequest() {

        val inputData = workDataOf(TOKEN_KEY to model.token)

        val trainModelRequest = OneTimeWorkRequestBuilder<TrainModelWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context!!).enqueue(trainModelRequest)
        context?.toast("Model train request sent")
    }

    private fun onSaveButtonClick() = lifecycleScope.launch {
        val personList = personRecyclerAdapter?.personList ?: mutableListOf()
        val studentList = mutableListOf<Student>()

        val customView = layoutInflater.inflate(R.layout.dialog_save, null)

        val courseCodeEditText = customView.findViewById<EditText>(R.id.courseCodeEditText)

        val saveDialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setTitle("Set course code")
            .setPositiveButton("Save") { _, _ ->
                if (courseCodeEditText.text.toString().isBlank()) {
                    requireContext().toast("Course code missing")
                } else {
                    for (person in personList) {
                        studentList.add(
                            Student(person.rollNumber, getDateInISOFormat(), courseCodeEditText.text.toString())
                        )
                    }
                    sendSaveRequest(studentList)
                }
            }
            .setNegativeButton("Cancel") { dI, _ -> dI.dismiss() }
            .setCancelable(true)
            .create()

        saveDialog.show()
    }

    private fun sendSaveRequest(studentList: MutableList<Student>) = lifecycleScope.launch {
        val response = withContext(Dispatchers.IO) {
            model.saveAttendance(client, studentList)
        }
        context?.toast(response.statusMessage)
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
        if (performRecognitionSwitch.isChecked) {
            (loadingDialog?.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(
                R.string.identifying_faces
            )
            val personList = withContext(Dispatchers.IO) { model.identifyFace(client, image) }

            if (personList.isEmpty()) {
                saveFab.hide()
                noFacesToRecognizeTextView.visibility = View.VISIBLE
            } else {
                personRecyclerAdapter?.swapData(personList)
                saveFab.show()
                noFacesToRecognizeTextView.visibility = View.GONE
            }
        } else {
            (loadingDialog?.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(
                R.string.uploading_image
            )
            val response = withContext(Dispatchers.IO) { model.uploadImage(client, image) }
            context?.toast(response.statusMessage)
        }
        loadingDialog?.dismiss()
    }

    override fun onPersonClicked(person: Person, position: Int) {
        val customView = layoutInflater.inflate(R.layout.dialog_edit_student, null)

        val nameEditText = customView.findViewById<EditText>(R.id.nameEditText)
        nameEditText.setText(person.name)

        val rollNoEditText = customView.findViewById<EditText>(R.id.rollNoEditText)
        rollNoEditText.setText(person.rollNumber)

        val studentEditorDialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setTitle("Edit student details")
            .setPositiveButton("Save") { _, _ ->
                if (nameEditText.text.toString().isBlank() || rollNoEditText.text.toString().isBlank()) {
                    requireContext().toast("Please enter name and roll no")
                    onPersonClicked(person, position)
                } else {
                    personRecyclerAdapter?.updateStudentDetails(
                        nameEditText.text.toString(), rollNoEditText.text.toString(), position
                    )
                    requireContext().toast("Student details changed")
                }
            }
            .setNegativeButton("Cancel") { dI, _ -> dI.dismiss() }
            .setCancelable(true)
            .create()

        studentEditorDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout_action) {
            logout()
        } else if(item.itemId == R.id.history_action) {
            findNavController().navigate(R.id.attendance_action)
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