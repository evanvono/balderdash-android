package com.evanvonoehsen.balderdash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_write_definition.*
import kotlinx.android.synthetic.main.fragment_write_definition.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "word"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentWriteDefinition.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentWriteDefinition : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var view = inflater.inflate(R.layout.fragment_write_definition, container, false)
        view.tvWordToDefine.text = param1.toString()
        view.btnDoneDefinition.setOnClickListener {
            val userDefinition = view.etDefinition.text.toString()
            (activity as GameActivity).definitionWritten(param1.toString(), userDefinition)
        }

        return view
    }

    companion object {
        const val TAG = "WRITE_DEFINITION_TAG"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment FragmentWriteDefinition.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            FragmentWriteDefinition().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}