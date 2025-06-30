package com.radhapyari.ai_for_crop_diseases_detection.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.radhapyari.ai_for_crop_diseases_detection.R
import com.radhapyari.ai_for_crop_diseases_detection.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }

    private fun init(view: View){
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents(){

        binding.authTestView.setOnClickListener {
            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
        binding.skip.setOnClickListener {
            navControl.navigate(R.id.action_signUpFragment_to_homeFragment)
        }

        binding.signupbtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val pass = binding.password.text.toString().trim()
            val verifyPass = binding.confirmpassword.text.toString().trim()
            val username = binding.username.text.toString().trim()

            if(email.isNotEmpty() && pass.isNotEmpty() && verifyPass.isNotEmpty()){
                if(pass == verifyPass){
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                        OnCompleteListener{
                            if(it.isSuccessful){
                                val uid = auth.currentUser?.uid

                                // Store username in Firestore
                                val userMap = hashMapOf(
                                    "username" to username,
                                    "email" to email
                                )

                                val db = FirebaseFirestore.getInstance()
                                db.collection("Users").document(uid ?: "").set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()
                                        navControl.navigate(R.id.action_signUpFragment_to_homeFragment)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }

                            }else{
                                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }else{
                    Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "Empty fields Not Allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }


}