package conversor;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;



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

private List<Coche> parsearXML(File archivo) throws Exception {
    List<Coche> coches = new ArrayList<>();
    DocumentBuilder constructor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document documento = constructor.parse(archivo);
    NodeList nodos = documento.getElementsByTagName("coche");
    for (int i = 0; i < nodos.getLength(); i++) {
        Element elemento = (Element) nodos.item(i);
        Coche coche = new Coche();
        coche.setMarca(obtenerValorElemento(elemento, "Marca"));
        coche.setModelo(obtenerValorElemento(elemento, "Modelo"));
        coche.setAño(Integer.parseInt(obtenerValorElemento(elemento, "Año")));
        coche.setColor(obtenerValorElemento(elemento, "Color"));
        coche.setPrecio(Double.parseDouble(obtenerValorElemento(elemento, "Precio")));
        coches.add(coche);
    }
    return coches;
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