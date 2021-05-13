package com.evanvonoehsen.balderdash

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.evanvonoehsen.balderdash.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.create_username_dialog.*
import kotlinx.android.synthetic.main.create_username_dialog.view.*


class CreateUsernameDialog : DialogFragment() {

    interface CreateUsernameHandler {
        fun usernameCreated(username: String)
    }

    lateinit var gameHandler: CreateUsernameHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CreateUsernameHandler) {
            gameHandler = context
        } else {
            throw RuntimeException(
                    getString(R.string.interface_error)
            )
        }
    }

    lateinit var etUsername : EditText
    lateinit var pbUsername : ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("")

        val dialogView = requireActivity().layoutInflater.inflate(
                R.layout.create_username_dialog, null
        )
        etUsername = dialogView.etUsername
        dialogBuilder.setView(dialogView)

        pbUsername = dialogView.pbUsername
        pbUsername.isVisible = false

        dialogBuilder.setPositiveButton(getString(R.string.ok)) {
            dialog, which ->

        }

        return dialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()

        val dialog = dialog as AlertDialog
        val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            if (etUsername.text.isNotEmpty() && !etUsername.text.contains(" ")) {
                checkUsernameAndContinue(etUsername.text.toString())
            } else {
                etUsername.error = "Please enter a valid username without spaces"
            }
        }
    }


    fun checkUsernameAndContinue(username: String) {
        etUsername.isVisible = true
        FirebaseFirestore.getInstance().collection(FirebaseData.USER_DATA).document(FirebaseAuth.getInstance().uid!!).set(UserData(mutableListOf<String>(), username))
        gameHandler.usernameCreated(username)
        dismiss()
    }
}