package com.example.mycameraapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseViewModelFactory: FirebaseViewModelFactory
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_register.visibility = View.INVISIBLE
        button_login.text = "Registar"
        supportActionBar?.title = "Registar novo utilizador"

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
                    FirebaseModel.REGISTER_SUCCESS -> {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    FirebaseModel.REGISTER_FAILED -> {

                    }
                }
            })

        button_login.setOnClickListener {
            if (!input_password.text.toString().isNullOrBlank() && !input_user.text.toString().isNullOrBlank()) {
                setViewsVisibility(true)
                firebaseViewModel.register(
                    input_user.text.toString(),
                    input_password.text.toString(),
                    this
                )
            }
            else {
                Toast.makeText(this, "Email e/ou password em falta.", Toast.LENGTH_SHORT).show()
            }
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