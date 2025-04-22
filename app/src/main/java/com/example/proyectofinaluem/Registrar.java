package com.example.proyectofinaluem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registrar extends AppCompatActivity {

    private EditText etNombre, etMarca, etTalla, etCantidad, etPrecio;
    private ImageView ivImagenSeleccionada;
    private Button btnSeleccionarImagen, btnConfirmar, btnVolver;

    private DatabaseReference productosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Referencia a la base de datos
        productosRef = FirebaseDatabase.getInstance().getReference("producto");

        // Enlazar vistas
        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etTalla = findViewById(R.id.etTalla);
        etCantidad = findViewById(R.id.etCantidad);
        etPrecio = findViewById(R.id.etPrecio);
        ivImagenSeleccionada = findViewById(R.id.ivImagenSeleccionada);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnVolver = findViewById(R.id.btnVolver);

        // Listener para volver
        btnVolver.setOnClickListener(v -> finish());

        // Por ahora no seleccionamos imagen, pero puedes lanzar un intent aquí más adelante

        // Confirmar inserción
        btnConfirmar.setOnClickListener(v -> registrarProducto());
    }

    private void registrarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String talla = etTalla.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(marca) || TextUtils.isEmpty(talla)
                || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad o precio inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear ID automático
        String id = productosRef.push().getKey();

        // Crear objeto producto
        Producto producto = new Producto(id, nombre, marca, talla, cantidad, precio, "");

        // Subir a Firebase
        if (id != null) {
            productosRef.child(id).setValue(producto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Producto registrado", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etMarca.setText("");
        etTalla.setText("");
        etCantidad.setText("");
        etPrecio.setText("");
        ivImagenSeleccionada.setImageResource(0);
    }
}
