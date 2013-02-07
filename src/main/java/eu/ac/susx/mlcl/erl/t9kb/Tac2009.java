/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ac.susx.mlcl.erl.t9kb;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Abstraction Singularity The Good Intentions Singularity
 *
 * The exact instant, during software development, at which your elegantly designed framework starts
 * to degenerate towards the unmaintainable corrosive mess it was destined to become.
 *
 * Abstraction Singularity: The exact instant, during software development, at which your elegantly
 * designed framework starts to degenerate towards the unmaintainable corrosive mess it was destined
 * to become.
 *
 * @author hiam20
 */
@Nonnull
public class Tac2009 {

    public static void main(String[] args)
            throws ParserConfigurationException, SAXException, IOException {


        File dataDir = new File("/Volumes/LocalScratchHD/LocalHome/Data/TAC 2009 KBP Evaluation Reference/data");
        File mapDbFile = new File("t9kb.mapdb");

        T9KnowledgeBase kb = T9KnowledgeBase.create(mapDbFile, dataDir);
//        T9KB kb = T9KB.open(mapDbFile);


        // Tristan Nitot
        T9Entity ent = kb.getEntityById("E0420900");        
        System.out.println(ent);
        
        
        kb.close();

    }
}
