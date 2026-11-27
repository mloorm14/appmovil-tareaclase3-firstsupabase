package com.example.appsupabasetestteilor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.appsupabasetestteilor.data.SupabaseManager
import com.example.appsupabasetestteilor.models.Alumno
import com.example.appsupabasetestteilor.models.Materia
import com.example.appsupabasetestteilor.utils.SupabaseErrorHandler
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class acrListaSeleccion : AppCompatActivity() {
    private var consultaMaterias: Job? = null
    private var consultaAlumnos: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acr_lista_seleccion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val actNiveles = findViewById<AutoCompleteTextView>(R.id.listaNiveles)
        actNiveles.setAdapter(ArrayAdapter.createFromResource(this, R.array.niveles, android.R.layout.simple_spinner_dropdown_item))
        actNiveles.setOnItemClickListener { _, _, position, _ ->
            consultaAlumnos?.cancel()
            findViewById<ListView>(R.id.lvAlumnos).adapter = null
            llenarMaterias(position + 1)
        }
        findViewById<AutoCompleteTextView>(R.id.listaMaterias).setOnItemClickListener { _, _, _, _ ->
            cargarAlumnos()
        }
    }

    private fun cargarAlumnos() {
        consultaAlumnos?.cancel()
        consultaAlumnos = lifecycleScope.launch {
            try {
                val alumnos = SupabaseManager.client.from("alumnos")
                    .select { order("nombres", Order.ASCENDING) }
                    .decodeList<Alumno>()
                val datos = alumnos.map {
                    mapOf(
                        "nombres" to it.nombres,
                        "correo" to (it.correo ?: "Sin registrar"),
                        "telefono" to (it.telefono ?: "Sin registrar")
                    )
                }
                findViewById<ListView>(R.id.lvAlumnos).adapter = SimpleAdapter(
                    this@acrListaSeleccion, datos, R.layout.item_alumno,
                    arrayOf("nombres", "correo", "telefono"),
                    intArrayOf(R.id.txtNombre, R.id.txtCorreo, R.id.txtTelefono)
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@acrListaSeleccion, e)
            }
        }
    }

    private fun llenarMaterias(nivel: Int) {
        val actMaterias = findViewById<AutoCompleteTextView>(R.id.listaMaterias)
        consultaMaterias?.cancel()
        actMaterias.setText("", false)
        actMaterias.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, emptyList()))
        consultaMaterias = lifecycleScope.launch {
            try {
                val materias = SupabaseManager.client
                    .from("materias")
                    .select {
                        filter { eq("nivel", nivel) }
                        order("nombre", Order.ASCENDING)
                    }
                    .decodeList<Materia>()
                val nombres = materias.mapNotNull { it.nombre }
                if (nombres.isEmpty()) {
                    Toast.makeText(this@acrListaSeleccion, "No se encontraron materias para este semestre.", Toast.LENGTH_LONG).show()
                }
                actMaterias.setAdapter(ArrayAdapter(this@acrListaSeleccion, android.R.layout.simple_spinner_dropdown_item, nombres))
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@acrListaSeleccion, e)
            }
        }
    }
}
