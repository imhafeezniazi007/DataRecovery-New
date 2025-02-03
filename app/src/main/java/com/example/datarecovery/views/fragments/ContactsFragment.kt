package com.example.datarecovery.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentContactsBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.views.activities.AllDuplicateActivity
import com.example.datarecovery.views.adapters.ContactParentAdapter

class ContactsFragment : Fragment() {
    lateinit var binding: FragmentContactsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val madapter = ContactParentAdapter(
            requireContext(),
            (requireActivity() as AllDuplicateActivity).duplicateContacts,
            (requireActivity() as AllDuplicateActivity).selectedContacts,
            object :
                DataListener {
                override fun onRecieve(any: Any) {
                    val item = any as ContactModel
                    Log.d("TAG", "onRecieve: ${item.name}")
                    if ((requireActivity() as AllDuplicateActivity).selectedContacts.contains(item)) {
                        (requireActivity() as AllDuplicateActivity).selectedContacts.remove(item)
                    } else {
                        (requireActivity() as AllDuplicateActivity).selectedContacts.add(item)
                    }
                }

            })
        binding.duplicateRV.adapter = madapter
        if ((requireActivity() as AllDuplicateActivity).duplicateContacts.isEmpty()) {
            binding.noTV.visibility = View.VISIBLE
        }
        clickListener()
    }

    private fun clickListener() {
        binding.deletelayout.setOnClickListener {
            if ((requireActivity() as AllDuplicateActivity).selectedContacts.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_item_selected), Toast.LENGTH_LONG).show()
            } else {
                (requireActivity() as AllDuplicateActivity).selectedContacts.withIndex().forEach {
                    it.value.id?.let { it1 ->
                        (requireActivity() as AllDuplicateActivity).deleteContactById(it1)
                    }
                }
                (requireActivity() as AllDuplicateActivity).filesDeleted(object : DataListener {
                    override fun onRecieve(any: Any) {
                        if (any as Boolean) {
                            (requireActivity() as AllDuplicateActivity).finish()
                        }
                    }

                })
            }
        }

    }

}