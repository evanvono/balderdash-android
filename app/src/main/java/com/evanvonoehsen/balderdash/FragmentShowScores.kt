package com.evanvonoehsen.balderdash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_choose_definition.*
import kotlinx.android.synthetic.main.fragment_choose_definition.view.*
import kotlinx.android.synthetic.main.fragment_show_scores.*
import kotlinx.android.synthetic.main.fragment_show_scores.view.*
import kotlinx.android.synthetic.main.participant_row.view.*
import kotlinx.android.synthetic.main.show_definition_row.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentShowScores.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentShowScores : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: ArrayList<String>? = ArrayList<String>()
    private var param2: ArrayList<Int>? = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getStringArrayList(ARG_PARAM1)
            param2 = it.getIntegerArrayList(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        Log.e("TAG", param1.toString() + " spacer " + param2.toString())
        var view = inflater.inflate(R.layout.fragment_show_scores, container, false)

        for (i in 0..param1!!.size) {
            addShowDefinitionRow(view,param1!!.get(i), param2!!.get(i).toString())
        }
        view.btnCloseScores.setOnClickListener {
            (activity as GameActivity).checkReadyAndNewRound()
        }
        return view
    }

    fun addShowDefinitionRow(view: View, user: String, score: String) {
        val scoreRow = layoutInflater.inflate(R.layout.participant_row, null)
        scoreRow.tvUsername.text = user
        scoreRow.tvHost.text = score

        view.layoutMainScores.addView(scoreRow, 0)
    }

    companion object {
        const val TAG = "SHOW_SCORES_TAG"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentShowScores.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<String>, param2: ArrayList<Int>) =
            FragmentShowScores().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PARAM1, param1)
                    putIntegerArrayList(ARG_PARAM2, param2)
                }
            }
    }
}