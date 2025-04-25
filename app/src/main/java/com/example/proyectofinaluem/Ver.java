package com.example.proyectofinaluem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class Ver extends AppCompatActivity {

    private RecyclerView recyclerProductos;
    private ProductoAdapter adapter;
    private ArrayList<Producto> listaProductos;
    private DatabaseReference productosRef;
    private Button btnSalir, btnEscanearQR, btnVerTodos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        // Inicializar vistas
        recyclerProductos = findViewById(R.id.recyclerProductos);
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(this, listaProductos);
        recyclerProductos.setAdapter(adapter);

        // Firebase reference
        productosRef = FirebaseDatabase.getInstance().getReference("producto");

        // Botones
        btnSalir = findViewById(R.id.btnSalir);
        btnEscanearQR = findViewById(R.id.btnEscanearQR);
        btnVerTodos = findViewById(R.id.btnVerTodos);

        // Botón Salir
        btnSalir.setOnClickListener(v -> {
            startActivity(new Intent(Ver.this, Menu.class));
            finish();
        });

        // Botón Escanear QR
        btnEscanearQR.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(Ver.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Escanea el código QR del producto");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.initiateScan();
        });

        // Botón Ver todos
        btnVerTodos.setOnClickListener(v -> cargarProductos());

        // Cargar todos los productos al inicio
        cargarProductos();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            } else {
                buscarProductoPorId(result.getContents());
            }
        }
    }

    private void buscarProductoPorId(String idEscaneado) {
        productosRef.child(idEscaneado).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Producto producto = snapshot.getValue(Producto.class);
                if (producto != null) {
                    listaProductos.clear();
                    listaProductos.add(producto);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(Ver.this, "Producto encontrado: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Ver.this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Ver.this, "Error al buscar producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProductos() {
        productosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaProductos.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Producto producto = data.getValue(Producto.class);
                    listaProductos.add(producto);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Ver.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
