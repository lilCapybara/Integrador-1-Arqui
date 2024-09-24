package com.example.integrador1.dao;

import  com.example.integrador1.entities.Cliente;

import java.sql.*;
import java.util.List;

public class ClienteDAO {
    private Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertCliente (Cliente cliente) {
        String query = "INSERT INTO Cliente (idCliente, nombre, email) VALUES (?, ?, ?)";
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, cliente.getIdCliente()); // idPersona
            ps.setString(2, cliente.getNombre()); // nombre
            ps.setString(3, cliente.getEmail()); // edad
            ps.executeUpdate();
            System.out.println("Cliente insertado exitosamente.");
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

    public boolean delete(Integer id) {
        String query = "DELETE FROM Cliente WHERE idCliente = ?";
        PreparedStatement ps = null;
        boolean isDeleted = false;

        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Cliente eliminado exitosamente.");
                isDeleted = true;
            } else {
                System.out.println("No se encontró ningun cliente con ese ID.");
            }
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

        return isDeleted;
    }


    public Cliente find(Integer pk) {
        String query = "SELECT p.nombre, p.email " +
                "FROM Cliente c " +
                "WHERE c.idCliente = ?";
        Cliente clienteById = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, pk); // Establecer el parámetro en la consulta SQL
            rs = ps.executeQuery();
            if (rs.next()) { // Verificar si hay resultados
                String nombre = rs.getString("nombre");
                String email = rs.getString("email");
                // Crear una nueva instancia de Persona con los datos recuperados de la consulta
                clienteById = new Cliente(pk, nombre, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return clienteById;
    }


    public void ClientesOrdenadosPorFacturacion() {

        String query = "SELECT Cliente.nombre, SUM(Factura_Producto.cantidad * Producto.valor) AS total_facturado " +
                "FROM Cliente " +
                "JOIN Factura ON Cliente.idCliente = Factura.idCliente " +
                "JOIN Factura_Producto ON Factura.idFactura = Factura_Producto.idFactura " +
                "JOIN Producto ON Factura_Producto.idProducto = Producto.idProducto " +
                "GROUP BY Cliente.idCliente " +
                "ORDER BY total_facturado DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Lista de clientes ordenada por el total facturado:");

            while (rs.next()) {
                String nombreCliente = rs.getString("nombre");
                double totalFacturado = rs.getDouble("total_facturado");

                System.out.println("Cliente: " + nombreCliente + " - Total facturado: " + totalFacturado);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

