package s700.demo.galaxy_guide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interprets sentences providing information and asking for calculations of the intergalactic currency.
 * Data is stored in-memory. No persistence engine is configured in this project at this time.
 */
public class DemoGalaxyGuide {

    private static final int CONFIG_WOOD_A_WOOD_CHUCK_CAN_CHUCK = 700;

    /**
     * Note, when a material value is provided with unknown intergalactic unit,
     * there is a possibility that for the same material we got a known value in the past.
     * <p>
     * Set this config to true if you think it would often be stale and incorrect.
     * <p>
     * This feature is beyond original scope. This can be simplified away if not desired.
     */
    private static final boolean CONFIG_PURGE_STALE_POSSIBLE_INACCURACIES = false;

    public static final String CLUELESS = "I have no idea what you're talking about.";
    public static final String ACKNOWLEDGED = "Thank you. Acknowledged.";
    public static final String ACKNOWLEDGED_ALT = "Thank you. Acknowledged, but anticipating more information about the numerals.";

    private final Map<String, RomanNumeral> knownIntergalacticNumerals = new HashMap<>(RomanNumeral.values().length);
    private final Map<String, Double> knownMaterialValues = new HashMap<>();
    /** e.g. "glob glob Silver is 34 Credits" was provided before "glob is <i>[numeral]</i>" was specified */
    private final Map<String, PendingMaterialValue> pendingMaterialValues = new HashMap<>();
    /** efficient lookup table used to resolve any {@link #pendingMaterialValues} into {@link #knownMaterialValues} */
    private final Map<String, List<String>> pendingIntergalacticNumerals = new HashMap<>();

    // NOTE: string is converted to lowercase before parsing to ensure consistent handling of galaxy-provided units.
    // for example, we don't want multiple separate intergalacticUnits entries for glob vs Glob vs GLOB.
    // This may improve performance of regex by not requiring Pattern.CASE_INSENSITIVE option.

    /** e.g. "glob is I", "prok is V", etc. */
    private static final Pattern REGEX_INPUT_NUMERAL = Pattern.compile("(\\S+) is ([" + RomanNumeral.ALL_LETTERS.toLowerCase() + "])"); // String.format would be easier to read except that it prevents regex syntax highlighting

    /** e.g. "glob glob Silver is 34 Credits", "glob prok Gold is 57800 Credits", etc. */
    private static final Pattern REGEX_INPUT_MATERIAL = Pattern.compile("(.+) (\\S+) is ((?!0)\\d{1,18}) credits?"); // (?!0) prevents octal notation. keeps values in range of unsigned Long and prevents NumberFormatException

    /** e.g. "how much is pish tegj glob glob ?" */
    private static final Pattern REGEX_ASK_INTERGALACTIC_CONVERSION = Pattern.compile("how much is (.+)\\?");

    /** e.g. "how many Credits is glob prok Silver ?" */
    private static final Pattern REGEX_ASK_MATERIAL_VALUE = Pattern.compile("how many credits is (.+) (\\S+)\\?");

    // NOTE: as I warned you above, all these must be lowercase. This and a couple other normalizations are handled by the clean function.
    private static final String ULTIMATE_QUESTION_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING = "how much wood could a woodchuck chuck if a woodchuck could chuck wood?";

    private static final Pattern REGEX_CLEAN1 = Pattern.compile("\\s+");
    private static final Pattern REGEX_CLEAN2 = Pattern.compile("^\\s+|\\s+(?=\\?\\s*$|$)");
    private static final Pattern REGEX_SPACE = Pattern.compile(" ");
    /**
     * Performance optimization is to keep an empty list cached here at all times.
     * Every time it is used, it would be replaced with a new empty list.
     * This can be simplified away if not desired.
     */
    private List<String> emptyList = new ArrayList<>();

    /**
     * @param sentence                A specific sentence to be interpreted
     * @param allowEditsNotThreadSafe For performance reasons, this function is not thread-safe. For this reason, you can set this to false to prevent edits and only allow read-only sentences. In read-heavy environments, this is a plus.
     * @return answer to question. returns {@link #CLUELESS} if invalid input is provided. returns {@link #ACKNOWLEDGED} or {@link #ACKNOWLEDGED_ALT} if input was just information with no question. Returns a string starting with "Whoops! " If the some other error has occured.
     */
    public String query(String sentence, boolean allowEditsNotThreadSafe) {
        sentence = clean(sentence);
        Matcher m;
        try {

            if ((m = Util.matches(sentence, REGEX_INPUT_NUMERAL)) != null) {
                checkEdit(sentence, allowEditsNotThreadSafe);
                return queryInputNumeral(m.group(1), RomanNumeral.valueOf(m.group(2).toUpperCase()));
            }
            if ((m = Util.matches(sentence, REGEX_INPUT_MATERIAL)) != null) {
                checkEdit(sentence, allowEditsNotThreadSafe);
                return queryInputMaterial(m.group(1), m.group(2), Long.parseLong(m.group(3)));
            }
            if ((m = Util.matches(sentence, REGEX_ASK_INTERGALACTIC_CONVERSION)) != null)
                return queryAskIntergalacticConversion(m.group(1));
            if ((m = Util.matches(sentence, REGEX_ASK_MATERIAL_VALUE)) != null)
                return queryAskMaterialValue(m.group(1), m.group(2));
            if (sentence.equals(ULTIMATE_QUESTION_TO_LIFE_THE_UNIVERSE_AND_EVERYTHING))
                return queryAskUltimateQuestion();
        } catch (Whoops e) {
            return "Whoops! " + e.getMessage();
        }
        return CLUELESS;
    }

    private String queryInputNumeral(String intergalacticNumeral, RomanNumeral numeral) throws Whoops {
        RomanNumeral previousValue = this.knownIntergalacticNumerals.putIfAbsent(intergalacticNumeral, numeral);
        if (numeral.equals(previousValue))
            return ACKNOWLEDGED;
        if (previousValue != null)
            throw new Whoops("You have provided " + intergalacticNumeral + " equivalent as " + numeral + ". However in the past you have specified the value as " + numeral + ". Therefore the new value is rejected.");
        resolvePendingNumeralIfAny(intergalacticNumeral);
        return ACKNOWLEDGED;
    }

    private String queryInputMaterial(String intergalacticValue, String material, long credits) throws Whoops {
        String[] intergalacticNumeralArray = REGEX_SPACE.split(intergalacticValue);
        int materialQuantity;
        try {
            materialQuantity = parseIntergalactic(intergalacticNumeralArray);
        } catch (IllegalStateException e) {
            queuePendingMaterial(material, intergalacticNumeralArray, credits);
            return ACKNOWLEDGED_ALT;
        }
        pushKnownMaterial(material, materialQuantity, credits);
        return ACKNOWLEDGED;
    }

    private void pushKnownMaterial(String material, int materialQuantity, long credits) {
        knownMaterialValues.put(material, (double) credits / (double) materialQuantity);
        pendingMaterialValues.remove(material);
    }

    /**
     * @see #CONFIG_PURGE_STALE_POSSIBLE_INACCURACIES
     */
    private void queuePendingMaterial(String material, String[] intergalacticNumeralArray, long credits) {
        pendingMaterialValues.put(material, new PendingMaterialValue(intergalacticNumeralArray, credits));
        if (CONFIG_PURGE_STALE_POSSIBLE_INACCURACIES)
            knownMaterialValues.remove(material);
        for (String intergalacticNumeral : intergalacticNumeralArray) {
            if (!knownIntergalacticNumerals.containsKey(intergalacticNumeral)) {
                Map<String, List<String>> pendingIntergalacticNumerals = this.pendingIntergalacticNumerals;
                List<String> pendingMats = getListOrPopulateWithEmptyList(intergalacticNumeral, pendingIntergalacticNumerals);
                pendingMats.add(material);
            }
        }
    }

    /**
     * Utility function with a performance optimization. This can be simplified away if not desired.
     *
     * @see #emptyList
     */
    private <K> List<String> getListOrPopulateWithEmptyList(K key, Map<K, List<String>> map) {
        List<String> pendingMats = map.putIfAbsent(key, emptyList);
        if (pendingMats == null) {
            pendingMats = emptyList;
            emptyList = new ArrayList<>();
        }
        return pendingMats;
    }

    /**
     * This feature is beyond original scope but improves flexibility of the application.
     * In the event {@link #queryInputMaterial} was invoked before the required {@link #queryInputNumeral(String, RomanNumeral)}
     * is provided, this will <i>'put 2+2 together'</i> for the user.
     */
    private void resolvePendingNumeralIfAny(String intergalacticNumeral) {
        List<String> pendingMaterials = pendingIntergalacticNumerals.remove(intergalacticNumeral);
        if (pendingMaterials == null)
            return;
        pendingMaterials.forEach(material -> {
            PendingMaterialValue pendingMaterialValue = pendingMaterialValues.get(material);
            if (pendingMaterialValue == null)
                return; // pushKnownMaterial has since been invoked so this material is known and no resolve needed
            int materialQuantity;
            try {
                materialQuantity = parseIntergalactic(pendingMaterialValue.intergalacticQuantity);
            } catch (IllegalStateException e) { // undiscovered numeral passed to parseIntergalactic
                // in all cases this means an additional entry is present in pendingIntergalacticNumerals.
                // TODO add unit tests to scrutinize and prove this is a factual statement
                assert !pendingIntergalacticNumerals.isEmpty();
                return;
            }
            pushKnownMaterial(material, materialQuantity, pendingMaterialValue.credits);
        });
    }

    /** @see #parseIntergalactic(String[]) */
    private int parseIntergalactic(String intergalacticNumerals) throws IllegalStateException {
        return parseIntergalactic(REGEX_SPACE.split(intergalacticNumerals));
    }

    /**
     * @throws IllegalStateException if the corresponding {@link RomanNumeral} for one of the intergalactic numerals was not provided
     */
    private int parseIntergalactic(String[] intergalacticNumeralArray) throws IllegalStateException {
        RomanNumeral[] romanNumeralArray = new RomanNumeral[intergalacticNumeralArray.length];
        for (int i = 0; i < intergalacticNumeralArray.length; i++) {
            romanNumeralArray[i] = this.knownIntergalacticNumerals.get(intergalacticNumeralArray[i]);
            if (romanNumeralArray[i] == null)
                // TODO, should we create a custom exception for this for clarity?
                throw new IllegalStateException(String.format(
                        "Sorry, I've never seen the intergalactic numeral %s before.", romanNumeralArray[i]));
        }
        return parseRoman(romanNumeralArray);
    }

    static int parseRoman(RomanNumeral[] romanNumeralArray) {
        // TODO could have stricter detection of invalid values here.
        // for example VIX would produce 14 when it should probably throw exception (or produce 4)
        int value = 0;
        for (int i = 0; i < romanNumeralArray.length; i++) {
            RomanNumeral numeralHere = romanNumeralArray[i];
            boolean subtracting = false;
            for (int j = i + 1; j < romanNumeralArray.length; j++) {
                RomanNumeral numeralAhead = romanNumeralArray[j];
                if (numeralHere.value < numeralAhead.value) {
                    subtracting = true;
                    break;
                }
            }
            if (subtracting)
                value -= numeralHere.value;
            else
                value += numeralHere.value;
        }
        return value;
    }

    private String queryAskIntergalacticConversion(String intergalacticNumerals) throws Whoops {
        try {
            int quantity = parseIntergalactic(intergalacticNumerals);
            return String.format("%s is %d", intergalacticNumerals, quantity);
        } catch (IllegalStateException e) { // undiscovered numeral passed to parseIntergalactic
            throw new Whoops(e.getMessage());
        }
    }

    private String queryAskMaterialValue(String intergalacticNumerals, String material) throws Whoops {
        int quantity;
        try {
            quantity = parseIntergalactic(intergalacticNumerals);
        } catch (IllegalStateException e) {
            throw new Whoops(e.getMessage());
        }
        Double known = knownMaterialValues.get(material);
        if (known != null)
            return String.format("%s %s is %.2f Credits", intergalacticNumerals, material, known * quantity);
        PendingMaterialValue pending = pendingMaterialValues.get(material);
        if (pending != null) {
            List<String> unknownNumerals = new ArrayList<>();
            for (String intergalacticNumeral : pending.intergalacticQuantity)
                if (!knownIntergalacticNumerals.containsKey(intergalacticNumeral))
                    unknownNumerals.add(intergalacticNumeral);
            assert !unknownNumerals.isEmpty(); // TODO add unit tests to prove this will never happen
            throw new Whoops(String.format(
                    "I don't yet know the credits value because I have not yet learned conversions for %s. All I know is %s %s is %d.",
                    material, String.join(" or ", unknownNumerals), String.join(" ", pending.intergalacticQuantity), material, pending.credits));
        }
        throw new Whoops(String.format("I've never heard of %s before.", material));
    }

    private String queryAskUltimateQuestion() {
        Double known = knownMaterialValues.get("wood");
        String response = String.format("A woodchuck can chuck %d wood.", CONFIG_WOOD_A_WOOD_CHUCK_CAN_CHUCK);
        if (known != null)
            response += String.format(" That's %.2f credits.", known * CONFIG_WOOD_A_WOOD_CHUCK_CAN_CHUCK);
        else
            response += " I'm not sure how many credits that would be...";
        return response;
    }

    private void checkEdit(String sentence, boolean allowEdits) {
        if (!allowEdits)
            throw new RuntimeException(String.format(
                    "ensure thread-safety, and then set allowEdits to true before processing this query: %s", sentence));
    }

    /** Simple cleanup of extra whitespace or other user errors that may cause unexpected results */
    private String clean(String sentence) {
        sentence = sentence.toLowerCase(); // TODO specify a Unicode locale for best results
        sentence = REGEX_CLEAN1.matcher(sentence).replaceAll(" ");
        sentence = REGEX_CLEAN2.matcher(sentence).replaceAll("");
        return sentence;
    }

    public static class Whoops extends Exception {
        public Whoops(String s) {
        }
    }

    private class PendingMaterialValue {
        String[] intergalacticQuantity;
        Long credits;

        private PendingMaterialValue(String[] intergalacticQuantity, Long credits) {
            this.intergalacticQuantity = intergalacticQuantity;
            this.credits = credits;
        }
    }
}
