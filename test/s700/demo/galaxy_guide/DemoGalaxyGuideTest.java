package s700.demo.galaxy_guide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static s700.demo.galaxy_guide.RomanNumeral.*;

class DemoGalaxyGuideTest {

    @Test
    void parseRoman() {
        assertEquals(DemoGalaxyGuide.parseRoman(new RomanNumeral[]{I}), 1);
        assertEquals(DemoGalaxyGuide.parseRoman(new RomanNumeral[]{I,V}), 4);
        assertEquals(DemoGalaxyGuide.parseRoman(new RomanNumeral[]{I,I,X}), 8);
        assertEquals(DemoGalaxyGuide.parseRoman(new RomanNumeral[]{X,I}), 11);
        assertEquals(DemoGalaxyGuide.parseRoman(new RomanNumeral[]{L}), 50);
    }
}