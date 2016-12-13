package eu.sollers.odata.jugc.common.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.sollers.odata.jugc.common.annotation.ODataEntity;
import eu.sollers.odata.jugc.common.annotation.ODataKey;
import eu.sollers.odata.jugc.common.annotation.ODataNavigationProperty;
import eu.sollers.odata.jugc.common.annotation.ODataProperty;
import eu.sollers.odata.jugc.common.util.ReflectionUtil;

/**
 * Base for all OData Entities.
 * Subclasses has to be POJOs.
 */
@MappedSuperclass
public class JpaOdataEntity extends Entity {
    @Transient
    private static final Logger LOG = LoggerFactory.getLogger(JpaOdataEntity.class);

    @Transient
    private Proxy proxy = new Proxy();

    /**
     * URI is something like id of the OData entity and it is used in many places,
     * e.g. Navigation Properties, Location Header.
     * If the entity has a set assigned, it should be used inside URI id.
     *
     * @return URI for accessing the entity or null if no set assigned.
     */
    @Override
    public URI getId() {
        if (proxy.set == null) {
            proxy.set = getClass().getAnnotation(ODataEntity.class).entitySetName();
        }

        if (proxy.id == null && !proxy.set.equals(ODataEntity.NO_SET)) {
            try {
                String uriString = proxy.set + "(";
                List<Field> keys = new ArrayList<>();
                for (Field f : getAccessibleFields()) {
                    if (f.isAnnotationPresent(ODataKey.class)) {
                        keys.add(f);
                    }
                }
                if (keys.size() == 1) {
                    uriString += keys.get(0).get(this).toString();
                } else {
                    for (Field key : keys) {
                        // for many keys either their order is important or they can be used as name=value pairs
                        uriString +=
                                key.getAnnotation(ODataProperty.class).name() + "=" + key.get(this).toString() + ",";
                    }
                    uriString = uriString.substring(0, uriString.length() - 1);
                }
                proxy.id = new URI(uriString + ")");
            } catch (IllegalArgumentException | IllegalAccessException | URISyntaxException e) {
                LOG.error("Can't find Entity Key", e);
            }
        }
        return proxy.id;
    }

    @Override
    public Property getProperty(String name) {
        for (Property prop : getProperties()) {
            if (name.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public List<Property> getProperties() {
        if (proxy.properties == null) {
            proxy.properties = new ArrayList<>();
            proxy.propertyAccessors = new HashMap<>();
            for (Field f : getAccessibleFields()) {
                ODataProperty annotation = f.getAnnotation(ODataProperty.class);
                if (annotation != null) {
                    String name = annotation.name();
                    RealAccessors<JpaOdataEntity> accessors = new RealAccessors<>(this, f);
                    proxy.properties.add(new Property(null, name, annotation.valueType(), accessors.get(this)));
                    proxy.propertyAccessors.put(name, accessors);
                }
            }
        }
        return proxy.properties;
    }

    public String getJavaFieldNameForODataName(String name) {
        if (proxy.propertyAccessors.containsKey(name)) {
            return proxy.propertyAccessors.get(name).getFieldName();
        } else if (proxy.linkAccessors.containsKey(name)) {
            return proxy.linkAccessors.get(name).getFieldName();
        }
        throw new IllegalArgumentException("No Java property for OData property " + name + " on " + getClass());
    }

    @Override
    public Link getNavigationLink(String name) {
        for (Link link : getNavigationLinks()) {
            if (name.equals(link.getTitle())) {
                return link;
            }
        }
        return null;
    }

    @Override
    public List<Link> getNavigationLinks() {
        if (proxy.links == null) {
            proxy.links = new ArrayList<>();
            proxy.linkAccessors = new HashMap<>();
            for (Field f : getAccessibleFields()) {
                ODataNavigationProperty annotation = f.getAnnotation(ODataNavigationProperty.class);
                if (annotation != null) {
                    Link link = new Link();

                    RealAccessors<JpaOdataEntity> accessors = new RealAccessors<>(this, f);
                    Object value = accessors.get(this);

                    String name = annotation.name();
                    link.setTitle(name);
                    link.setRel("http://docs.oasis-open.org/odata/ns/related/" + name);

                    if (annotation.collection()) {
                        EntityCollection entityCollection = new EntityCollection();
                        entityCollection.getEntities().addAll((Collection<Entity>) value);
                        link.setInlineEntitySet(entityCollection);
                    } else {
                        link.setInlineEntity((Entity) value);
                    }

                    proxy.links.add(link);
                    proxy.linkAccessors.put(name, accessors);
                }
            }
        }
        return proxy.links;
    }

    /**
     * Method for reading the refreshed version of the entity.
     * It flushes cache.
     */
    public void refreshAndGet() {
        proxy.id = null;
        proxy.links = null;
        proxy.fields = null;

        getId();
        getProperties();
        getNavigationLinks();
    }

    /**
     * Method for updating just sent fields (those in entity).
     */
    public void patch(Entity entity) {
        setFromEntity(entity, false);
    }

    /**
     * Method for updating and setting fields which were not sent to null.
     */
    public void put(Entity entity) {
        setFromEntity(entity, true);
    }

    protected Field[] getAccessibleFields() {
        if (proxy.fields == null) {
            proxy.fields = ReflectionUtil.getFieldsUpToJpaOdataEntity(getClass());
            for (Field f : proxy.fields) {
                f.setAccessible(true);
            }
        }
        return proxy.fields;
    }

    @SuppressWarnings("unchecked")
    // TODO: navigation should be also updated
    private void setFromEntity(Entity entity, boolean overrideWithNull) {
        List<Property> sourceProperties = entity.getProperties();

        if (overrideWithNull) {
            for (Property prop : proxy.properties) {
                if (!sourceProperties.contains(prop)) {
                    proxy.propertyAccessors.get(prop.getName()).set(this, null); // unchecked
                }
            }
        }

        for (Property prop : sourceProperties) {
            RealAccessors<JpaOdataEntity> accessors = proxy.propertyAccessors.get(prop.getName()); // unchecked
            accessors.set(this, prop.getValue());
        }

        // cleaning proxy - we need new values in the cache since next call
        proxy.properties = null;
    }

    /**
     * Proxy to skip iterating through annotations each time some method is called.
     */
    private static class Proxy {
        URI id;
        String set;
        Field[] fields;
        List<Link> links;
        List<Property> properties;
        Map<String, RealAccessors> linkAccessors;
        Map<String, RealAccessors> propertyAccessors;
    }

    /**
     * Getter and setter from the "java" entity.
     *
     * @param <T>
     *         entity to be used in OData service
     */
    private static class RealAccessors<T extends JpaOdataEntity> {
        private String name;
        private Method getter;
        private Method setter;

        RealAccessors(T entity, Field f) {
            name = f.getName();
            try {
                getter = entity.getClass().getMethod(prepareGetter());
                setter = entity.getClass().getMethod(prepareSetter());
            } catch (NoSuchMethodException e) {
                LOG.error("Reflection problem with preparing an accessor", e);
            }
        }

        String getFieldName() {
            return name;
        }

        Object get(T entity) {
            try {
                return getter.invoke(entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("Reflection problem with getting a value", e);
            }

            return null;
        }

        void set(T entity, Object value) {
            try {
                setter.invoke(entity, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("Reflection problem with setting a value", e);
            }
        }

        private String prepareGetter() {
            return prepareAccessor('g');
        }

        private String prepareSetter() {
            return prepareAccessor('s');
        }

        private String prepareAccessor(char type) {
            return type + "et" + name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }
}
