package com.evanvonoehsen.balderdash

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.evanvonoehsen.balderdash.data.GameState
import com.evanvonoehsen.balderdash.data.LiveGame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GameActivity : AppCompatActivity() {

    private var isHost: Boolean = false
    private lateinit var gameID: String
    private lateinit var liveGame: LiveGame
    private lateinit var gameState: GameState

    private var listenerReg: MutableList<ListenerRegistration> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        isHost = intent.getBooleanExtra(JoinGameActivity.HOST_EXTRA, false)
        gameID = intent.getStringExtra(JoinGameActivity.GAME_ID_EXTRA).toString()

        initLiveGameQuery()
        initStateQuery()
        setupActionBars()

    }

    private fun initStateQuery () {
        val queryRef = FirebaseFirestore.getInstance().collection(FirebaseData.GAME_STATES)
        val docRef = queryRef.whereEqualTo(FieldPath.documentId(), gameID)
        val listener = docRef.addSnapshotListener { querySnapshots, e ->
            val change = querySnapshots!!.documentChanges.first()
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    val data = change.document.data!!
                    updateLocalGameState(data)
                    handleGameState()
                }
                DocumentChange.Type.MODIFIED -> {
                    val data = change.document.data!!
                    updateLocalGameState(data)
                    handleGameState()

                }
                DocumentChange.Type.REMOVED -> {
                    finish()
                };
            }
        }
        listenerReg.add(listener)
    }
    private fun initLiveGameQuery () {
        val queryRef = FirebaseFirestore.getInstance().collection(FirebaseData.LIVE_GAMES)
        val docRef = queryRef.whereEqualTo(FieldPath.documentId(), gameID)
        val listener = docRef.addSnapshotListener { querySnapshots, e ->
            val change = querySnapshots!!.documentChanges.first()
            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    val data = change.document.data!!
                    updateLocalLiveGame(data)

                }
                DocumentChange.Type.MODIFIED -> {
                    val data = change.document.data!!
                    updateLocalLiveGame(data)

                    if (isHost) {
                        if (gameState.state == 1) {
                            checkIfAllDefinitionsFull()
                        }
                        else if (gameState.state == 2) {
                            if (liveGame.readyForResults.toInt() >= liveGame.playerUsernames.size) {
                                Log.e("TAG", "moving to state 3")
                                gameState.state = 3
                                updateCloudGameState(gameState)
                            }
                        }
                        else if (gameState.state == 3) {
                            checkIfAllDefinitionsCleared()
                        }
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    finish()
                };
            }
        }
        listenerReg.add(listener)
    }

    private fun updateLocalGameState(data: Map<String, Any>) {
        gameState = GameState((data.get("state") as Long).toInt())
    }

    private fun updateLocalLiveGame(data: Map<String, Any>) {
        liveGame = LiveGame(
            data.get("host") as String,
            data.get("chooser") as String,
            data.get("players") as List<String>,
            data.get("playerUsernames") as List<String>,
            data.get("pointTotals") as MutableMap<String, Long>,
            data.get("selectedWord") as String,
            data.get("correctDefiniton") as String,
            data.get("playerDefinitions") as MutableMap<String,String>,
            data.get("pastWords") as MutableList<String>,
            data.get("readyForResults") as Long
        )
    }

    private fun setWord(word: String, definition: String) {
        liveGame.selectedWord = word
        liveGame.correctDefiniton = definition
        liveGame.playerDefinitions.put("#CORRECT#",definition)
        updateCloudLiveGame(liveGame)
    }

    private fun checkIfAllDefinitionsCleared() {
        for (definition in liveGame.playerDefinitions.values) {
            if (!definition.equals("")) {
                return
            }
        }

        checkScoresAndNewRound()
    }

    private fun checkIfAllDefinitionsFull() {
        for (definition in liveGame.playerDefinitions.values) {
            if (definition.equals("")) {
                return
            }
        }
        gameState.state = 2
        updateCloudGameState(gameState)
    }

    private fun checkScoresAndNewRound() {
        for (score in liveGame.pointTotals.values) {
            if (score >= WINNING_SCORE) {
                //TODO: start results activity and show the results
            }
        }
        newRound()
    }

    public fun checkReadyAndNewRound() {
        liveGame.readyForResults -= 1
        updateCloudLiveGame(liveGame)
        if (liveGame.readyForResults.toInt() == 0) {
            checkScoresAndNewRound()
        } else {
            showFragmentByTag(FragmentWait.TAG, mapOf("param1" to "Waiting for other players..."))
        }
    }

    private fun newRound() {
        gameState.state = 0
        liveGame.readyForResults = 0.toLong()
        liveGame.pastWords.add(liveGame.selectedWord)
        liveGame.selectedWord = ""
        var chooserIndex = liveGame.players.indexOf(liveGame.chooser) + 1
        if (chooserIndex >= liveGame.players.size) {
            chooserIndex = 0
        }
        liveGame.chooser = liveGame.players.get(chooserIndex)
        updateCloudLiveGame(liveGame)
        updateCloudGameState(gameState)
    }

    public fun wordSelected(word: String, definition: String) {
        setWord(word, definition)
        gameState = GameState(1) //definition writing state
        updateCloudGameState(gameState)
    }

    public fun definitionWritten(word: String, definition: String) {
        var definitionsMap = liveGame.playerDefinitions
        definitionsMap.put(FirebaseData.username, definition)
        liveGame.playerDefinitions = definitionsMap
        updateCloudLiveGame(liveGame)
        showFragmentByTag(FragmentWait.TAG, mutableMapOf("param1" to "Waiting for all players to submit definitions..."))
    }

    private fun showScores() {
        var usersList = arrayListOf<String>()
        var scoresList = arrayListOf<Integer>()
        for (user in liveGame.playerUsernames) {
            scoresList.add(Integer(liveGame.pointTotals.get(user)!!.toInt()))
        }
        for (user in liveGame.playerUsernames) {
            usersList.add(user)
        }
        showFragmentByTag(FragmentShowScores.TAG, mapOf("param1" to usersList, "param2" to scoresList)) //inide this fragment, call below function in button
    }

    private fun keysetToStringArrayList(keyset: Iterable<String>): ArrayList<String> {
        var list = ArrayList<String>()
        keyset.forEach {
            list.add(it)
        }
        return list
    }

    public fun clearDefinitionAndWait() {
        liveGame.playerDefinitions.put(FirebaseData.username, "")
        liveGame.playerDefinitions.put("#CORRECT#","")
        showFragmentByTag(FragmentWait.TAG, mapOf("param1" to "Waiting for other players..."))
        updateCloudLiveGame(liveGame)

    }

    public fun chooseDefinition(user: String) {
        if (user.equals("#CORRECT#")) {
            var points = (liveGame.pointTotals.get(FirebaseData.username) as Long).toInt()
            liveGame.pointTotals.put(FirebaseData.username, points + 1.toLong())
        } else {
            var points = (liveGame.pointTotals.get(FirebaseData.username) as Long).toInt()
            liveGame
            liveGame.pointTotals.put(user, points + 1.toLong())
        }
        liveGame.readyForResults += 1
        updateCloudLiveGame(liveGame)
        showFragmentByTag(FragmentWait.TAG, mapOf("param1" to "Waiting for players to choose definitions..."))
    }
    private fun updateCloudGameState(newGameState: GameState) {
        val queryRef = FirebaseFirestore.getInstance().collection(FirebaseData.GAME_STATES).document(gameID)
        gameState = newGameState
        queryRef.set(newGameState)
    }

    private fun updateCloudLiveGame(newLiveGame: LiveGame) {
        val queryRef = FirebaseFirestore.getInstance().collection(FirebaseData.LIVE_GAMES).document(gameID)
        liveGame = newLiveGame
        queryRef.set(newLiveGame)
    }


    private fun handleGameState() {
        Log.v("TAG", liveGame.chooser + "as compared to" + FirebaseAuth.getInstance().uid.toString())
        when (gameState.state) {
            0 -> {
                if (liveGame.chooser.equals(FirebaseAuth.getInstance().uid.toString())) {
                    showFragmentByTag(FragmentChooseWord.TAG, mapOf())
                } else {
                    showFragmentByTag(FragmentWait.TAG, mapOf("param1" to "Waiting for word to be chosen..."))
                }
            }
            1 -> {
                showFragmentByTag(FragmentWriteDefinition.TAG, mapOf("param1" to liveGame.selectedWord))
            }
            2 -> {
                var playerDefinitions: MutableMap<String, String> = mutableMapOf()
                for (entry in liveGame.playerDefinitions.entries) {
                    if (entry.key != FirebaseData.username) {
                        playerDefinitions.put(entry.key, entry.value)
                    }
                }
                Log.e("TAG", "Going to choose definition now")
                Log.v("TAG", keysetToStringArrayList(playerDefinitions.values).toString())
                showFragmentByTag(FragmentChooseDefinition.TAG, mapOf("param1" to keysetToStringArrayList(playerDefinitions.keys), "param2" to keysetToStringArrayList(playerDefinitions.values), "param3" to liveGame.selectedWord, "param4" to false))
            }
            3 -> {
                showFragmentByTag(FragmentChooseDefinition.TAG, mapOf("param1" to keysetToStringArrayList(liveGame.playerDefinitions.keys), "param2" to keysetToStringArrayList(liveGame.playerDefinitions.values), "param3" to liveGame.selectedWord, "param4" to true))
            }
            4 -> {
                showScores()
            }
        }

    }

    fun showFragmentByTag(tag: String, data: Map<String, Any>) {
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag(tag)
        when (tag) {
            FragmentWait.TAG -> {
                fragment = FragmentWait.newInstance(data.get("param1") as String)
            }
            FragmentChooseWord.TAG -> {
                fragment = FragmentChooseWord()
            }
            FragmentWriteDefinition.TAG -> {
                fragment = FragmentWriteDefinition.newInstance(data.get("param1") as String)
            }
            FragmentChooseDefinition.TAG -> {
                fragment = FragmentChooseDefinition.newInstance(
                        data.get("param1") as ArrayList<String>,
                        data.get("param2") as ArrayList<String>,
                        data.get("param3") as String,
                        data.get("param4") as Boolean
                )
            }
            FragmentShowScores.TAG -> {
                fragment = FragmentShowScores()
            }
        }

        var fragTrans = supportFragmentManager.beginTransaction()
        fragTrans.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragTrans.replace(R.id.fragmentContainer, fragment!!, tag)
        fragTrans.commit()
    }

    fun setupActionBars() {
        this.supportActionBar?.hide()

        val window: Window = this@GameActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this@GameActivity, R.color.persimmon_variant)
    }

    companion object {
        const val WINNING_SCORE: Int = 7
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg.forEach {
            it.remove()
        }
    }

}