package eu.sollers.odata.jugc.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for properties which navigate to other entities.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataNavigationProperty {
    String name();

    // TODO: do we need this?
    String type() default "";

    /**
     * It is possible to navigate either to a single entity
     * or to an entity collection.
     */
    // TODO: reflection should differentiate by the type of an annotated property
    boolean collection() default false;
}
