package eu.sollers.odata.jugc.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If this annotate a method from ODataEntity class,
 * this method will be exposed as OData Bound Function.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataOperation {
    String name();

    /**
     * Actions are called with POST method, while Functions are just for GET.
     * Also Action should modify an entity.
     */
    // TODO: intelligent differentiation when parsing reflections
    boolean action() default false;
}
