package com.radhapyari.ai_for_crop_diseases_detection.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.radhapyari.ai_for_crop_diseases_detection.R
import com.radhapyari.ai_for_crop_diseases_detection.databinding.FragmentProfileBinding



class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentProfileBinding
    private lateinit var navControl: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navControl = Navigation.findNavController(view)

//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.homeFragment)
//                }
//            }
//        )

        loadUserData()

        val user = auth.currentUser


        binding.usernameEdit.setOnClickListener {
            showEditUsernameDialog()
        }
        binding.emailPart.setOnClickListener {
            val email = user?.email
            AlertDialog.Builder(requireContext())
                .setTitle("Email")
                .setMessage(email)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()

        }

        binding.passwordEdit.setOnClickListener {
            val dialog = ChangePasswordFragment()
            dialog.show(parentFragmentManager, "ChangePasswordDialog")
        }
        binding.contactPart.setOnClickListener {
            showContactUsDialog()
        }

        binding.logoutPart.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_profileFragment_to_signInFragment)
        }
    }

    private fun loadUserData(){
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("Users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    val email = document.getString("email") ?: "No email"

                    binding.username.text = username
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditUsernameDialog() {
        val context = requireContext()

        val editText = EditText(context)
        editText.hint = "Enter new username"
        editText.setText(binding.username.text.toString())

        AlertDialog.Builder(context)
            .setTitle("Edit Username")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateUsername(newName)
                } else {
                    Toast.makeText(context, "Username can't be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun updateUsername(newName: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("Users").document(uid)
            .update("username", newName)
            .addOnSuccessListener {
                binding.username.text = newName
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showContactUsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Contact Us")
            .setMessage("For help, email:\nagricareblaze@gmail.com")
            .setPositiveButton("Email") { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:agricareblaze@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "App Support")
                }
                startActivity(Intent.createChooser(intent, "Send Email"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}