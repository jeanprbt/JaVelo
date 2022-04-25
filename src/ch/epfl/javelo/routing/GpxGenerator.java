package ch.epfl.javelo.routing;


import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Classe représentant un générateur d'itinéraire au format GPX.
 *
 * @author Jean Perbet (341418)
 * @author Cassio Manuguerra (346232)
 */
public class GpxGenerator {

    private GpxGenerator(){}

    /**
     * Méthode retournant le document GPX correspondant à l'itinéraire et à son profil passés en argument.
     *
     * @param route l'itinéraire dont on veut écrire le GPX
     * @param profile le profil en long de l'itinéraire
     * @return le document GPX correspondant à l'itinéraire
     */
    public static Document createGpx(Route route, ElevationProfile profile){
        
        //Création de l'embryon de document constitué des éléments gpx, metadata et name
        Document doc = newDocument();
        
        //La balise gpx
        Element root = doc.createElementNS("http://www.topografix.com/GPX/1/1", "gpx");
        doc.appendChild(root);
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 " + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        //La balise metadata
        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        //La balise name
        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        //La balise rte
        Element rte = doc.createElement("rte");
        root.appendChild(rte);
        addStartToGpx(doc, rte, route, profile);
        double position = 0;

        for (Edge edge : route.edges()) {
            //Création de la balise rtept (route point)
            Element rtept = doc.createElement("rtept");
            rtept.setAttribute("lat", "" + Math.toDegrees(edge.toPoint().lat()));
            rtept.setAttribute("lon", "" + Math.toDegrees(edge.toPoint().lon()));
            rte.appendChild(rtept);

            //Création de la balise ele (elevation)
            position += edge.length() ;
            Element ele = doc.createElement("ele");
            ele.setTextContent("" + profile.elevationAt(position));
            rtept.appendChild(ele);
        }

        return doc ;
    }

    /**
     * Méthode permettant d'écrire le document GPX correspondant à l'itinéraire dans le fichier passés en arguments.
     *
     * @param path le chemin d'accès au fichier dans lequel on veut écrire le document GPX
     * @param route l'itinéraire dont on veut écrire le GPX dans le fichier fileName
     * @param profile le profil de l'itinéraire
     * @throws IOException si le fileName est invalide
     */
    public static void writeGpx(Path path, Route route, ElevationProfile profile) throws IOException {
        Document doc = createGpx(route, profile);
        Writer w = new FileWriter(path.toString());
        Transformer transformer;
        try {
            transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(w));
        } catch (TransformerException e){
            throw new Error(e); //Should never happen
        }
    }

    /**
     * Méthode privée permettant d'ajouter au document GPX le premier point de l'itinéraire.
     *
     * @param doc le document GPX auquel ajouter le point de départ
     * @param rte la balise dans laquelle ajouter le point de départ
     * @param route l'itinéraire dont on veut ajouter le point de départ
     * @param profile le profil de l'itinéraire dont on veut ajouter le point de départ
     */
    private static void addStartToGpx(Document doc, Element rte, Route route, ElevationProfile profile){
        Element firstRtept = doc.createElement("rtept");
        PointCh startPoint = route.points().get(0);
        firstRtept.setAttribute("lat", "" + Math.toDegrees(startPoint.lat()));
        firstRtept.setAttribute("lon", "" + Math.toDegrees(startPoint.lon()));
        rte.appendChild(firstRtept);
        Element ele = doc.createElement("ele");
        ele.setTextContent("" + route.elevationAt(0));
        firstRtept.appendChild(ele);
    }

    /**
     * Méthode privée permettant de créer un nouveau document vide.
     *
     * @return un document vide
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // Should never happen
        }
    }
}
