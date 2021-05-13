package com.evanvonoehsen.balderdash

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.join_game_dialog.view.*
import java.lang.RuntimeException

class JoinGameDialog : DialogFragment() {

    interface GameHandler {
        fun gameJoined(gameID: String, gameCode: String)
    }

    lateinit var gameHandler: GameHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameHandler) {
            gameHandler = context
        } else {
            throw RuntimeException(
                getString(R.string.interface_error)
            )
        }
    }

    lateinit var etGameCode : EditText
    lateinit var pbGame : ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("")

        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.join_game_dialog, null
        )
        etGameCode = dialogView.etGamecode
        dialogBuilder.setView(dialogView)

        pbGame = dialogView.pbGame
        pbGame.isVisible = false

        dialogBuilder.setPositiveButton(getString(R.string.ok)) {
                dialog, which ->

        }
        dialogBuilder.setNegativeButton(getString(R.string.cancel)) {
                dialog, which ->
            //
        }

        return dialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()

        val dialog = dialog as AlertDialog
        val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            if (etGameCode.text.isNotEmpty() && etGameCode.text.length == 6) {
                checkGameAndJoin()
            } else {
                etGameCode.error = getString(R.string.game_code_length_requirement)
            }
        }
    }
    //
    private fun handleGameJoin(gameID: String, gameCode: String) {

        val dialog = dialog as AlertDialog

        pbGame.isVisible = false
        dialog.dismiss()

        gameHandler.gameJoined(gameID, gameCode)
    }

    fun checkGameAndJoin() {
        pbGame.isVisible = true

        FirebaseFirestore.getInstance().collection(FirebaseData.PENDING_GAMES)
            .whereEqualTo("code", etGameCode.text.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    etGameCode.error = getString(R.string.find_game_error)
                    pbGame.isVisible = false
                } else {
                    val game = documents.first()
                    val gameID = game.id.toString()
                    handleGameJoin(gameID, etGameCode.text.toString())
                }
            }
    }
}