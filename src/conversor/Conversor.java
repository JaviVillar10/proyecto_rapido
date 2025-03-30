/**
 * @author Adrián Herrera
 * @author Javier Villar
 */
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
import com.google.gson.reflect.TypeToken;

public class Conversor {
    private String rutaCarpeta;
    private List<Map<String, Object>> datos = new ArrayList<>();
    private String archivoSeleccionado;
    private List<String> cabeceras = new ArrayList<>();

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

    private int obtenerEntero(Scanner lector, String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return lector.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("¡Error! Ingrese un número válido.");
                lector.next();
            }
        }
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
        if (rutaCarpeta == null)
            return;
        File carpeta = new File(rutaCarpeta);
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.toLowerCase().matches(".*\\.(csv|json|xml)"));
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

        datos.clear();
        cabeceras.clear();
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        try {
            switch (extension) {
                case "csv":
                    datos = parsearCSV(archivo);
                    break;
                case "json":
                    datos = parsearJSON(archivo);
                    break;
                case "xml":
                    datos = parsearXML(archivo);
                    break;
                default:
                    System.out.println("Formato no soportado.");
                    return;
            }
            archivoSeleccionado = nombreArchivo;
            System.out.println("Archivo leído correctamente. Registros cargados: " + datos.size());
        } catch (Exception e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parsearCSV(File archivo) throws IOException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String lineaCabecera = br.readLine();
            if (lineaCabecera == null) {
                throw new IOException("El archivo CSV está vacío");
            }

            String[] headers = lineaCabecera.split(",");
            cabeceras.addAll(Arrays.asList(headers));

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(",");
                Map<String, Object> registro = new HashMap<>();

                for (int i = 0; i < headers.length && i < valores.length; i++) {
                    registro.put(headers[i], valores[i]);
                }
                resultado.add(registro);
            }
        }
        return resultado;
    }

    private List<Map<String, Object>> parsearJSON(File archivo) throws IOException {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(archivo)) {
            List<Map<String, Object>> resultado = gson.fromJson(reader,
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());

            if (!resultado.isEmpty()) {
                cabeceras.addAll(resultado.get(0).keySet());
            }

            return resultado;
        }
    }

    private List<Map<String, Object>> parsearXML(File archivo) throws Exception {
        List<Map<String, Object>> resultado = new ArrayList<>();
        DocumentBuilder constructor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document documento = constructor.parse(archivo);

        Element raiz = documento.getDocumentElement();
        NodeList nodos = raiz.getChildNodes();

        Set<String> todasCabeceras = new HashSet<>();
        for (int i = 0; i < nodos.getLength(); i++) {
            if (nodos.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elemento = (Element) nodos.item(i);
                NodeList propiedades = elemento.getChildNodes();

                for (int j = 0; j < propiedades.getLength(); j++) {
                    if (propiedades.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        todasCabeceras.add(propiedades.item(j).getNodeName());
                    }
                }
            }
        }
        cabeceras.addAll(todasCabeceras);

        for (int i = 0; i < nodos.getLength(); i++) {
            if (nodos.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elemento = (Element) nodos.item(i);
                Map<String, Object> registro = new HashMap<>();

                for (String cabecera : cabeceras) {
                    NodeList elementos = elemento.getElementsByTagName(cabecera);
                    if (elementos.getLength() > 0) {
                        registro.put(cabecera, elementos.item(0).getTextContent());
                    }
                }

                if (!registro.isEmpty()) {
                    resultado.add(registro);
                }
            }
        }

        return resultado;
    }

    private String obtenerValorElemento(Element padre, String nombreEtiqueta) {
        return padre.getElementsByTagName(nombreEtiqueta).item(0).getTextContent();
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
                case 1:
                    archivoSalida = asegurarExtension(archivoSalida, "csv");
                    escribirCSV(archivoSalida);
                    break;
                case 2:
                    archivoSalida = asegurarExtension(archivoSalida, "json");
                    escribirJSON(archivoSalida);
                    break;
                case 3:
                    archivoSalida = asegurarExtension(archivoSalida, "xml");
                    escribirXML(archivoSalida);
                    break;
                default:
                    System.out.println("Opción inválida.");
                    return;
            }
            System.out.println("Archivo guardado en: " + archivoSalida.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error durante la conversión: " + e.getMessage());
        }
    }

    private void escribirXML(File archivoSalida) throws Exception {
        Document documento = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element raiz = documento.createElement("coches");
        documento.appendChild(raiz);

        for (Coche coche : coches) {
            Element elementoCoche = documento.createElement("coche");
            crearElemento(documento, elementoCoche, "Marca", coche.getMarca());
            crearElemento(documento, elementoCoche, "Modelo", coche.getModelo());
            crearElemento(documento, elementoCoche, "Año", String.valueOf(coche.getAño()));
            crearElemento(documento, elementoCoche, "Color", coche.getColor());
            crearElemento(documento, elementoCoche, "Precio", String.format("%.2f", coche.getPrecio()));
            raiz.appendChild(elementoCoche);
        }

        Transformer transformador = TransformerFactory.newInstance().newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformador.transform(new DOMSource(documento), new StreamResult(archivoSalida));
    }

    private void crearElemento(Document documento, Element padre, String nombre, String valor) {
        Element elemento = documento.createElement(nombre);
        elemento.appendChild(documento.createTextNode(valor));
        padre.appendChild(elemento);
    }

    private File asegurarExtension(File archivo, String extension) {
        if (!archivo.getName().toLowerCase().endsWith("." + extension)) {
            return new File(archivo.getAbsolutePath() + "." + extension);
        }
        return archivo;
    }

    private void escribirCSV(File archivoSalida) throws IOException {
        try (PrintWriter escritor = new PrintWriter(archivoSalida)) {
            escritor.println("Marca,Modelo,Año,Color,Precio");
            for (Coche coche : coches) {
                escritor.printf("%s,%s,%d,%s,%.2f%n",
                        coche.getMarca(), coche.getModelo(), coche.getAño(), coche.getColor(), coche.getPrecio());
            }
        }
    }

    }

    private void escribirJSON(File archivoSalida) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter escritor = new FileWriter(archivoSalida)) {
            gson.toJson(coches, escritor);
        }
    }
