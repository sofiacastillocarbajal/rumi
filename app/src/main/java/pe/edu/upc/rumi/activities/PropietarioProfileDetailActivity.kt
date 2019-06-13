package pe.edu.upc.rumi.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageButton
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import org.json.JSONObject
import pe.edu.upc.rumi.R
import pe.edu.upc.rumi.network.RumiApi
import pe.edu.upc.rumi.util.UserDefaults

class PropietarioProfileDetailActivity : AppCompatActivity() {

    private lateinit var backImageButton: AppCompatImageButton
    private lateinit var cerrarSesionImageButton: AppCompatImageButton

    private lateinit var nameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var ocupacionValueTextView: TextView
    private lateinit var birthdateValueTextView: TextView

    private lateinit var editPasswordImageButton: AppCompatImageButton
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_propietario_profile_detail)

        supportActionBar?.hide()

        nameTextView = findViewById(R.id.nameTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        progressBar = findViewById(R.id.progressBar)
        addressTextView = findViewById(R.id.addressTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        ocupacionValueTextView = findViewById(R.id.ocupacionValueTextView)
        birthdateValueTextView = findViewById(R.id.birthdateValueTextView)
        backImageButton = findViewById(R.id.backImageButton)
        backImageButton.setOnClickListener {
            finish()
        }

        cerrarSesionImageButton = findViewById(R.id.cerrarSesionImageButton)
        cerrarSesionImageButton.setOnClickListener {
            showDialogSimple("Mensaje", "¿Seguro que desea cerrar sesión?")
        }

        editPasswordImageButton = findViewById(R.id.editPasswordImageButton)
        editPasswordImageButton.setOnClickListener {
            showDialogChangePassword()
        }

        if (UserDefaults.role == "ROOMER") {
            editPasswordImageButton.visibility = View.GONE
        }

        getParticipantData()
    }

    private fun getParticipantData() {
        nameTextView.apply { text = UserDefaults.name }
        phoneTextView.apply {
            if (UserDefaults.phone == "null" || UserDefaults.phone == ""  || UserDefaults.phone == null)
                text = "Sin teléfono"
            else
                text = UserDefaults.phone
        }
        addressTextView.apply { text = "Lima, Perú" }
        descriptionTextView.apply {
            if (UserDefaults.description == "null" || UserDefaults.description == "" || UserDefaults.description == null)
                text = "Sin descripción"
            else
                text = UserDefaults.description
        }
        ocupacionValueTextView.apply {
            if (UserDefaults.occupation == "null" || UserDefaults.occupation == "" || UserDefaults.occupation == null)
                text = "Sin ocupación"
            else
                text = UserDefaults.occupation
        }
        birthdateValueTextView.apply {
            if (UserDefaults.birthdate == "null" || UserDefaults.birthdate == "" || UserDefaults.birthdate == null)
                text = "Sin fecha"
            else
                text = UserDefaults.birthdate
        }
    }

    fun showDialogSimple(titulo: String?, detalle: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(detalle)
        builder.setPositiveButton("Ok"){dialog, which ->
            UserDefaults.clearFromSharedPreferences(this)
            val intent = Intent(this, FullscreenActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        builder.setNegativeButton("Cancel"){dialog, which ->}
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun showDialogChangePassword() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPasswordEditText = view.findViewById(R.id.newPasswordEditText) as EditText
        val confirmNewPasswordEditText = view.findViewById(R.id.confirmNewPasswordEditText) as EditText

        builder.setTitle("Ingrese nueva contraseña")
        builder.setView(view)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            val newPass = newPasswordEditText.text.toString()
            val confirmNewPass = confirmNewPasswordEditText.text.toString()

            if (newPass == "" || confirmNewPass == "") Toast.makeText(this, "No deje ningún campo vacio.", Toast.LENGTH_LONG).show()
            else if (newPass != confirmNewPass) Toast.makeText(this, "Las contraseñas ingresadas no coinciden.", Toast.LENGTH_LONG).show()
            else updatePassword(newPass)
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 -> dialog.cancel() }
        builder.show()
    }

    fun updatePassword(newPass: String) {
        progressBar.visibility = View.VISIBLE

        val jsonBody = JSONObject()
        jsonBody.put("Password", newPass)
        jsonBody.put("ConfirmPassword", newPass)

        AndroidNetworking.patch(RumiApi.resetPassword)
            .addHeaders("Authorization", "Bearer ${UserDefaults.token}")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(jsonBody)
            .build()
            .getAsString(
                object: StringRequestListener {
                    override fun onResponse(response: String?) {
                        response?.apply {
                            if (response == "true"){
                                showToastMessage("Contraseña actualizada correctamente")
                            }
                            else{
                                showToastMessage("Error al actualizar la contraseña.")
                            }
                        }
                        progressBar.visibility = View.GONE
                    }
                    override fun onError(anError: ANError?) {
                        progressBar.visibility = View.GONE

                        anError?.apply {
                            if (errorCode == 0){
                                showToastMessage("Error de conexión.")
                            }
                            else{
                                showToastMessage("Error al actualizar la contraseña.")
                            }
                        }
                    }
                })
    }

    fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
