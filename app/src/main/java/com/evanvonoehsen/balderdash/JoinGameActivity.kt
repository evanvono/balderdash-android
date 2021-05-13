package com.evanvonoehsen.balderdash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.evanvonoehsen.balderdash.adapter.ParticipantsAdapter
import com.evanvonoehsen.balderdash.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.Document
import kotlinx.android.synthetic.main.activity_join_game.*
import java.util.*
import kotlin.concurrent.schedule

class JoinGameActivity : AppCompatActivity() {

    lateinit var participantsAdapter: ParticipantsAdapter

    private lateinit var pendingGameRef: DocumentReference
    private var listenerReg: ListenerRegistration? = null
    private lateinit var pendingGame: PendingGame
    var isHost = false
    lateinit var gameCode: String
    lateinit var gameID: String

    companion object {
        val HOST_EXTRA = "HOST"
        val GAME_ID_EXTRA = "GAME_ID_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        gameID = intent.getStringExtra(FragmentMenu.GAME_ID)!!
        gameCode = intent.getStringExtra(FragmentMenu.GAME_CODE)!!
        isHost = intent.getBooleanExtra(FragmentMenu.IS_HOST, false)
        tvCode.text = gameCode
        pendingGameRef = FirebaseFirestore.getInstance().collection(
            FirebaseData.PENDING_GAMES).document(gameID!!)

        participantsAdapter = ParticipantsAdapter(this)
        recyclerParticipants.adapter = participantsAdapter

        setupActionBars()

        btnStart.visibility = View.GONE

        if (isHost) {
            btnStart.setOnClickListener{ startGame() }
            btnCancel.setOnClickListener{ cancelGame() }
        } else {
            btnCancel.setOnClickListener{ leaveGame() }
        }
        initFirebaseQuery()
    }

    private fun setupActionBars() {
        this.supportActionBar?.hide()

        val window: Window = this@JoinGameActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.statusBarColor = ContextCompat.getColor(this@MainMenuActivity, R.color.persimmon_variant)
    }

    private fun startGame() {
        if (participantsAdapter.itemCount >= 2) {
            var beganPendingGame = pendingGame
            beganPendingGame.started = true
            pendingGameRef.set(beganPendingGame)

            var definitionsMap = mutableMapOf<String,String>()
            var scoresMap = mutableMapOf<String,Long>()
            pendingGame.playerUsernames.forEach {
                definitionsMap.put(it, "")
                scoresMap.put(it, 0.toLong())
            }
            definitionsMap.put("#CORRECT#","")


            FirebaseFirestore.getInstance().collection(FirebaseData.GAME_STATES).document(gameID).set(GameState(0))
            FirebaseFirestore.getInstance().collection(FirebaseData.LIVE_GAMES).document(gameID).set(
                LiveGame(
                    FirebaseAuth.getInstance().uid.toString(),
                    FirebaseAuth.getInstance().uid.toString(),
                    pendingGame.players,
                    pendingGame.playerUsernames,
                    scoresMap,
                    "",
                    "",
                    definitionsMap,
                    mutableListOf(),
                    0
                )
            ).addOnSuccessListener {
                var intent = Intent(this, GameActivity::class.java)
                intent.putExtra(HOST_EXTRA, true)
                intent.putExtra(GAME_ID_EXTRA, gameID)
                startActivity(intent)

                Timer("Deleting the pending game", false).schedule(5000) {
                    FirebaseFirestore.getInstance().collection(FirebaseData.PENDING_GAMES).document(gameID)
                        .delete()
                    finish()
                }
            }.addOnFailureListener {
                //failed to start the game
            }



        } else {
            Toast.makeText(this, "There must be at least two players", Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelGame() {
        FirebaseFirestore.getInstance().collection(FirebaseData.PENDING_GAMES).document(gameID)
            .delete()
    }
    private fun leaveGame() {

    }

    private fun updatePendingGame(data: Map<String, Any>) {
        pendingGame = PendingGame(
            data.get("code") as String,
            data.get("players") as MutableList<String>,
            data.get("playerUsernames") as MutableList<String>,
            data.get("started") as Boolean
        )
    }

    private fun initFirebaseQuery () {
        val queryRef = FirebaseFirestore.getInstance().collection(FirebaseData.PENDING_GAMES)
        val docRef = queryRef.whereEqualTo("code", gameCode)
        listenerReg = docRef.addSnapshotListener { querySnapshots, e ->

            val change = querySnapshots!!.documentChanges.first()

            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    val data = change.document.data!!
                    updatePendingGame(data)
                    var newParticipantsList: MutableList<Participant> = mutableListOf()
                    var hostBool = true
                    for (username in data.get("playerUsernames") as List<String>) {
                        newParticipantsList.add(Participant(username, hostBool))
                        hostBool = false
                    }
                    participantsAdapter.changeInternalList(newParticipantsList)

                }
                DocumentChange.Type.MODIFIED -> {
                    val data = change.document.data!!
                    if ((data.get("started") as Boolean) && !isHost) {
                        var intent = Intent(this, GameActivity::class.java)
                        intent.putExtra(HOST_EXTRA, false)
                        intent.putExtra(GAME_ID_EXTRA, gameID)
                        startActivity(intent)

                    } else {
                        var newParticipantsList: MutableList<Participant> = mutableListOf()

                        var hostBool = true
                        for (username in data.get("playerUsernames") as List<String>) {
                            newParticipantsList.add(Participant(username, hostBool))
                            hostBool = false
                        }
                        updatePendingGame(data)
                        participantsAdapter.changeInternalList(newParticipantsList)
                        if (participantsAdapter.itemCount > 1 && isHost) {
                            btnStart.visibility = View.VISIBLE
                        } else {
                            btnStart.visibility = View.GONE
                        }
                        Log.d("TAG", "Modified game: ${change.document.data}")
                    }
                }
                DocumentChange.Type.REMOVED -> {
//                    Toast.makeText(this, getString(R.string.game_deleted), Toast.LENGTH_LONG).show()
//                    Log.e("TAG", "Finishing")
                    finish()
                };
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }
}
