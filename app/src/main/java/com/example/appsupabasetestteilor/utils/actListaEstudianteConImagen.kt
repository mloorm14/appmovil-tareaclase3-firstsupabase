package com.example.appsupabasetestteilor.utils

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appsupabasetestteilor.R
import com.example.appsupabasetestteilor.data.SupabaseManager
import com.example.appsupabasetestteilor.models.Alumno
import com.example.appsupabasetestteilor.models.Materia
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class actListaEstudianteConImagen : AppCompatActivity() {
    private var consultaMaterias: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act_lista_estudiante_con_imagen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialAutoCompleteTextView>(R.id.listaNiveles)
            .setOnItemClickListener { _, _, position, _ -> cargarMaterias(position + 1) }

        val lista = findViewById<ListView>(R.id.lvAlumnos)
        lista.addHeaderView(layoutInflater.inflate(R.layout.ly_header, lista, false), null, false)
        lista.setOnItemClickListener { parent, _, position, _ ->
            val alumno = parent.getItemAtPosition(position) as? Map<*, *>
                ?: return@setOnItemClickListener
            Toast.makeText(this, "Item seleccionado: ${alumno["nombres"]}", Toast.LENGTH_SHORT).show()
        }
        cargarAlumnos()
    }

    private fun cargarMaterias(nivel: Int) {
        consultaMaterias?.cancel()
        val selector = findViewById<MaterialAutoCompleteTextView>(R.id.listaMaterias)
        selector.setText("", false)
        selector.setSimpleItems(emptyArray<String>())
        consultaMaterias = lifecycleScope.launch {
            try {
                val materias = SupabaseManager.client.from("materias")
                    .select {
                        filter { eq("nivel", nivel) }
                        order("nombre", Order.ASCENDING)
                    }.decodeList<Materia>()
                selector.setSimpleItems(materias.mapNotNull { it.nombre }.toTypedArray())
                if (materias.isEmpty()) {
                    Toast.makeText(this@actListaEstudianteConImagen, "No hay materias para este semestre", Toast.LENGTH_SHORT).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@actListaEstudianteConImagen, e)
            }
        }
    }

    private fun cargarAlumnos() {
        lifecycleScope.launch {
            try {
                val alumnos = SupabaseManager.client.from("alumnos")
                    .select { order("nombres", Order.ASCENDING) }
                    .decodeList<Alumno>()
                val datos = alumnos.map {
                    mapOf("id" to it.id, "nombres" to it.nombres,
                        "correo" to it.correo.orEmpty(), "telefono" to it.telefono.orEmpty(),
                        "foto" to it.foto.orEmpty())
                }
                val adapter = SimpleAdapter(
                    this@actListaEstudianteConImagen, datos, R.layout.item_alumno,
                    arrayOf("nombres", "correo", "telefono", "foto"),
                    intArrayOf(R.id.txtNombre, R.id.txtCorreo, R.id.txtTelefono, R.id.imgAlumno)
                )
                adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                    if (view.id == R.id.imgAlumno) {
                        Glide.with(view)
                            .load((data as? String)?.takeIf { it.isNotBlank() })
                            .placeholder(R.drawable.ic_alumno)
                            .error(R.drawable.ic_alumno)
                            .into(view as ImageView)
                        true
                    } else {
                        false
                    }
                }
                findViewById<ListView>(R.id.lvAlumnos).adapter = adapter
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@actListaEstudianteConImagen, e)
            }
        }
    }
}
