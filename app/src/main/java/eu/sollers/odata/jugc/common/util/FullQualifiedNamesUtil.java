package eu.sollers.odata.jugc.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to return full qualified names of types, actions, annotations
 */
public class FullQualifiedNamesUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FullQualifiedNamesUtil.class);
    private static final Properties properties = new Properties();
    private static boolean defaultsApplied = true;

    static {
        try {
            InputStream propFile = FullQualifiedNamesUtil.class.getResourceAsStream("/odata.properties");
            if (propFile != null) {
                defaultsApplied = false;
                properties.load(propFile);
            }
        } catch (IOException e) {
            defaultsApplied = true;
        }

        if (defaultsApplied) {
            LOG.info("Unable to read OData properties. Defaults applied");
        }
    }

    // TODO: verify 'namespace' as a property and following 'namespace.' properties
    private static final String NAMESPACE_BASE = propertyOrDefault("namespace", "Schema");

    public static final String ROOT = propertyOrDefault("root", "OData.svc");

    public static final class NAMESPACE {
        public static final String ENUMS = subnamespace(propertyOrDefault("namespace.enums", "Enums"));
        public static final String ENTITIES = subnamespace(propertyOrDefault("namespace.entities", "Entities"));
        public static final String ACTIONS = subnamespace(propertyOrDefault("namespace.actions", "Actions"));
        public static final String FUNCTIONS = subnamespace(propertyOrDefault("namespace.functions", "Functions"));
        public static final String COMPLEX_TYPES = subnamespace(
                propertyOrDefault("namespace.complextypes", "ComplexTypes"));
    }

    public static final String CONTAINER = propertyOrDefault("container", "Service");
    public static final FullQualifiedName CONTAINER_FQN = new FullQualifiedName(NAMESPACE_BASE, CONTAINER);

    public static FullQualifiedName createFullQualifiedEnumName(String name) {
        return new FullQualifiedName(NAMESPACE.ENUMS, name);
    }

    public static FullQualifiedName createFullQualifiedEntityName(String name) {
        return new FullQualifiedName(NAMESPACE.ENTITIES, name);
    }

    public static FullQualifiedName createFullQualifiedActionName(String name) {
        return new FullQualifiedName(NAMESPACE.ACTIONS, name);
    }

    public static FullQualifiedName createFullQualifiedFunctionName(String name) {
        return new FullQualifiedName(NAMESPACE.FUNCTIONS, name);
    }

    public static FullQualifiedName createFullQualifiedComplexTypeName(String name) {
        return new FullQualifiedName(NAMESPACE.COMPLEX_TYPES, name);
    }

    private static String propertyOrDefault(String property, String defaultValue) {
        if (defaultsApplied) {
            return defaultValue;
        }
        String prop = properties.get(property).toString();
        return prop == null ? defaultValue : prop;
    }

    private static String subnamespace(String sub) {
        return NAMESPACE_BASE + "." + sub;
    }
}
