package eu.sollers.odata.jugc.common.provider.csdl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlOperation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import eu.sollers.odata.jugc.common.annotation.ODataEntity;
import eu.sollers.odata.jugc.common.annotation.ODataKey;
import eu.sollers.odata.jugc.common.annotation.ODataNavigationProperty;
import eu.sollers.odata.jugc.common.annotation.ODataOperation;
import eu.sollers.odata.jugc.common.annotation.ODataOperationParameter;
import eu.sollers.odata.jugc.common.annotation.ODataProperty;
import eu.sollers.odata.jugc.common.entity.JpaOdataEntity;
import eu.sollers.odata.jugc.common.entity.JpaOdataMediaEntity;
import eu.sollers.odata.jugc.common.exception.CsdlExtractException;
import eu.sollers.odata.jugc.common.util.EdmTypeUtil;
import eu.sollers.odata.jugc.common.util.FullQualifiedNamesUtil;
import eu.sollers.odata.jugc.common.util.ReflectionUtil;

/**
 * Class which describes an entity in the way understandable for Provider.
 * Operates on Java class in order to get its declared OData annotations.
 */
public class JpaCsdlEntity<T extends JpaOdataEntity> implements CsdlProvider {
    private Class<T> clazz;
    private CsdlEntitySet eSet;
    private CsdlEntityType eType;
    private FullQualifiedName fqn;

    // operations bound to the entity
    private List<CsdlAction> actions = new ArrayList<>();
    private List<CsdlFunction> functions = new ArrayList<>();

    private List<CsdlNavigationProperty> navigationProperties = new ArrayList<>();
    private List<CsdlNavigationPropertyBinding> navigationBindings = new ArrayList<>();

    private ODataEntity entityAnnotation;

    public JpaCsdlEntity(Class<T> clazz) throws CsdlExtractException {
        this.clazz = clazz;
        this.entityAnnotation = clazz.getAnnotation(ODataEntity.class);
        if (this.entityAnnotation == null) {
            throw new CsdlExtractException("Entity must be annotated as ODataEntity to build its CSDL representation");
        }
        init();
    }

    @Override
    public CsdlEntitySet getCsdlEntitySet() {
        return this.eSet;
    }

    @Override
    public CsdlEntityType getCsdlEntityType() {
        return this.eType;
    }

    /**
     * Methods from the entity.
     */
    @Override
    public List<CsdlAction> getCsdlActions() {
        return this.actions;
    }

    /**
     * Read-only methods from the entity.
     */
    @Override
    public List<CsdlFunction> getCsdlFunctions() {
        return this.functions;
    }

    /**
     * Entity shouldn't define enums inside.
     */
    @Override
    public CsdlEnumType getCsdlEnumType() {
        return null;
    }

    /**
     * Entity shouldn't define classes inside.
     */
    @Override
    public CsdlComplexType getCsdlComplexType() {
        return null;
    }

    @Override
    public FullQualifiedName getFQN() {
        return fqn;
    }

    /**
     * Returns class which is the source of CSDL.
     */
    public Class<T> getJavaClass() {
        return clazz;
    }

    private void init() throws CsdlExtractException {
        this.fqn = FullQualifiedNamesUtil.createFullQualifiedEntityName(entityAnnotation.name());

        this.eType = new CsdlEntityType().setName(entityAnnotation.name())
                                         .setHasStream(JpaOdataMediaEntity.class.isAssignableFrom(getClass()));

        List<CsdlPropertyRef> keys = new ArrayList<>();
        List<CsdlProperty> properties = new ArrayList<>();

        Field[] fields = ReflectionUtil.getFieldsUpToJpaOdataEntity(clazz);
        for (Field f : fields) {
            if (f.isAnnotationPresent(ODataKey.class)) {
                keys.add(extractKey(f));
            }
            if (f.isAnnotationPresent(ODataProperty.class)) {
                properties.add(extractProperty(f.getAnnotation(ODataProperty.class)));
            }
            if (f.isAnnotationPresent(ODataNavigationProperty.class)) {
                handleNavigation(f.getAnnotation(ODataNavigationProperty.class), f);
            }
        }
        Method[] methods = ReflectionUtil.getMethodsUpToJpaOdataEntity(clazz);
        for (Method method : methods) {
            ODataOperation funcAnnotation = method.getAnnotation(ODataOperation.class);
            if (funcAnnotation != null) {
                if (funcAnnotation.action()) {
                    handleAction(method);
                } else {
                    handleFunction(method);
                }
            }
        }
        this.eType.setProperties(properties).setNavigationProperties(this.navigationProperties).setKey(keys);
        this.eSet = new CsdlEntitySet().setName(entityAnnotation.entitySetName()).setType(getFQN())
                                       .setNavigationPropertyBindings(this.navigationBindings);
    }

    private CsdlPropertyRef extractKey(Field f) throws CsdlExtractException {
        ODataProperty prop = f.getAnnotation(ODataProperty.class);
        if (prop == null) {
            throw new CsdlExtractException("Field annotated as ODataKey must be annotated as ODataProperty as well");
        }
        return new CsdlPropertyRef().setName(prop.name());
    }

    private CsdlProperty extractProperty(ODataProperty annotation) {
        return new CsdlProperty().setName(annotation.name()).setType(annotation.type().getFullQualifiedName());
    }

    private void handleFunction(Method method) throws CsdlExtractException {
        CsdlFunction function = new CsdlFunction();
        handleOperationAnnotation(method, function);
        this.functions.add(function);
    }

    private void handleAction(Method method) throws CsdlExtractException {
        CsdlAction action = new CsdlAction();
        handleOperationAnnotation(method, action);
        this.actions.add(action);
    }

    private void handleOperationAnnotation(Method method, CsdlOperation operation) throws CsdlExtractException {
        ODataOperation methodAnnotation = method.getAnnotation(ODataOperation.class);
        operation.setName(methodAnnotation.name()).setBound(true);

        List<CsdlParameter> parameters = new ArrayList<>();
        CsdlParameter bindingParameter = new CsdlParameter().setNullable(false).setType(getFQN())
                                                            .setName(BINDING_PARAM_NAME);
        parameters.add(bindingParameter);

        Class<?>[] clazzes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < clazzes.length; i++) {
            Class<?> clazz = clazzes[i];
            // TODO: more than primitive types
            CsdlParameter csdlParameter = new CsdlParameter().setNullable(false)
                                                             .setType(EdmTypeUtil.getPrimitiveTypeFor(clazz))
                                                             .setCollection(EdmTypeUtil.isCollection(clazz));
            for (Annotation annotation : paramAnnotations[i]) {
                if ((annotation instanceof ODataOperationParameter)) {
                    ODataOperationParameter paramAnnotation = (ODataOperationParameter) annotation;
                    csdlParameter.setName(paramAnnotation.name());
                    break;
                }
            }
            parameters.add(csdlParameter);
        }
        operation.setParameters(parameters);

        Class<?> returnTypeClass = method.getReturnType();
        if (!returnTypeClass.equals(Void.TYPE)) {
            CsdlReturnType returnType = new CsdlReturnType().setCollection(EdmTypeUtil.isCollection(returnTypeClass))
                                                            .setType(EdmTypeUtil.getPrimitiveTypeFor(returnTypeClass));

            operation.setReturnType(returnType);
        }
    }

    private void handleNavigation(ODataNavigationProperty annotation, Field f) throws CsdlExtractException {
        boolean collection = annotation.collection();

        Class<?> type;
        if (collection) {
            // TODO: array?
            ParameterizedType listType = (ParameterizedType) f.getGenericType();
            type = (Class) listType.getActualTypeArguments()[0];
        } else {
            type = f.getType();
        }

        ODataEntity entity = type.getAnnotation(ODataEntity.class);
        if (entity == null) {
            throw new CsdlExtractException("Type " + type.getName() + " must be annotated as ODataEntity");
        }

        addNavigationProperty(entity, annotation.name(), collection);
    }

    private void addNavigationProperty(ODataEntity toEntity, String name, boolean isCollection) {
        CsdlNavigationPropertyBinding binding = new CsdlNavigationPropertyBinding().setTarget(toEntity.entitySetName())
                                                                                   .setPath(name);

        FullQualifiedName fqn = FullQualifiedNamesUtil.createFullQualifiedEntityName(toEntity.name());
        CsdlNavigationProperty navigationProperty = new CsdlNavigationProperty().setName(name).setType(fqn)
                                                                                .setCollection(isCollection);

        this.navigationBindings.add(binding);
        this.navigationProperties.add(navigationProperty);
    }
}