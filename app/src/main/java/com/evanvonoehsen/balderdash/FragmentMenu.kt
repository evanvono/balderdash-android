package com.evanvonoehsen.balderdash

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.evanvonoehsen.balderdash.data.PendingGame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.view.*

class FragmentMenu : Fragment() {
    var pendingGamesCollection = FirebaseFirestore.getInstance().collection(
        FirebaseData.PENDING_GAMES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        view.btnCreateGame.setOnClickListener {
            createPendingGame()
        }
        view.btnJoinGame.setOnClickListener {
            joinPendingGame()
        }
        return view
    }

    companion object {
        const val GAME_CODE = "GAME_CODE"
        const val GAME_ID = "GAME_ID"
        const val IS_HOST = "IS_HOST"
        const val MIN_GAME_CODE = 100000
        const val MAX_GAME_CODE = 999999
        const val PERMISSION_REQUEST_CODE = 1001

        @JvmStatic
        fun newInstance() = FragmentMenu()
    }

    private fun createPendingGame() {
        var code = ((Math.random() * (MAX_GAME_CODE - MIN_GAME_CODE)) + MIN_GAME_CODE).toInt().toString()
        val pendingGame = PendingGame(
            code,
            mutableListOf(FirebaseAuth.getInstance().currentUser!!.uid),
            mutableListOf(FirebaseData.username),
            false
        )

        pendingGamesCollection.add(pendingGame)
            .addOnSuccessListener { documentReference ->


                val intent = Intent(activity, JoinGameActivity::class.java)
                intent.putExtra(GAME_CODE, code)
                intent.putExtra(GAME_ID, documentReference.id)
                intent.putExtra(IS_HOST, true)
                startActivity(intent)
            }
            .addOnFailureListener{
                Toast.makeText(activity,
                    "Error ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun joinPendingGame() {
        JoinGameDialog().show(activity!!.supportFragmentManager, "TAG_CITY_DIALOG")
    }

}