package com.radhapyari.ai_for_crop_diseases_detection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.radhapyari.ai_for_crop_diseases_detection.R


class ChangePasswordFragment : DialogFragment() {


    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var updateButton: View
    private lateinit var closeButton: ImageView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =  inflater.inflate(R.layout.fragment_change_password, container, false)

        oldPassword = binding.findViewById(R.id.old_password)
        newPassword = binding.findViewById(R.id.new_password)
        updateButton = binding.findViewById(R.id.updateBtn)
        closeButton = binding.findViewById(R.id.Close)


        closeButton.setOnClickListener {
            dismiss()
        }


        updateButton.setOnClickListener {
            val oldpass = oldPassword.text.toString().trim()
            val newpass = newPassword.text.toString().trim()

            val user = auth.currentUser
            val email = user?.email

            if(user != null && email != null && oldpass.isNotEmpty() && newpass.isNotEmpty()){
                val credential = EmailAuthProvider.getCredential(email, oldpass)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newpass)
                            .addOnSuccessListener {
                                Toast.makeText(context,"Password Updated Successfully", Toast.LENGTH_SHORT).show()
                                dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Re-auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                Toast.makeText(context, "Please enter both passwords", Toast.LENGTH_SHORT).show()
            }
        }


        return binding
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


}