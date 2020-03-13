package com.example.mycameraapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mycameraapp.Injection
import com.example.mycameraapp.R
import com.example.mycameraapp.model.FirebaseModel
import com.example.mycameraapp.view_model.FirebaseViewModel
import com.example.mycameraapp.view_model.FirebaseViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseViewModelFactory: FirebaseViewModelFactory
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseViewModelFactory =
            Injection.provideFirebaseViewModelFactory()
        firebaseViewModel = ViewModelProvider(this, firebaseViewModelFactory).get(FirebaseViewModel::class.java)

        compositeDisposable = CompositeDisposable()

        compositeDisposable.add(firebaseViewModel.getLoginResult()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setViewsVisibility(false)
                when(it) {
                    FirebaseModel.LOGIN_SUCCESS -> {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    FirebaseModel.LOGIN_FAILED -> {

                    }
                }
            })

        button_login.setOnClickListener {
            if (!input_password.text.toString().isNullOrBlank() && !input_user.text.toString().isNullOrBlank()) {
                setViewsVisibility(true)
                firebaseViewModel.login(
                    input_user.text.toString(),
                    input_password.text.toString(),
                    this
                )
            }
            else {
                Toast.makeText(this, "Email e/ou password em falta.", Toast.LENGTH_SHORT).show()
            }
        }

        button_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed)
            compositeDisposable.dispose()
    }

    private fun setViewsVisibility(isLoading: Boolean) {
        button_login.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
        input_password.isEnabled = !isLoading
        input_user.isEnabled = !isLoading
    }
}