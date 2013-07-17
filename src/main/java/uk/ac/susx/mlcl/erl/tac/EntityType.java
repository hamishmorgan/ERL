package uk.ac.susx.mlcl.erl.tac;

/**
 * Enum of valid entity types; organizations, persons, and locations (geo-political entities).
 *
 * @author Hamish Morgan
 */
public enum EntityType {
    /**
     * Organisation entity type
     */
    ORG,
    /**
     * Person entity type
     */
    PER,
    /**
     * Geo-Political entity type (a.k.a Location)
     */
    GPE
}
