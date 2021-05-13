package com.evanvonoehsen.balderdash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.evanvonoehsen.balderdash.data.Definition
import com.evanvonoehsen.balderdash.data.WordResult
import com.evanvonoehsen.balderdash.network.WordAPI
import kotlinx.android.synthetic.main.fragment_choose_word.*
import kotlinx.android.synthetic.main.fragment_choose_word.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentChooseWord.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentChooseWord : Fragment() {

    lateinit var retrofit: Retrofit
    lateinit var wordAPI: WordAPI

    val oldestWordnikDate = LocalDate.parse("2015-01-01")
    val numPossibleWordnikDays = ChronoUnit.DAYS.between(oldestWordnikDate, LocalDate.now())


    //TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_choose_word, container, false)

        listOf<Button>(view.btnFirstWord,view.btnSecondWord,view.btnThirdWord,view.btnFourthWord).forEach {
            it.alpha = 0f
            setButtonWord(it)
        }

        return view
    }

    private fun setButtonWord(wordButton: Button) {
        val randDate = generateRandomDateString()

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.wordnik.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        wordAPI = retrofit.create(WordAPI::class.java)

        val call = wordAPI.getWord(
            randDate,
            "7j2dwkbnnjm8w7wds1q171dwjpzymhp8ijcusvysz3d9oz90a"
        ) //replace w build config
        call.enqueue(object : Callback<WordResult> {
            override fun onResponse(call: Call<WordResult>, response: Response<WordResult>) {
                val body = response.body()!!
                wordButton.setText(body.word)

                val definition = body.definitions?.minBy { ((it as Definition).text as String).length }
                val trimmedDefinition = definition?.text.toString().split(";")[0].split(".")[0]
                wordButton.setOnClickListener {
                    (activity as GameActivity).wordSelected(body.word!!, trimmedDefinition)
                }
                startAlphaAnim(wordButton, 0)

            }

            override fun onFailure(call: Call<WordResult>, t: Throwable) {
                Log.v("TAG","failed to get words")
//                Toast.makeText(activity, "Could not get words", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun generateRandomDateString(): String {
        val randomDayNum = (Math.random() * numPossibleWordnikDays).toLong()
        val selectedDate = oldestWordnikDate.plusDays(randomDayNum)
        return selectedDate.toString()
    }

    fun startAlphaAnim(btn: Button, offset: Long) {
        val animation1 = AlphaAnimation(0.0f, 1.0f)
        animation1.startOffset = offset
        animation1.duration = 2000
        animation1.fillAfter = true
        btn.startAnimation(animation1)
        btn.alpha = 1.0f
    }

    companion object {

        const val TAG = "CHOOSE_WORD_TAG"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentChooseWord.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
//            param1: String, param2: String
            ) =
            FragmentChooseWord()
//                .apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
    }
}