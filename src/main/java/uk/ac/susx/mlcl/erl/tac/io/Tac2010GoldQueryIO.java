package uk.ac.susx.mlcl.erl.tac.io;

import nu.xom.Element;
import uk.ac.susx.mlcl.erl.tac.queries.Query;
import uk.ac.susx.mlcl.erl.xml.XomB;

/**
 * Class for reading and writing to Query XML files following TAC 2010 Gold-standard specification.
 *
 * @author Hamish Morgan
 */

public class Tac2010GoldQueryIO extends Tac2009QueryIO {


    static final String ENTITY_ELEM_NAME = "entity";

    @Override
    Query parseQuery(Element queryElement) {
        final String id = queryElement.getAttribute(ID_ATTR_NAME).getValue();
        final String name = queryElement.getFirstChildElement(NAME_ELEM_NAME).getValue();
        final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
        final String entityNodeId = queryElement.getFirstChildElement(ENTITY_ELEM_NAME).getValue();
        return new Query(id, name, docId, entityNodeId);
    }

    @Override
    XomB.ElementBuilder formatQuery(XomB x, Query query) {
        return x.element(QUERY_ELEM_NAME)
                .addAttribute(ID_ATTR_NAME, query.getId())
                .add(x.element(NAME_ELEM_NAME).add(query.getName()))
                .add(x.element(DOC_ID_ELEM_NAME).add(query.getDocId()))
                .add(x.element(ENTITY_ELEM_NAME).add(query.getEntityNodeId()));
    }
}
