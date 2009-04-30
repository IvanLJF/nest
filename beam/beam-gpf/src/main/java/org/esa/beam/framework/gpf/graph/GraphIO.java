package org.esa.beam.framework.gpf.graph;

import com.bc.ceres.binding.dom.DomElementXStreamConverter;
import com.bc.ceres.binding.dom.DomElement;
import com.bc.ceres.util.TemplateReader;
import com.thoughtworks.xstream.XStream;
import org.esa.beam.framework.gpf.internal.ApplicationData;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * The {@link GraphIO} class contains methods for the
 * serialization/deserialization of XML-based {@link Graph} definitions.
 *
 * @author Maximilian Aulinger
 * @author Norman Fomferra
 * @author Ralf Quast
 */
public class GraphIO {

    /**
     * Serializes the given {@code graph} into XML
     *
     * @param graph  the {@code graph} to write into XML
     * @param writer the writer to use for serialization.
     */
    public static void write(Graph graph, Writer writer) {
        XStream xStream = initXstream();
        xStream.toXML(graph, writer);
    }

    /**
     * Deserializes a {@code graph} from an XML Reader.
     *
     * @param reader the readerto use for deserialization
     * @return the deserialized <code>graph</code>
     * @throws GraphException if an error occurs during reading
     */
    public static Graph read(Reader reader) throws GraphException {
        return read(reader, null);
    }


    /**
     * Deserializes a {@link Graph} from a XML Reader using a mapping
     * for the substitution of template variables inside the XML-based
     * {@link Graph} definition.
     *
     * @param reader    the XML reader
     * @param variables a mapping from template variable names to their string values.
     * @return the deserialized <code>graph</code>
     * @throws GraphException if an error occurs during reading
     */
    public static Graph read(Reader reader, Map<String, String> variables) throws GraphException {
        XStream xStream = initXstream();
        Reader inputReader = reader;
        if (variables != null) {
            inputReader = new TemplateReader(reader, variables);
        }
        // todo - throw exception if a given variable does not exist in the graph definition or if variables are not set.
        Graph graph = (Graph) xStream.fromXML(inputReader);
        if (graph.getVersion() == null) {
            throw new GraphException("No version is specified in the graph xml.");
        } else if (!Graph.CURRENT_VERSION.equals(graph.getVersion())) {
            throw new GraphException("Wrong version given in the graph xml. " +
                    "Given version: " + graph.getVersion() + " Current version: " + Graph.CURRENT_VERSION);
        }
        return graph;
    }

    /**
     * Creates and initializes the underlying serialization tool XStream.
     *
     * @return an initilalized instance of {@link XStream}
     */
    private static XStream initXstream() {
        XStream xStream = new XStream();
        xStream.setClassLoader(GraphIO.class.getClassLoader());

        xStream.alias("graph", Graph.class);
        xStream.useAttributeFor(Graph.class, "id");
        xStream.addImplicitCollection(Graph.class, "nodeList", Node.class);

        xStream.alias("header", Header.class);

        xStream.alias("target", HeaderTarget.class);
        xStream.useAttributeFor(HeaderTarget.class, "nodeId");
        xStream.aliasAttribute(HeaderTarget.class, "nodeId", "refid");

        xStream.addImplicitCollection(Header.class, "sources", "source", HeaderSource.class);
        xStream.registerConverter(new HeaderSource.Converter());

        xStream.addImplicitCollection(Header.class, "parameters", "parameter", HeaderParameter.class);
        xStream.registerConverter(new HeaderParameter.Converter());

        xStream.alias("node", Node.class);
        xStream.aliasField("operator", Node.class, "operatorName");
        xStream.useAttributeFor(Node.class, "id");

        xStream.alias("sources", SourceList.class);
        xStream.aliasField("sources", Node.class, "sourceList");
        xStream.registerConverter(new SourceList.Converter());

        xStream.alias("parameters", DomElement.class);
        xStream.aliasField("parameters", Node.class, "configuration");
        xStream.registerConverter(new DomElementXStreamConverter());

        xStream.alias("applicationData", ApplicationData.class);
        xStream.addImplicitCollection(Graph.class, "applicationData", ApplicationData.class);
        xStream.registerConverter(new ApplicationData.AppConverter());
        return xStream;
    }

    /**
     * Constructor. Private, in order to prevent instantiation.
     */
    private GraphIO() {

    }
}