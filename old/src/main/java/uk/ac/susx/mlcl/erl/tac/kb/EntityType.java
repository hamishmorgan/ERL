package uk.ac.susx.mlcl.erl.tac.kb;

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
    GPE,

    /**
     * Unknown type
     */
    UKN
}
