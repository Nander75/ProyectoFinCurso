package com.example.proyectofinaluem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class Editar extends AppCompatActivity {

    private EditText etNombre, etMarca, etTalla, etCantidad, etPrecio;
    private ImageView ivImagenSeleccionada;
    private Button btnActualizar, btnEliminar, btnSalir, btnSeleccionarImagen;
    private ProgressBar progressBar;

    private DatabaseReference productosRef;
    private Producto productoActual;
    private Uri imagenUri;
    private StorageReference storageReference;

    private static final int IMAGE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        productosRef = FirebaseDatabase.getInstance().getReference("producto");
        storageReference = FirebaseStorage.getInstance().getReference("producto_images");

        // Enlazar vistas
        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etTalla = findViewById(R.id.etTalla);
        etCantidad = findViewById(R.id.etCantidad);
        etPrecio = findViewById(R.id.etPrecio);
        ivImagenSeleccionada = findViewById(R.id.ivImagenSeleccionada);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir = findViewById(R.id.btnSalir);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        progressBar = findViewById(R.id.progressBar);

        // Obtener producto desde intent
        productoActual = (Producto) getIntent().getSerializableExtra("producto");

        if (productoActual != null) {
            mostrarDatosProducto(productoActual);
        }

        // Botón seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());

        // Botón actualizar
        btnActualizar.setOnClickListener(v -> actualizarProducto());

        // Botón eliminar
        btnEliminar.setOnClickListener(v -> eliminarProducto());

        // Botón salir
        btnSalir.setOnClickListener(v -> finish());
    }

    private void mostrarDatosProducto(Producto producto) {
        etNombre.setText(producto.getNombre());
        etMarca.setText(producto.getMarca());
        etTalla.setText(producto.getTalla());
        etCantidad.setText(String.valueOf(producto.getCantidad()));
        etPrecio.setText(String.valueOf(producto.getPrecio()));

        if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
            Glide.with(this)
                    .load(producto.getImagen())
                    .placeholder(R.drawable.sega_logo)
                    .into(ivImagenSeleccionada);
        } else {
            ivImagenSeleccionada.setImageResource(R.drawable.sega_logo);
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData();
            if (imagenUri != null) {
                ivImagenSeleccionada.setImageURI(imagenUri);
            } else {
                Toast.makeText(Editar.this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
            }
        }
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

        if (imagenUri != null) {
            // Mostrar el ProgressBar mientras se sube la imagen
            progressBar.setVisibility(View.VISIBLE);

            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());
            fileReference.putFile(imagenUri)
                    .addOnProgressListener(taskSnapshot -> {
                        // Se puede actualizar el progreso si lo deseas
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressBar.setProgress((int) progress);
                    })
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        productoActual.setNombre(nombre);
                        productoActual.setMarca(marca);
                        productoActual.setTalla(talla);
                        productoActual.setCantidad(cantidad);
                        productoActual.setPrecio(precio);
                        productoActual.setImagen(uri.toString());

                        productosRef.child(productoActual.getId()).setValue(productoActual)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                });
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            productoActual.setNombre(nombre);
            productoActual.setMarca(marca);
            productoActual.setTalla(talla);
            productoActual.setCantidad(cantidad);
            productoActual.setPrecio(precio);

            productosRef.child(productoActual.getId()).setValue(productoActual)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void eliminarProducto() {
        productosRef.child(productoActual.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
