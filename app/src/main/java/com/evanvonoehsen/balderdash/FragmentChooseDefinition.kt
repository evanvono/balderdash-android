package com.evanvonoehsen.balderdash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.choose_definition_row.view.*
import kotlinx.android.synthetic.main.fragment_choose_definition.*
import kotlinx.android.synthetic.main.fragment_choose_definition.view.*
import kotlinx.android.synthetic.main.show_definition_row.view.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PLAYER_IDS = "playerIds"
private const val ARG_DEFINITIONS = "definitions"
private const val ARG_WORD = "word"
private const val ARG_IS_REVEAL = "isReveal"


/**
 * A simple [Fragment] subclass.
 * Use the [FragmentChooseDefinition.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentChooseDefinition : Fragment() {
    // TODO: Rename and change types of parameters
    private var playerIds: ArrayList<String>? = null
    private var definitions: ArrayList<String>? = null
    private var word: String = ""
    private var isReveal: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerIds = it.getStringArrayList(ARG_PLAYER_IDS)
            definitions = it.getStringArrayList(ARG_DEFINITIONS)
            word = it.getString(ARG_WORD).toString()
            isReveal = it.getBoolean(ARG_IS_REVEAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("TAG", "In create view of fragment now")
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_choose_definition, container, false)

        view.tvDisplayWord.text = word
        if (isReveal) {
            for (i in 0..playerIds!!.size - 1) {
                addShowDefinitionRow(view, playerIds!!.get(i), definitions!!.get(i))
            }
            view.btnCloseDefinitions.setOnClickListener {
                (activity as GameActivity).clearDefinitionAndWait()
            }
        } else {
            for (i in 0..playerIds!!.size - 1) {
                addChooseDefinitionRow(view, playerIds!!.get(i), definitions!!.get(i))
            }
            view.btnCloseDefinitions.visibility = View.GONE
        }

        return view
    }

    fun addChooseDefinitionRow(view: View, user: String, definition: String) {
        val definitionRow = layoutInflater.inflate(R.layout.choose_definition_row, null)
        definitionRow.tvPossibleDefinition.text = definition
        definitionRow.btnChooseDefinition.setOnClickListener {
            (activity as GameActivity).chooseDefinition(user)
        }

        view.layoutMain.addView(definitionRow, 0)

    }
    fun addShowDefinitionRow(view: View, user: String, definition: String) {
        val definitionRow = layoutInflater.inflate(R.layout.show_definition_row, null)
        definitionRow.tvUserDefinition.text = definition
        var username = user
        if (user == "#CORRECT#") {
            username = "Correct Answer"
        }
        definitionRow.tvDefinitionUser.text = username
        view.layoutMain.addView(definitionRow, 0)
    }



    companion object {
        const val TAG = "CHOOSE_DEFINITION_TAG"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param playerIds Parameter 1.
         * @param definitions Parameter 2.
         * @return A new instance of fragment FragmentChooseDefinition.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(playerIds: ArrayList<String>, definitions: ArrayList<String>, word: String, isReveal: Boolean) =
            FragmentChooseDefinition().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PLAYER_IDS, playerIds)
                    putStringArrayList(ARG_DEFINITIONS, definitions)
                    putString(ARG_WORD, word)
                    putBoolean(ARG_IS_REVEAL, isReveal)
                }
            }
    }
}