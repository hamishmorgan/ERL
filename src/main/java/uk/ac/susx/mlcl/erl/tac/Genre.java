package uk.ac.susx.mlcl.erl.tac;

import javax.annotation.Nonnull;

/**
 * Enum that indicates the genre of a particular document; i.e the type of resource is was taken from.
 *
 * @author Hamish Morgan
 */
public enum Genre {

    /**
     * Newswire - consisting of newswire feeds in multiple languages
     */
    NW,

    /**
     * Weblogs (WB/WL) – consisting of posts to informal web-based journals of varying topical content
     */
    WB,

    /**
     * Broadcast News - consisting of "talking head"-style news broadcasts from radio and/or television networks.
     */
    BN,

    /**
     * Broadcast Conversation - consisting of talk shows plus roundtable discussions and other interactive-style
     * broadcasts from radio and/or television networks.
     */
    BC,
    /**
     * Web Newsgroups – consisting of posts to electronic bulletin boards, Usenet newsgroups, discussion groups and
     * similar forums
     */
    NG,
    /**
     * Conversational Telephone Speech - consisting of phone conversations of variable duration among subjects who
     * may or may not know each other with topics that may or may not be assigned.
     */
    CTS;

    /**
     * Derive the Genre from the given document id string.
     *
     * @param docId Document id to get the genre from
     * @return the genre of the given document
     */
    @Nonnull
    public static Genre forDocumentId(@Nonnull final String docId) {
        if (docId.startsWith("APW_") || docId.startsWith("AFP_") || docId.startsWith("NYT_")
                || docId.startsWith("XIN_") || docId.startsWith("CNA_")
                || docId.startsWith("LTW_") || docId.startsWith("WPB_")) {
            return NW;
        } else if (docId.startsWith("eng-NG-") || docId.startsWith("eng-WL-")
                || docId.startsWith("groups.google.com_")
                || docId.startsWith("blogspot.com_")
                || docId.startsWith("juancole.com_")
                || docId.startsWith("typepad.com_")) {
            return WB;
        } else if (docId.startsWith("bolt-eng-DF-")) {
            return NG;
        } else {
            throw new IllegalArgumentException("Unknown genre for document " + docId);
        }
    }

}
