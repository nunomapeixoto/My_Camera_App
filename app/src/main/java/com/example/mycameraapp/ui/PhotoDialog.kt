package com.example.mycameraapp.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.example.mycameraapp.GlideApp
import com.example.mycameraapp.R
import kotlinx.android.synthetic.main.dialog_fullscreen_image_view.*

class PhotoDialog : DialogFragment() {

    companion object {
        const val PHOTO_URI = "photoUri"
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.dialog_fullscreen_image_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor = ContextCompat.getColor(requireContext(),
            R.color.colorAccent
        )
        circularProgressDrawable.start()


        GlideApp.with(requireActivity())
            .load(Uri.parse(arguments?.getString(PHOTO_URI))).into(image_view)
    }


}