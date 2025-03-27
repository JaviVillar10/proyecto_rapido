package conversor;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

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

