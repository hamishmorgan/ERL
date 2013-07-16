package uk.ac.susx.mlcl.erl.tac;

/**
 * Enum that indicates the genre of a particular document; i.e the type of resource is was taken from.
 *
 * @author Hamish Morgan
 */
public enum Genre {

    WEB,
    NEWS_WIRE;

    public static Genre valueOfAlias(String s) {
        return Aliases.valueOf(s).getGenre();
    }

    public static boolean isValidAlias(String s) {
        try {
            Aliases.valueOf(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private enum Aliases {

        WB(WEB),
        WL(WEB),
        NW(NEWS_WIRE);
        private final Genre genre;

        private Aliases(Genre genre) {
            this.genre = genre;
        }

        private Genre getGenre() {
            return genre;
        }
    }


//    /**
//     * web data
//     *
//     *
//     */
//    WL,
//    /**
//     * news-wire data
//     */
//    NW,
//
//    WB;
}
