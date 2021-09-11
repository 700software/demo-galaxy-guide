package s700.demo.galaxy_guide;

/**
 * Fun fact: values can be multiplied by 1,000 by adding an overline to the letters.
 * However, this feature of Roman Numerals is not supported by this application.
 */
public enum RomanNumeral {

    I(1),
    V(5),
    X(10),
    L(50),
    C(100),
    D(500),
    M(1000);

    public static final String ALL_LETTERS = "IVXLCDM";
    public final int value;

    RomanNumeral(int value) {
        this.value = value;
    }
}
