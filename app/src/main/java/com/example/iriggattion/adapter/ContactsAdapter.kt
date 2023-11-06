package com.example.iriggattion.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.iriggattion.databinding.ContactsItemBinding
import com.example.iriggattion.model.ContactModel

class ContactsAdapter: RecyclerView.Adapter<ContactsAdapter.ViewHolderContacts> {
    private lateinit var binding : ContactsItemBinding
    private val context : Context
    var contactsArrayList : ArrayList<ContactModel>

    constructor(context: Context, contactsArrayList: ArrayList<ContactModel>) : super() {
        this.context = context
        this.contactsArrayList = contactsArrayList
    }
    inner class ViewHolderContacts(itemView: View): RecyclerView.ViewHolder(itemView){
        var name1 : TextView = binding.date
        var contact1 : TextView = binding.tvState
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderContacts {
        binding = ContactsItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolderContacts(binding.root)
    }

    override fun getItemCount(): Int {
        return contactsArrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolderContacts, position: Int) {
        val model = contactsArrayList[position]
        val name = model.fullName
        val contact = model.phone

        holder.apply {
            name1.text = name
            contact1.text = contact
        }
        holder.itemView.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$contact")
            if (dialIntent.resolveActivity(holder.itemView.context.packageManager) != null) {
                holder.itemView.context.startActivity(dialIntent)
            } else {
                Toast.makeText(holder.itemView.context, "No app can handle this action", Toast.LENGTH_SHORT).show()
            }
        }
    }
}