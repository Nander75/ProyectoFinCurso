package com.example.proyectofinaluem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private final Context context;
    private final List<Producto> listaProductos;
    private int productoSeleccionado = -1; // Índice del producto a resaltar

    public ProductoAdapter(Context context, List<Producto> listaProductos) {
        this.context = context;
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.tvNombre.setText(producto.getNombre());
        holder.tvCantidad.setText("Cantidad: " + producto.getCantidad());
        holder.tvPrecio.setText("Precio: " + producto.getPrecio() + "€");

        // Cargar imagen usando Glide
        if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
            Glide.with(context)
                    .load(producto.getImagen())
                    .placeholder(R.drawable.sega_logo)
                    .into(holder.ivProducto);
        } else {
            holder.ivProducto.setImageResource(R.drawable.sega_logo);
        }

        // Resaltar si es el producto seleccionado
        if (position == productoSeleccionado) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        // Abrir la actividad Editar con los datos del producto
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Editar.class);
            intent.putExtra("producto", producto);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    // Método para resaltar un producto por nombre
    public void resaltarProducto(String nombre) {
        for (int i = 0; i < listaProductos.size(); i++) {
            if (listaProductos.get(i).getNombre().equalsIgnoreCase(nombre)) {
                productoSeleccionado = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvCantidad, tvPrecio;
        ImageView ivProducto;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            ivProducto = itemView.findViewById(R.id.ivProducto);
        }
    }
}
