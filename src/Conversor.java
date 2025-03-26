package conversor;
import java.io.*;
import java.util.*;

public class Conversor {
    private String rutaCarpeta;
    private List<Coche> coches = new ArrayList<>();
    private String archivoSeleccionado;

    public static void main(String[] args) {
        new Conversor().ejecutar();
    }

    private void ejecutar() {
        Scanner lector = new Scanner(System.in);
        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenu();
            int opcion = obtenerEntero(lector, "Seleccione una opción: ");
            lector.nextLine(); 
            switch (opcion) {
                case 1: seleccionarCarpeta(lector); break;
                case 2: leerArchivo(lector); break;
                case 3: convertirArchivo(lector); break;
                case 4: ejecutando = false; break;
                default: System.out.println("Opción inválida.");
            }
        }
        lector.close();
    }

    private void mostrarMenu() {
        System.out.println("\n=== Menú Principal ===");
        System.out.println("1. Seleccionar carpeta");
        System.out.println("2. Leer archivo");
        System.out.println("3. Convertir a otro formato");
        System.out.println("4. Salir");
        System.out.println("--------------------");
        System.out.println("Carpeta actual: " + (rutaCarpeta != null ? rutaCarpeta : "No seleccionada"));
        System.out.println("Archivo seleccionado: " + (archivoSeleccionado != null ? archivoSeleccionado : "Ninguno"));
        listarContenidoCarpeta();
    }

    private List<Coche> parsearCSV(File archivo) throws IOException {
        List<Coche> coches = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                Coche coche = new Coche();
                coche.setMarca(datos[0]);
                coche.setModelo(datos[1]);
                coche.setAño(Integer.parseInt(datos[2]));
                coche.setColor(datos[3]);
                coche.setPrecio(Double.parseDouble(datos[4]));
                coches.add(coche);
            }
        }
        return coches;
    }