package com.example.aasha

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.aasha.databinding.DialogProfileBinding
import com.google.firebase.auth.FirebaseUser
import java.lang.IllegalStateException

class ProfileDialogFragment : DialogFragment() {

    internal lateinit var listener: NoticeDialogListener
    private var userImageUrl: Uri? = null

    private val fileChooser = registerForActivityResult(OpenDocumentContract()) { uri ->
        userImageUrl = uri
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: ProfileDialogFragment,
                userName: String?,
                textView: TextView,
                userImageUrl: Uri?,
                imageView: ImageView
        )
        fun onDialogNeutralClick(dialog: ProfileDialogFragment)
        fun getUserObject() : FirebaseUser?
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException("$context must implement NoticeDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_profile, null)
            val user = listener.getUserObject()

            val binding = DialogProfileBinding.bind(view)

            binding.profileImage.setOnClickListener {
                fileChooser.launch(arrayOf("image/*"))
            }

            binding.profileName.text = user?.displayName
            binding.profileJoinDate.text = user?.metadata?.creationTimestamp.toString()
            binding.nameChangeButton.setOnClickListener {
                binding.editNameLayout.visibility = View.VISIBLE
            }
            Glide.with(binding.profileImage)
                .load(user?.photoUrl)
                .into(binding.profileImage)

            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                .setPositiveButton(R.string.save_edits) { _, _ -> listener.onDialogPositiveClick(this,
                            binding.editNameEditText.text?.toString(),
                            binding.editNameEditText,
                            userImageUrl,
                            binding.profileImage
                )}
                .setNeutralButton(R.string.sign_out) { _, _ -> listener.onDialogNeutralClick(this) }

            builder.create()
        } ?: throw IllegalStateException("Activity can not be null")
    }
}