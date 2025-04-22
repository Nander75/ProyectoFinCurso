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

import java.util.ArrayList;

public class Ver extends AppCompatActivity {

    private RecyclerView recyclerProductos;
    private ProductoAdapter adapter;
    private ArrayList<Producto> listaProductos;
    private DatabaseReference productosRef;
    private Button btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        // Inicializar componentes de la interfaz
        recyclerProductos = findViewById(R.id.recyclerProductos);
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(this, listaProductos);
        recyclerProductos.setAdapter(adapter);

        // Obtener referencia a la base de datos
        productosRef = FirebaseDatabase.getInstance().getReference("producto");

        // Cargar los productos
        cargarProductos();

        // Configurar el botón "Salir"
        btnSalir = findViewById(R.id.btnSalir); // Obtener el botón
        btnSalir.setOnClickListener(v -> {
            // Redirigir al menú cuando el botón sea presionado
            startActivity(new Intent(Ver.this, Menu.class));
            finish(); // Terminar la actividad actual
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
