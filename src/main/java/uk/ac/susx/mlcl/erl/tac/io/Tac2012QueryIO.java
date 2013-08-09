package uk.ac.susx.mlcl.erl.tac.io;

import nu.xom.Element;
import uk.ac.susx.mlcl.erl.tac.queries.Query;
import uk.ac.susx.mlcl.erl.xml.XomB;

import javax.annotation.Nonnull;

/**
 * Class for reading and writing to Query XML files following TAC 2012 specification.
 *
 * @author Hamish Morgan
 */

public class Tac2012QueryIO extends Tac2011QueryIO {


    static final String BEGIN_ELEM_NAME = "beg";
    static final String END_ELEM_NAME = "end";

    @Nonnull
    @Override
    Query parseQuery(@Nonnull Element queryElement) {
        final String id = queryElement.getAttribute(ID_ATTR_NAME).getValue();
        final String name = queryElement.getFirstChildElement(NAME_ELEM_NAME).getValue();
        final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
        final int beg = Integer.parseInt(queryElement.getFirstChildElement(BEGIN_ELEM_NAME).getValue());
        final int end = Integer.parseInt(queryElement.getFirstChildElement(END_ELEM_NAME).getValue());
        return new Query(id, name, docId, beg, end);
    }

    @Nonnull
    @Override
    XomB.ElementBuilder formatQuery(@Nonnull XomB x, @Nonnull Query query) {
        return x.element(QUERY_ELEM_NAME)
                .addAttribute(ID_ATTR_NAME, query.getId())
                .add(x.element(NAME_ELEM_NAME).add(query.getName()))
                .add(x.element(DOC_ID_ELEM_NAME).add(query.getDocId()))
                .add(x.element(BEGIN_ELEM_NAME).add(Integer.toString(query.getBeg())))
                .add(x.element(END_ELEM_NAME).add(Integer.toString(query.getEnd())));
    }
}
