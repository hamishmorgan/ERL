package uk.ac.susx.mlcl.erl.tac.io;

import nu.xom.Element;
import uk.ac.susx.mlcl.erl.tac.Query;
import uk.ac.susx.mlcl.erl.xml.XomB;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 17/07/2013
* Time: 14:48
* To change this template use File | Settings | File Templates.
*/
public class Tac2012QueryIO extends Tac2009QueryIO {

    static final String BEGIN_ELEM_NAME = "beg";
    static final String END_ELEM_NAME = "end";

    @Override
    Query parseQuery(Element queryElement) {
        final String id = queryElement.getAttribute(QUERY_ID_ATTR_NAME).getValue();
        final String name = queryElement.getFirstChildElement(QUERY_NAME_ELEM_NAME).getValue();
        final String docId = queryElement.getFirstChildElement(DOC_ID_ELEM_NAME).getValue();
        final int beg = Integer.parseInt(queryElement.getFirstChildElement(BEGIN_ELEM_NAME).getValue());
        final int end = Integer.parseInt(queryElement.getFirstChildElement(END_ELEM_NAME).getValue());
        return new Query(id, name, docId, beg, end);
    }

    @Override
    XomB.ElementBuilder formatQuery(XomB x, Query query) {
        return x.element(QUERY_ELEM_NAME)
                .addAttribute(QUERY_ID_ATTR_NAME, query.getId())
                .add(x.element(QUERY_NAME_ELEM_NAME).add(query.getName()))
                .add(x.element(DOC_ID_ELEM_NAME).add(query.getDocId()))
                .add(x.element(BEGIN_ELEM_NAME).add(Integer.toString(query.getBeg())))
                .add(x.element(END_ELEM_NAME).add(Integer.toString(query.getEnd())));
    }

}
