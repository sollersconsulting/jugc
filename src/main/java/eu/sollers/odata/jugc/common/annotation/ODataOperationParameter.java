package eu.sollers.odata.jugc.common.annotation;

/**
 * OData Action/Function contains parameters.
 * When Bound, first parameter should be an entity or collection,
 * which Action/Function is bound to.
 */
public @interface ODataOperationParameter {
    String name();
}
