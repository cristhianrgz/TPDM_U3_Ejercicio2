package mx.edu.tpdm_u3_ejercicio2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var platillo : EditText ?= null
    var cliente : EditText ?= null
    var telefono : EditText ?= null
    var fechaEntrega : EditText ?= null
    var horaEntrega : EditText ?= null
    var precio : EditText ?= null
    var insertar : Button ?= null
    var listaView : ListView?= null
    var actualizar : Button ?= null
    //declarando el objeto firestore
    var baseRemota = FirebaseFirestore.getInstance()

    //declarar objetos tipo arreglo dinamico
    var registrosRemotos = ArrayList<String>()
    var keys = java.util.ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        platillo = findViewById(R.id.editPlatillo)
        cliente = findViewById(R.id.editCliente)
        telefono = findViewById(R.id.editTelefono)
        fechaEntrega = findViewById(R.id.editfechaEntrega)
        horaEntrega = findViewById(R.id.edithoraEntrega)
        precio = findViewById(R.id.editprecio)
        insertar = findViewById(R.id.btnInsertar)
        listaView = findViewById(R.id.listaRegitrados)
        actualizar = findViewById(R.id.btnActualizar)

        insertar?.setOnClickListener {
            var datosInsertar = hashMapOf(
                "platilo" to platillo?.text.toString(),
                "cliente" to cliente?.text.toString(),
                "telefono" to telefono?.text.toString(),
                "fechaEntrega" to fechaEntrega?.text.toString(),
                "horaEntrega" to horaEntrega?.text.toString(),
                "precio" to precio?.text.toString().toDouble()
            )

            baseRemota.collection("pedidos")
                .add(datosInsertar as Map<String, Any>).addOnSuccessListener {
                Toast.makeText(this, "Se inserto correctamente", Toast.LENGTH_LONG).show()
            }
                .addOnFailureListener{
                    Toast.makeText(this, "No se pudo insertar"+it.message, Toast.LENGTH_LONG).show()
                }
            limpiarCampos()
        }
        baseRemota.collection("pedidos").addSnapshotListener { querySnapshot, e ->
            if(e !=null){
                Toast.makeText(this,"EROR, no se pudo hacer la consulta", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            registrosRemotos.clear()
            keys.clear()

            for (document in querySnapshot!!){
                var cadena = document.getString("platilo")+" -- "+ document.getString("cliente")+" -- "+document.getString("telefono")+" -- "+
                        document.getString("fechaEntrega")+" -- "+document.getString("horaEntrega")+" -- "+
                        document.getDouble("precio")+"\n"
                registrosRemotos.add(cadena)
                keys.add(document.id)
            }

            if(registrosRemotos.size == 0){
                registrosRemotos.add("NO HAY REGISTROS GUARDADOS")
            }

            var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, registrosRemotos)
            listaView?.adapter = adapter
        }
        listaView?.setOnItemClickListener{ adapterView, view, i, l ->
            if(keys.size == 0){
                return@setOnItemClickListener
            }

            AlertDialog.Builder(this).setTitle("ATENCION")
                .setMessage("¿Que deseas hacer con "+registrosRemotos.get(i)+" ?")
                .setPositiveButton("Eliminar"){dialog, which ->
                    baseRemota.collection("pedidos").document(keys.get(i)).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this,"Se borro correctamente el registro", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText(this,"No se pudo borrar el registro", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Actualizar"){dialog, which ->
                    baseRemota.collection("pedidos").document(keys.get(i)).get()

                        .addOnSuccessListener {
                            platillo?.setText(it.getString("platilo"))
                            cliente?.setText(it.getString("cliente"))
                            telefono?.setText(it.getString("telefono"))
                            fechaEntrega?.setText(it.getString("fechaEntrega"))
                            horaEntrega?.setText(it.getString("horaEntrega"))
                            precio?.setText(it.getDouble("precio").toString())
                        }
                        .addOnFailureListener{
                            platillo?.setText("NULL")
                            cliente?.setText("NULL")
                            telefono?.setText("NO SE ENCONTRO DATO")
                            fechaEntrega?.setText("NO SE ENCONTRO DATO")
                            horaEntrega?.setText("NO SE ENCONTRO DATO")
                            precio?.setText("NO SE ENCONTRO DATO")

                            platillo?.isEnabled=false
                            cliente?.isEnabled = false
                            telefono?.isEnabled = false
                            fechaEntrega?.isEnabled = false
                            horaEntrega?.isEnabled = false
                            precio?.isEnabled = false
                            actualizar?.isEnabled = false
                        }

                    actualizar?.setOnClickListener{
                        var datosActualizar = hashMapOf(
                            "platilo" to platillo?.text.toString(),
                            "cliente" to cliente?.text.toString(),
                            "telefono" to telefono?.text.toString(),
                            "fechaEntrega" to fechaEntrega?.text.toString(),
                            "horaEntrega" to horaEntrega?.text.toString(),
                            "precio" to precio?.text.toString().toDouble()
                        )
                        baseRemota.collection("pedidos").document(keys.get(i)).set(datosActualizar as Map<String, Any>)
                            .addOnSuccessListener {
                                limpiarCampos()
                                Toast.makeText(this,"Se actualizo correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener(){
                                Toast.makeText(this,"No se pudo actualizar", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNeutralButton("Cancelar"){dialog, which -> }.show()
        }
    }

    fun limpiarCampos(){
        platillo?.setText("")
        cliente?.setText("")
        telefono?.setText("")
        fechaEntrega?.setText("")
        horaEntrega?.setText("")
        precio?.setText("")
    }

}
