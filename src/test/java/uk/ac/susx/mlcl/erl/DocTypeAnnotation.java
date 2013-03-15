package uk.ac.susx.mlcl.erl;

import edu.stanford.nlp.ling.CoreAnnotation;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 15/03/2013
* Time: 18:09
* To change this template use File | Settings | File Templates.
*/
public class DocTypeAnnotation implements CoreAnnotation<String> {
    public Class<String> getType() {
        return String.class;
    }
}
