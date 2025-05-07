package com.example.proyectofinaluem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventarioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> productosList;
    private Button btnUsarQR, btnUsarImagen, btnDeshacer, btnSalir;

    private DatabaseReference productosRef;
    private Producto productoSeleccionado;
    private int cantidadAnterior;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        productosRef = FirebaseDatabase.getInstance().getReference("producto");

        recyclerView = findViewById(R.id.recyclerProductos);
        btnUsarQR = findViewById(R.id.btnUsarQR);
        btnUsarImagen = findViewById(R.id.btnUsarImagen);
        btnDeshacer = findViewById(R.id.btnDeshacer);
        btnSalir = findViewById(R.id.btnSalir);

        productosList = new ArrayList<>();
        adapter = new ProductoAdapter(this, productosList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        cargarProductos();

        btnUsarQR.setOnClickListener(v -> escanearQR());
        btnUsarImagen.setOnClickListener(v -> tomarFoto());
        btnDeshacer.setOnClickListener(v -> deshacerCambio());
        btnSalir.setOnClickListener(v -> finish());
    }

    private void cargarProductos() {
        productosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productosList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Producto producto = snapshot.getValue(Producto.class);
                    productosList.add(producto);
                }
                adapter.notifyDataSetChanged();
                adapter.resaltarProducto(null); // limpia resaltado
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(InventarioActivity.this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void escanearQR() {
        Intent intent = new Intent(this, CaptureActivityPortrait.class);
        startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
    }

    private void tomarFoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // QR result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String idProducto = result.getContents();
            Producto producto = buscarProductoPorID(idProducto);
            if (producto != null) {
                productoSeleccionado = producto;
                cantidadAnterior = producto.getCantidad();
                producto.setCantidad(producto.getCantidad() + 1);
                actualizarProducto(producto);
                resaltarYScroll(producto.getNombre());
            } else {
                Toast.makeText(this, "Producto no encontrado con ese QR", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Imagen result
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap foto = (Bitmap) data.getExtras().get("data");

            try {
                ImageClassifier classifier = new ImageClassifier(this);
                String nombreDetectado = classifier.classify(foto);

                if (nombreDetectado != null) {
                    Toast.makeText(this, "Producto detectado: " + nombreDetectado, Toast.LENGTH_SHORT).show();
                    Producto producto = buscarProductoPorNombre(nombreDetectado);
                    if (producto != null) {
                        productoSeleccionado = producto;
                        cantidadAnterior = producto.getCantidad();
                        producto.setCantidad(producto.getCantidad() + 1);
                        actualizarProducto(producto);
                        resaltarYScroll(producto.getNombre());
                    } else {
                        Toast.makeText(this, "Producto no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No se pudo identificar el producto (confianza baja)", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar el modelo de IA", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resaltarYScroll(String nombreProducto) {
        adapter.resaltarProducto(nombreProducto);
        for (int i = 0; i < productosList.size(); i++) {
            if (productosList.get(i).getNombre().equalsIgnoreCase(nombreProducto)) {
                recyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Producto buscarProductoPorID(String id) {
        for (Producto p : productosList) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private Producto buscarProductoPorNombre(String nombre) {
        for (Producto p : productosList) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    private void actualizarProducto(Producto producto) {
        productosRef.child(producto.getId()).setValue(producto)
                .addOnSuccessListener(aVoid -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(InventarioActivity.this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(InventarioActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private void deshacerCambio() {
        if (productoSeleccionado != null) {
            productoSeleccionado.setCantidad(cantidadAnterior);
            actualizarProducto(productoSeleccionado);
            resaltarYScroll(productoSeleccionado.getNombre());
        }
    }
}
