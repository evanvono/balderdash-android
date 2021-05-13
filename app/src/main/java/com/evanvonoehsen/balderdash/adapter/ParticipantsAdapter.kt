package com.evanvonoehsen.balderdash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evanvonoehsen.balderdash.R
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.evanvonoehsen.balderdash.data.Participant
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.participant_row.view.*


class ParticipantsAdapter: RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    lateinit var context: Context
    var  participantsList = mutableListOf<Participant>()


    constructor(context: Context) : super() {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val postRowView = LayoutInflater.from(context).inflate(R.layout.participant_row, parent, false)
        return ViewHolder(postRowView)
    }

    override fun getItemCount(): Int {
        return participantsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var participant = participantsList.get(holder.adapterPosition)
        holder.tvUsername.text = participant.username
        holder.tvHost.visibility = if (participant.isHost) {View.VISIBLE} else {View.GONE}
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tvUsername = itemView.tvUsername
        var tvHost = itemView.tvHost
    }

    fun changeInternalList(newList: MutableList<Participant>) {
        participantsList = newList
        notifyDataSetChanged()
    }
    
}