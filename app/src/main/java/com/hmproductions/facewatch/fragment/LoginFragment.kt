package com.hmproductions.facewatch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hmproductions.facewatch.FaceWatchClient
import com.hmproductions.facewatch.R
import com.hmproductions.facewatch.dagger.ContextModule
import com.hmproductions.facewatch.dagger.DaggerFaceWatchApplicationComponent
import com.hmproductions.facewatch.data.FaceWatchViewModel
import com.hmproductions.facewatch.utils.isSuccessful
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var client: FaceWatchClient

    private lateinit var model: FaceWatchViewModel
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = activity?.run { ViewModelProvider(this).get(FaceWatchViewModel::class.java) }
            ?: throw Exception("Invalid activity")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DaggerFaceWatchApplicationComponent.builder().contextModule(ContextModule(context!!)).build().inject(this)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.signing_in)
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        loginButton.setOnClickListener { onLoginButtonClick() }
    }

    private fun onLoginButtonClick() = lifecycleScope.launch {
        if (emailEditText.text.toString().isBlank()) {
            emailEditText.error = "Enter a valid email"
        } else if (passwordEditText.text.toString().isBlank() || passwordEditText.text.toString().length < 6) {
            passwordEditText.error = "Enter a 6 digit password"
        } else {
            loadingDialog?.show()
            val response = withContext(Dispatchers.IO) {
                model.login(client, emailEditText.text.toString(), passwordEditText.text.toString())
            }
            loadingDialog?.dismiss()

            if (response.statusCode.isSuccessful()) {
                model.token = response.token
                model.currentPhotosCount = response.currentPhotosCount
                model.email = emailEditText.text.toString()
                findNavController().navigate(
                    if (response.isAdmin) R.id.admin_login_successful_action else R.id.normal_login_successful_action
                )
            } else {
                context?.toast(response.statusMessage)
            }
        }
    }
}