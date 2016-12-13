package eu.sollers.odata.jugc.common.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import eu.sollers.odata.jugc.common.exception.CsdlExtractException;

/**
 * Util for mapping from Java to Edm types.
 */
public class EdmTypeUtil {

    public static FullQualifiedName getPrimitiveTypeFor(Class<?> clazz) throws CsdlExtractException {
        if (clazz == String.class) {
            return EdmPrimitiveTypeKind.String.getFullQualifiedName();
        } else if ((clazz == Integer.class) || (clazz == Integer.TYPE)) {
            return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
        } else if ((clazz == Double.class) || (clazz == Double.TYPE)) {
            return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
        } else if (clazz == BigDecimal.class) {
            return EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
        } else if (clazz == Calendar.class) {
            return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
        } else if ((clazz == Boolean.class) || (clazz == Boolean.TYPE)) {
            return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
        } else if ((clazz == Short.class) || (clazz == Short.TYPE)) {
            return EdmPrimitiveTypeKind.Int16.getFullQualifiedName();
        } else if ((clazz == Long.class) || (clazz == Long.TYPE)) {
            return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
        } else if (clazz == Timestamp.class) {
            return EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName();
        } else {
            throw new CsdlExtractException("Property of type " + clazz + " cannot be mapped to OData type");
        }
    }

    public static boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
    }
}
