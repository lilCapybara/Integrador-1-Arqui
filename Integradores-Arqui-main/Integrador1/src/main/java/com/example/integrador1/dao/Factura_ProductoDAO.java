package com.example.integrador1.dao;



import com.example.integrador1.entities.Factura_Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Factura_ProductoDAO {
    private Connection conn;

    public Factura_ProductoDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertFactura_Producto(Factura_Producto factura_Producto) {
        String query = "INSERT INTO Factura_Producto (idFactura, idProducto, cantidad) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, factura_Producto.getIdFactura()); // idPersona
            ps.setInt(2, factura_Producto.getIdProducto()); // nombre
            ps.setInt(3, factura_Producto.getCantidad()); // edad
            ps.executeUpdate();
            System.out.println("Factura_Producto insertado exitosamente.");
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
}