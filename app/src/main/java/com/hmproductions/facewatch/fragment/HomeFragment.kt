package com.hmproductions.facewatch.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.adapter.PersonRecyclerAdapter
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import com.hmproductions.facewatch.data.Person
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


class HomeFragment : Fragment(), PersonRecyclerAdapter.PersonClickListener {

    private val GALLERY_REQUEST_CODE = 101

    @Inject
    lateinit var client: FaceWatchClient

    private lateinit var model: FaceWatchViewModel
    private var personRecyclerAdapter: PersonRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personRecyclerAdapter = PersonRecyclerAdapter(context, null, this)
        model = activity?.run { ViewModelProvider(this).get(FaceWatchViewModel::class.java) }
            ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        personRecyclerView.adapter = personRecyclerAdapter
        personRecyclerView.layoutManager = LinearLayoutManager(context)
        personRecyclerView.setHasFixedSize(false)

        captureButton.setOnClickListener { pickImageFromGallery() }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putStringArrayListExtra(Intent.EXTRA_MIME_TYPES, arrayListOf("image/jpeg", "image/png"))
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                val fileUri = data?.data
                if (fileUri != null)
                    identifyPeopleFromFaces(fileUri)
            }
        }
    }

    private fun identifyPeopleFromFaces(uri: Uri) = lifecycleScope.launch {

        val actualPath = getActualPath(context!!, uri) ?: return@launch
        val file = File(actualPath)

        val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val personList = withContext(Dispatchers.IO) { model.identifyFace(client, image) }

        personRecyclerAdapter?.swapData(personList)
    }

    override fun onPersonClicked(person: Person?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getActualPath(context: Context, uri: Uri): String? {
        var result: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(projection[0])
                result = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }
}