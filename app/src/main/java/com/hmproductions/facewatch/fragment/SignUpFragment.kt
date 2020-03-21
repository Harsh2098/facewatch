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
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import javax.inject.Inject

class SignUpFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        (dialogView.findViewById<View>(R.id.progressDialog_textView) as TextView).setText(R.string.signing_up)
        loadingDialog = AlertDialog.Builder(context!!).setView(dialogView).setCancelable(false).create()

        newSignUpButton.setOnClickListener { onNewSignUpButtonClick() }
    }

    private fun onNewSignUpButtonClick() = lifecycleScope.launch {
        if (newEmailEditText.text.toString().isBlank()) {
            newEmailEditText.error = "Enter a valid email"
        } else if (newPasswordEditText.text.toString().isBlank() || newPasswordEditText.text.toString().length < 6) {
            newPasswordEditText.error = "Enter a 6 digit password"
        } else if (newNameEditText.text.toString().isBlank()) {
            newNameEditText.error = "Enter your Name"
        } else if (newRollNumberEditText.text.toString().isBlank()) {
            newRollNumberEditText.error = "Enter your Roll Number"
        } else {
            val isAdmin = userAdmin.isChecked

            loadingDialog?.show()
            val response = withContext(Dispatchers.IO) {
                model.signUp(
                    client, newEmailEditText.text.toString(), newNameEditText.text.toString(),
                    newRollNumberEditText.text.toString(), newPasswordEditText.text.toString(), isAdmin
                )
            }
            loadingDialog?.dismiss()

            if (response.statusCode.isSuccessful()) {
                showSuccessfulSignUpDialog()
            } else {
                context?.toast(response.statusMessage)
            }
        }
    }

    private fun showSuccessfulSignUpDialog() {
        AlertDialog.Builder(context!!)
            .setTitle("Sign Up Successful")
            .setMessage("Your new account has been successfully created")
            .setCancelable(false)
            .setPositiveButton(getString(R.string.okay)) { _, _ ->
                findNavController().navigate(
                    R.id.back_to_signIn_action
                )
            }
            .show()
    }
}