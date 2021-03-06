package pe.edu.upc.rumi.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.CardView
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import org.json.JSONObject
import pe.edu.upc.rumi.R
import pe.edu.upc.rumi.model.LoginResponse
import pe.edu.upc.rumi.network.RumiApi
import pe.edu.upc.rumi.util.UserDefaults

class LoginPropietarioActivity : AppCompatActivity() {

    private lateinit var registrarAhoraCardView: CardView
    private lateinit var iniciarSesionCardView: CardView

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var progressBar: ProgressBar

    private lateinit var googleAppCompatImageView: AppCompatImageView
    private lateinit var facebookAppCompatImageView: AppCompatImageView
    private lateinit var instagramAppCompatImageView: AppCompatImageView

    val TAG = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_propietario)
        supportActionBar?.hide()
        //validarSesion()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        progressBar = findViewById(R.id.progressBar)

        googleAppCompatImageView = findViewById(R.id.googleAppCompatImageView)
        googleAppCompatImageView.setOnClickListener { openWeb("https://www.youtube.com/")}
        facebookAppCompatImageView = findViewById(R.id.facebookAppCompatImageView)
        facebookAppCompatImageView.setOnClickListener { openWeb("https://www.facebook.com/")}
        instagramAppCompatImageView = findViewById(R.id.instagramAppCompatImageView)
        instagramAppCompatImageView.setOnClickListener { openWeb("https://www.instagram.com/")}

        registrarAhoraCardView = findViewById(R.id.registrarAhoraCardView)
        registrarAhoraCardView.setOnClickListener {
            val intent = Intent(this@LoginPropietarioActivity, RegisterPropietarioActivity::class.java)
            intent.putExtra("key", "Kotlin")
            startActivity(intent)
        }

        iniciarSesionCardView = findViewById(R.id.iniciarSesionCardView)
        iniciarSesionCardView.setOnClickListener {
            login()
        }
    }

    fun openWeb(url: String) {
        val openURL = Intent(android.content.Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    fun login() {
        if (emailEditText.text.toString() == "" || passwordEditText.text.toString() == ""){
            showDialogPositive("Error", "No deje ningun campo vacio")
        }
        else {
            loginRequest()
        }
    }

    private fun loginRequest() {
        iniciarSesionCardView.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val loginPost = JSONObject()
        loginPost.put("email", emailEditText.text)
        loginPost.put("password",passwordEditText.text)

        AndroidNetworking.post(RumiApi.signInUrl)
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(loginPost)
            .setPriority(Priority.HIGH)
            .setTag(TAG)
            .build()
            .getAsObject(LoginResponse::class.java,
                object: ParsedRequestListener<LoginResponse> {
                    override fun onResponse(response: LoginResponse?) {
                        response?.apply {
                            UserDefaults.name = profile.name
                            UserDefaults.surname = profile.surname
                            UserDefaults.phone = profile.phone
                            UserDefaults.gender = profile.gender
                            UserDefaults.birthdate = profile.birthdate
                            UserDefaults.image = profile.image
                            UserDefaults.description = profile.description
                            UserDefaults.occupation = profile.occupation
                            UserDefaults.score = profile.score
                            UserDefaults.role = role
                            UserDefaults.token = token
                            UserDefaults.saveInSharedPreferences(this@LoginPropietarioActivity)

                            if (UserDefaults.token != "") goToMainPropietario()
                        }
                    }
                    override fun onError(anError: ANError?) {
                        iniciarSesionCardView.isEnabled = true
                        progressBar.visibility = View.GONE

                        anError?.apply {
                            if (errorCode == 400){
                                showDialogPositive("Error","Error de credenciales, usuario o contraseña invalidos.")
                            }
                            else if (errorCode == 0){
                                showDialogPositive("Error","Error de conexión.")
                            }
                        }
                    }
                })
    }

    fun goToMainPropietario() {
        val intent = Intent(this@LoginPropietarioActivity, MainPropietarioActivity::class.java)
        intent.putExtra("key", "Kotlin")
        startActivity(intent)
        finishAffinity()
    }

    fun showDialogPositive(titulo: String?, detalle: String?) {
//        val builder = AlertDialog.Builder(this@LoginPropietarioActivity)
//        builder.setTitle(titulo)
//        builder.setMessage(detalle)
//        builder.setPositiveButton("Ok"){dialog, which ->}
//        val dialog: AlertDialog = builder.create()
//        dialog.show()

        Toast.makeText(this, detalle, Toast.LENGTH_LONG).show()
    }
}































