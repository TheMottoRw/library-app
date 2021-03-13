package fgtit.fpcore;

import com.fgtit.data.ConversionsEx;

/**
 * The class for fingerprint matching
 */
public class FPMatch {

    private static FPMatch mMatch = null;

    public static FPMatch getInstance() {
        if (mMatch == null) {
            mMatch = new FPMatch();
        }
        return mMatch;
    }

    /**
     * Method of initializing the match
     * @param inittype Type of initialization, set for 1 as default.
     * @param initcode The website of company set as "https://www.hfteco.com/"
     * @return 0 for initialize ok, else for failed.
     */
    public native int InitMatch(int inittype, String initcode);

    /**
     * Method of matching the two template
     * the two template should be in private standard in byte array
     * @param piFeatureA Template 1 with length 256
     * @param piFeatureB Template 2 with length 256
     * @return
     */
    public native int MatchTemplate(byte[] piFeatureA, byte[] piFeatureB);

    /**
     * method for converting the template into private standard
     * @param input template need to convert
     * @param output output template byte array.
     */
    public void ToStd(byte[] input, byte[] output) {
        ConversionsEx.getInstance().AnsiIsoToStd(input,output,ConversionsEx.ISO_19794_2005);
    }


    /**
     * Method for matching the two templates in any standard format.
     * the method will check the format of template and then do the match process.
     * @param piFeatureA template 1 for match
     * @param piFeatureB template 2 for match
     * @return the match score
     */
    public int MatchFingerData(byte[] piFeatureA, byte[] piFeatureB) {
        int at = ConversionsEx.getInstance().GetDataType(piFeatureA);
        int bt = ConversionsEx.getInstance().GetDataType(piFeatureB);
        if ((at == 0) && (bt == 0)) {
            if (piFeatureA.length >= 512) {
                byte tmp[] = new byte[256];
                System.arraycopy(piFeatureA, 0, tmp, 0, 256);
                int sc1 = MatchTemplate(tmp, piFeatureB);
                System.arraycopy(piFeatureA, 256, tmp, 0, 256);
                int sc2 = MatchTemplate(tmp, piFeatureB);
                if (sc1 > sc2)
                    return sc1;
                else
                    return sc2;
            } else {
                return MatchTemplate(piFeatureA, piFeatureB);
            }
        } else {
            byte adat[] = new byte[512];
            byte bdat[] = new byte[512];
            ConversionsEx.getInstance().AnsiIsoToStd(piFeatureA, adat, ConversionsEx.ISO_19794_2005);
            ConversionsEx.getInstance().AnsiIsoToStd(piFeatureB, bdat, ConversionsEx.ISO_19794_2005);

            return MatchTemplate(adat, bdat);
        }
    }

    static {
        System.loadLibrary("fgtitalg");
        System.loadLibrary("fpcore");
    }
}
