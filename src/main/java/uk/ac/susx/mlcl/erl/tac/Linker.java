package uk.ac.susx.mlcl.erl.tac;

import uk.ac.susx.mlcl.erl.tac.queries.Link;
import uk.ac.susx.mlcl.erl.tac.queries.Query;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 01/08/2013
* Time: 16:52
* To change this template use File | Settings | File Templates.
*/
public interface Linker {

    Link link(Query query);

}
