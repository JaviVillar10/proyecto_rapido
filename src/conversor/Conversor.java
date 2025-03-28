package conversor;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
                case 1:
                    seleccionarCarpeta(lector);
                    break;
                case 2:
                    leerArchivo(lector);
                    break;
                case 3:
                    convertirArchivo(lector);
                    break;
                case 4:
                    ejecutando = false;
                    break;
                default:
                    System.out.println("Opción inválida.");
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

private void seleccionarCarpeta(Scanner lector) {
    System.out.print("Ingrese la ruta de la carpeta: ");
    String ruta = lector.nextLine();
    File carpeta = new File(ruta);
    if (carpeta.isDirectory()) {
        rutaCarpeta = ruta;
        System.out.println("Carpeta seleccionada: " + ruta);
    } else {
        System.out.println("¡Error! La ruta no es válida.");
    }
}

private void listarContenidoCarpeta() {
    if (rutaCarpeta == null) return;
    File carpeta = new File(rutaCarpeta);
    File[] archivos = carpeta.listFiles((dir, nombre) -> 
        nombre.toLowerCase().matches(".*\\.(csv|json|xml)"));
    System.out.println("\nArchivos disponibles:");
    if (archivos != null && archivos.length > 0) {
        for (File archivo : archivos) {
            System.out.println("- " + archivo.getName());
        }
    } else {
        System.out.println("No hay archivos compatibles.");
    }
}
  
    private void leerArchivo(Scanner lector) {
        if (rutaCarpeta == null) {
            System.out.println("Primero seleccione una carpeta.");
            return;
        }
        System.out.print("Nombre del archivo a leer: ");
        String nombreArchivo = lector.nextLine();
        File archivo = new File(rutaCarpeta + File.separator + nombreArchivo);

        if (!archivo.exists()) {
            System.out.println("¡Error! El archivo no existe.");
            return;
        }

        coches.clear();
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        try {
            switch (extension) {
                case "csv":
                    coches = parsearCSV(archivo);
                    break;
                case "json":
                    coches = parsearJSON(archivo);
                    break;
                case "xml":
                    coches = parsearXML(archivo);
                    break;
                default:
                    System.out.println("Formato no soportado.");
                    return;
            }
            archivoSeleccionado = nombreArchivo;
            System.out.println("Archivo leído correctamente. Coches cargados: " + coches.size());
        } catch (Exception e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
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

    private void convertirArchivo(Scanner lector) {
        if (coches.isEmpty()) {
            System.out.println("No hay datos para convertir.");
            return;
        }
        System.out.println("Formatos disponibles:");
        System.out.println("1. CSV\n2. JSON\n3. XML");
        int formato = obtenerEntero(lector, "Seleccione formato de salida: ");
        lector.nextLine(); // Limpiar buffer
        
        System.out.print("Nombre del archivo de salida: ");
        String nombreArchivo = lector.nextLine();
        File archivoSalida = new File(rutaCarpeta + File.separator + nombreArchivo);
        
        try {
            switch (formato) {
                case 1: archivoSalida = asegurarExtension(archivoSalida, "csv");
                        escribirCSV(archivoSalida); break;
                case 2: archivoSalida = asegurarExtension(archivoSalida, "json");
                        escribirJSON(archivoSalida); break;
                case 3: archivoSalida = asegurarExtension(archivoSalida, "xml");
                        escribirXML(archivoSalida); break;
                default: System.out.println("Opción inválida."); return;
            }
            System.out.println("Archivo guardado en: " + archivoSalida.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error durante la conversión: " + e.getMessage());
        }
    }