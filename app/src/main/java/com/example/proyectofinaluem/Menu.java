package com.example.proyectofinaluem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Menu extends AppCompatActivity {

    private Button btnRegistrar, btnVer, btnSalir, btnInventario;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVer = findViewById(R.id.btnVer);
        btnSalir = findViewById(R.id.btnSalir);
        btnInventario = findViewById(R.id.btnInventario); // Nuevo botón para abrir inventario

        // Listeners
        btnRegistrar.setOnClickListener(v ->
                startActivity(new Intent(Menu.this, Registrar.class))
        );

        btnVer.setOnClickListener(v ->
                startActivity(new Intent(Menu.this, Ver.class))
        );

        btnSalir.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Menu.this, MainActivity.class));
            finish();
        });

        // Listener para el nuevo botón "Inventario"
        btnInventario.setOnClickListener(v ->
                startActivity(new Intent(Menu.this, InventarioActivity.class))
        );
    }
}
