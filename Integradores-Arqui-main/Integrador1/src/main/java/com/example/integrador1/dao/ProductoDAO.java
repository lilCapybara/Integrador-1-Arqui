package com.example.integrador1.dao;

import com.example.integrador1.entities.Cliente;
import com.example.integrador1.entities.Producto;

import java.sql.*;

public class ProductoDAO {
    private Connection conn;

    public ProductoDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertProducto (Producto producto) {
        String query = "INSERT INTO Producto (idProducto, nombre, valor) VALUES (?, ?, ?)";
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, producto.getIdProducto()); // idPersona
            ps.setString(2, producto.getNombre()); // nombre
            ps.setDouble(3, producto.getValor()); // edad
            ps.executeUpdate();
            System.out.println("Producto insertado exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    //Método obtenerProductoConMayorRecaudacion()
    //Se define una consulta que suma la recaudación total para cada producto.
    //La consulta combina las tablas Producto y Factura_Producto usando un JOIN basado en el campo idProducto.
    //Se agrupan los resultados por producto, ordenando las recaudaciones en orden descendente.
    //El LIMIT 1 asegura que se devuelva únicamente el producto con mayor recaudación.


    public String obtenerProductoConMayorRecaudacion() {
        String producto = null;
        String query = "SELECT Producto.nombre, SUM(Factura_Producto.cantidad * Producto.valor) AS recaudacion " +
                "FROM Producto " +
                "JOIN Factura_Producto ON Producto.idProducto = Factura_Producto.idProducto " +
                "GROUP BY Producto.idProducto " +
                "ORDER BY recaudacion DESC " +
                "LIMIT 1";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (((ResultSet) rs).next()) {
                producto = rs.getString("nombre");
                double recaudacion = rs.getDouble("recaudacion");

                System.out.println("El producto que más recaudó es: " + producto);
                System.out.println("Recaudación: " + recaudacion);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return producto;
    }
}
