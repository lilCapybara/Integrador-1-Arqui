package com.example.integrador1.utils;


import com.example.integrador1.dao.ClienteDAO;
import com.example.integrador1.dao.FacturaDAO;
import com.example.integrador1.dao.Factura_ProductoDAO;
import com.example.integrador1.dao.ProductoDAO;
import com.example.integrador1.entities.Cliente;
import com.example.integrador1.entities.Factura;
import com.example.integrador1.entities.Factura_Producto;
import com.example.integrador1.entities.Producto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//Se encarga de poblar la DB a partir de los CSV, ademas de crear o eliminar las tablas
public class HelperMySQL {
    private Connection conn = null;

    public HelperMySQL() {  //Constructor que establece conexion con DB
        String driver = "com.mysql.cj.jdbc.Driver";
        String uri = "jdbc:mysql://localhost:3306/integrador1";

        try {
            Class.forName(driver).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            conn = DriverManager.getConnection(uri, "root", "");
            conn.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (conn != null){
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //Permite reiniciar la DB en un contexto de pruebas
    public void dropTables() throws SQLException {
        String dropFactura_Producto = "DROP TABLE IF EXISTS Factura_Producto";
        this.conn.prepareStatement(dropFactura_Producto).execute();
        this.conn.commit();
        //Elimino Factura_Producto primero ya que depende de Factura y Producto

        String dropFactura = "DROP TABLE IF EXISTS Factura";
        this.conn.prepareStatement(dropFactura).execute();
        this.conn.commit();
        //Elimino Factura ahora, ya que depende de CLiente

        String dropCliente = "DROP TABLE IF EXISTS Cliente";
        this.conn.prepareStatement(dropCliente).execute();
        this.conn.commit();

        String dropProducto = "DROP TABLE IF EXISTS Producto";
        this.conn.prepareStatement(dropProducto).execute();
        this.conn.commit();
        //Cliente y Producto son tablas independientes, por eso se eliminan a lo utlimo

    }

    public void createTables() throws SQLException {
        String tableCliente = "CREATE TABLE IF NOT EXISTS Cliente(" +
                "idCliente INT NOT NULL, " +
                "nombre VARCHAR(500), " +
                "email VARCHAR(150), " +
                "CONSTRAINT Cliente_pk PRIMARY KEY (idCliente));" ;
        this.conn.prepareStatement(tableCliente).execute();
        this.conn.commit();

        String tableFactura = "CREATE TABLE IF NOT EXISTS Factura(" +
                "idFactura INT NOT NULL, " +
                "idCliente INT, " +
                "CONSTRAINT Factura_pk PRIMARY KEY (idFactura),  " +
                "CONSTRAINT FK_idCliente FOREIGN KEY (idCliente) REFERENCES Cliente (idCliente))";
        this.conn.prepareStatement(tableFactura).execute();
        this.conn.commit();

        String tableProducto = "CREATE TABLE IF NOT EXISTS Producto(" +
                "idProducto INT NOT NULL, " +
                "nombre  VARCHAR(45), " +
                "valor FLOAT , " +
                "CONSTRAINT Producto_pk PRIMARY KEY (idProducto));" ;
        this.conn.prepareStatement(tableProducto).execute();
        this.conn.commit();

        String tableFactura_Producto = "CREATE TABLE IF NOT EXISTS Factura_Producto(" +
                "idFactura INT NOT NULL, " +
                "idProducto INT NOT NULL, " +
                "cantidad INT , " +
                "CONSTRAINT FK_idProducto_f FOREIGN KEY (idProducto) REFERENCES Producto (idProducto),  " +
                "CONSTRAINT FK_idFactura_p FOREIGN KEY (idFactura) REFERENCES Factura (idFactura))";
        this.conn.prepareStatement(tableFactura_Producto).execute();
        this.conn.commit();
    }

    //Lee un archivo CSV y devuelve su contenido en forma de un iterable de registros Iterable<CSVRecord>
    private Iterable<CSVRecord> getData(String archivo) throws IOException {
        String path = "src\\main\\resources\\" + archivo;
        Reader in = new FileReader(path);
        String[] header = {};  // Puedes configurar tu encabezado personalizado aquí si es necesario
        CSVParser csvParser = CSVFormat.EXCEL.withHeader(header).parse(in);
        Iterable<CSVRecord> records = csvParser.getRecords();
        return records;
    }

    //Inserta datos en las tablas de las DB a partir de los archivos CSV
    public void populateDB() throws Exception {
        System.out.println("Populating DB...");

        for(CSVRecord row : getData("clientes.csv")) {
            if(row.size() >= 3) { // Verificar que hay al menos 3 campos en el CSVRecord
                String idString = row.get(0);
                if(!idString.isEmpty()) {
                    try {
                        int id = Integer.parseInt(idString);
                        String nombre = row.get(1);
                        String email = row.get(2);
                        Cliente cliente = new Cliente(id, nombre, email);
                        ClienteDAO c = new ClienteDAO(conn);
                        c.insertCliente(cliente);
                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato en datos de cliente: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Clientes insertados");

        for(CSVRecord row : getData("productos.csv")) {
            if(row.size() >= 3) { // Verificar que hay al menos 3 campos en el CSVRecord
                String idString = row.get(0);
                if(!idString.isEmpty()) {
                    try {
                        int id = Integer.parseInt(idString);
                        String nombre = row.get(1);
                        float valor = Float.parseFloat(row.get(2));
                        Producto producto = new Producto(id, nombre, valor);
                        ProductoDAO p = new ProductoDAO(conn);
                        p.insertProducto(producto);
                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato en datos de cliente: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Productos insertados");

        for(CSVRecord row : getData("facturas.csv")) {
            if(row.size() >= 2) { // Verificar que hay al menos 3 campos en el CSVRecord
                String idString = row.get(0);
                if(!idString.isEmpty()) {
                    try {
                        int idFactura = Integer.parseInt(idString);
                        int idCliente = Integer.parseInt(row.get(1));
                        Factura factura = new Factura(idFactura, idCliente);
                        FacturaDAO f = new FacturaDAO(conn);
                        f.insertFactura(factura);
                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato en datos de cliente: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Facturas insertadas");


        for(CSVRecord row : getData("facturas-productos.csv")) {
            if(row.size() >= 3) { // Verificar que hay al menos 3 campos en el CSVRecord
                String idString = row.get(0);
                if(!idString.isEmpty()) {
                    try {
                        int idFactura = Integer.parseInt(idString);
                        int idProducto = Integer.parseInt(row.get(1));
                        int cantidad = Integer.parseInt(row.get(2));
                        Factura_Producto factura_producto = new Factura_Producto(idFactura, idProducto,cantidad);
                        Factura_ProductoDAO f = new Factura_ProductoDAO(conn);
                        f.insertFactura_Producto(factura_producto);
                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato en datos de cliente: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Factura_Producto insertadas");


    }

}
