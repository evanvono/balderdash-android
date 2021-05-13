package com.evanvonoehsen.balderdash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.evanvonoehsen.balderdash.adapter.ViewPagerAdapter
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.evanvonoehsen.balderdash.data.PendingGame
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity(), JoinGameDialog.GameHandler {
    private var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            //do something here
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.actionBar?.hide()
        setContentView(R.layout.activity_main_menu)

        val viewPagerAdapter =
            ViewPagerAdapter(this, 2)
        mainViewPager.adapter = viewPagerAdapter

        mainViewPager.registerOnPageChangeCallback(pageChangeCallback)

        var pageNames: Array<String> = resources.getStringArray(R.array.tab_names)
        TabLayoutMediator(tabLayout, mainViewPager) { tab, position ->
            tab.text = pageNames[position]
        }.attach()

        mainViewPager.setPageTransformer(ZoomOutPageTransformer())

        setupActionBars()
        setupAccountInfo()
    }
    override fun onDestroy() {
        super.onDestroy()
        mainViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    fun setupActionBars() {
        this.supportActionBar?.hide()

        val window: Window = this@MainMenuActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.statusBarColor = ContextCompat.getColor(this@MainMenuActivity, R.color.persimmon_variant)
    }

    fun setupAccountInfo()  {
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        val docRef = FirebaseFirestore.getInstance().collection(FirebaseData.USER_DATA).document(userID)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    FirebaseData.pastWords = document.data!!.get("pastWords") as MutableList<String>
                    FirebaseData.username = document.data!!.get("username") as String
                } else {
                    Toast.makeText(this, "Signed out due to database error", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                setupAccountInfo()
            }
    }

    override fun gameJoined(gameID: String, gameCode: String) {
        Log.v("TAG", "Made it to gameJoined")

        //TODO: Fix a bug where it always crashes here. Prob something to do with the get document request

        val docRef = FirebaseFirestore.getInstance().collection(FirebaseData.PENDING_GAMES).document(gameID)
        docRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    var data = documentSnapshot.data!!

                    var pastGameState = PendingGame(
                            data.get("code") as String,
                            data.get("players") as MutableList<String>,
                            data.get("playerUsernames") as MutableList<String>,
                            data.get("started") as Boolean
                    )
                    Log.v("TAG", pastGameState.toString())
                    pastGameState.playerUsernames.add(FirebaseData.username)
                    pastGameState.players.add(FirebaseAuth.getInstance().currentUser!!.uid)
                    Log.v("TAG", pastGameState.toString())
                    docRef.set(pastGameState).addOnSuccessListener {
                        val intent = Intent(this, JoinGameActivity::class.java)
                        intent.putExtra(FragmentMenu.GAME_CODE, gameCode)
                        intent.putExtra(FragmentMenu.GAME_ID, gameID)
                        intent.putExtra(FragmentMenu.IS_HOST, false)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "Error! Please try again", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { err ->
                Log.v("TAG", err.toString())
            }
    }
}