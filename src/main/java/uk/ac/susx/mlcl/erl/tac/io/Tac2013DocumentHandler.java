package uk.ac.susx.mlcl.erl.tac.io;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 22/07/2013
* Time: 14:22
* To change this template use File | Settings | File Templates.
*/
interface Tac2013DocumentHandler {



    void documentStart(long offset);

    void documentEnd(long offset, CharSequence contents);

    void error(String description);
}
