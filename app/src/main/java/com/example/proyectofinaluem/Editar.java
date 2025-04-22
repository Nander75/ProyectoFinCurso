package com.example.proyectofinaluem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Editar extends AppCompatActivity {

    private EditText etNombre, etMarca, etTalla, etCantidad, etPrecio;
    private ImageView ivImagenSeleccionada;
    private Button btnActualizar, btnSalir;

    private DatabaseReference productosRef;
    private Producto productoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        productosRef = FirebaseDatabase.getInstance().getReference("producto");

        // Enlazar vistas
        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etTalla = findViewById(R.id.etTalla);
        etCantidad = findViewById(R.id.etCantidad);
        etPrecio = findViewById(R.id.etPrecio);
        ivImagenSeleccionada = findViewById(R.id.ivImagenSeleccionada);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnSalir = findViewById(R.id.btnSalir);

        // Obtener producto desde intent
        productoActual = (Producto) getIntent().getSerializableExtra("producto");

        if (productoActual != null) {
            mostrarDatosProducto(productoActual);
        }

        // Botón actualizar
        btnActualizar.setOnClickListener(v -> actualizarProducto());

        // Botón volver sin guardar
        btnSalir.setOnClickListener(v -> finish());
    }

    private void mostrarDatosProducto(Producto producto) {
        etNombre.setText(producto.getNombre());
        etMarca.setText(producto.getMarca());
        etTalla.setText(producto.getTalla());
        etCantidad.setText(String.valueOf(producto.getCantidad()));
        etPrecio.setText(String.valueOf(producto.getPrecio()));
        // Si tienes una imagen real, aquí podrías cargarla usando Glide o Picasso
    }

    private void actualizarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String talla = etTalla.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(marca) || TextUtils.isEmpty(talla)
                || TextUtils.isEmpty(cantidadStr) || TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
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

        // Actualizar objeto
        productoActual.setNombre(nombre);
        productoActual.setMarca(marca);
        productoActual.setTalla(talla);
        productoActual.setCantidad(cantidad);
        productoActual.setPrecio(precio);

        // Subir cambios a Firebase
        productosRef.child(productoActual.getId()).setValue(productoActual)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    finish(); // Vuelve al listado
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
