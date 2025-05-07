package com.example.proyectofinaluem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import android.widget.ProgressBar;
import android.widget.Toast;

public class Registrar extends AppCompatActivity {

    private EditText etNombre, etMarca, etTalla, etCantidad, etPrecio;
    private ImageView ivImagenSeleccionada;
    private Button btnSeleccionarImagen, btnConfirmar, btnVolver;
    private ProgressBar progressBar;  // Declaramos el ProgressBar

    private DatabaseReference productosRef;
    private Uri imagenUriSeleccionada;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Inicializamos el ProgressBar
        progressBar = findViewById(R.id.progressBar);

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

        // Volver
        btnVolver.setOnClickListener(v -> finish());

        // Selección de imagen
        btnSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Confirmar inserción
        btnConfirmar.setOnClickListener(v -> registrarProducto());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imagenUriSeleccionada = data.getData();
            Glide.with(this).load(imagenUriSeleccionada).into(ivImagenSeleccionada);
        }
    }

    private void registrarProducto() {
        String nombre = etNombre.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String talla = etTalla.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

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

        String id = productosRef.push().getKey();

        if (imagenUriSeleccionada != null) {
            // Mostrar el ProgressBar mientras se sube la imagen
            progressBar.setVisibility(View.VISIBLE);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("imagenes_productos");
            StorageReference fileRef = storageRef.child(UUID.randomUUID().toString());

            fileRef.putFile(imagenUriSeleccionada)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        guardarProducto(id, nombre, marca, talla, cantidad, precio, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        // Ocultar el ProgressBar si hay un error
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            guardarProducto(id, nombre, marca, talla, cantidad, precio, "");
        }
    }

    private void guardarProducto(String id, String nombre, String marca, String talla, int cantidad, double precio, String imagenUrl) {
        Producto producto = new Producto(id, nombre, marca, talla, cantidad, precio, imagenUrl);
        productosRef.child(id).setValue(producto)
                .addOnSuccessListener(aVoid -> {
                    // Ocultar el ProgressBar una vez que se haya guardado el producto
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Producto registrado", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    // Ocultar el ProgressBar si hay un error
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etMarca.setText("");
        etTalla.setText("");
        etCantidad.setText("");
        etPrecio.setText("");
        ivImagenSeleccionada.setImageResource(0);
        imagenUriSeleccionada = null;
    }
}
