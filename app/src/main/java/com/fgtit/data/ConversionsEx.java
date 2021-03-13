package com.fgtit.data;

import android.util.Base64;

/**
 * Class for conversion different format of fingerprint template
 * ISO19794-2 and ANSI-378
 */
public class ConversionsEx {

    /*
     *  define the number indicates different template formats
     */
    public static int STD_TEMPLATE = 0;
    public static int ANSI_378_2004 = 1;
    public static int ISO_19794_2005 = 2;
    public static int ISO_19794_2009 = 3;
    public static int ISO_19794_2011 = 4;

    /*
     *   The 4 different directions of the fingerprint image
     */
    public static int COORD_NOTCHANGE = 0;
    public static int COORD_MIRRORV = 1;
    public static int COORD_MIRRORH = 2;
    public static int COORD_ROTAING = 3;

    private static ConversionsEx mCom = null;

    public static ConversionsEx getInstance() {
        if (mCom == null) {
            mCom = new ConversionsEx();
        }
        return mCom;
    }

    /**
     * Convert private standard template into other formats
     * @param input private format fingerprint template.
     * @param output ISO format fingerprint template.
     * @param size the size of the template.
     * @param imgw the width of the fingerprint image.
     * @param imgh the width of the fingerprint image.
     * @param resx the resolution of image in horizontal.
     * @param resy the resolution of image in vertical.
     * @param type type of the format that need to convert into.
     * @return status of conversion
     */
    public native int StdToAnsiIso(byte[] input, byte[] output, int size, int imgw, int imgh, int resx, int resy, int type);

    /**
     * Method of converting the ISO format fingerprint template into private format.
     * @param input ISO format fingerprint template
     * @param output private format fingerprint template
     * @param type type of the format that need to convert into.
     * @return status of conversion.
     */
    public native int AnsiIsoToStd(byte[] input, byte[] output, int type);

    /**
     * Method of changing the coordinates of the fingerprint template in private format.
     * @param input Original fingerprint template
     * @param size The size of the template 256 bytes
     * @param output Changed fingerprint template
     * @param dk Direction need to change
     * @return status of change
     */
    public native int StdChangeCoord(byte[] input, int size, byte[] output, int dk);

    /**
     * Method of checking the format of the template.
     * @param input fingerprint template.
     * @return The type code of the format.
     */
    public native int GetDataType(byte[] input);

    /**
     * Example method of converting the template
     * @param input fingerprint template.
     * @param type The type code of the format.
     * @param dk Direction need to change
     * @return Base64 string template if convert succeed
     */
    public String ToAnsiIso(byte[] input, int type, int dk) {
        int dt = GetDataType(input);
        if (dt == STD_TEMPLATE) {
            byte output[] = new byte[512];
            byte tmpdat[] = new byte[512];
            StdChangeCoord(input, 256, tmpdat, dk);
            if(StdToAnsiIso(tmpdat,output,378,256,288,199,199,type)>0)
            //if (StdToAnsiIso(tmpdat, output, 378, 256, 288, 500, 500, type) > 0)
                return Base64.encodeToString(output, 0, 378, Base64.DEFAULT);
            return "";
        }
        return "";
    }

    public String To_Ansi378_2004(byte[] input) {
        return ToAnsiIso(input, ANSI_378_2004, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2005(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2005, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2009(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2009, COORD_NOTCHANGE);
    }

    public String To_Iso19794_2011(byte[] input) {
        return ToAnsiIso(input, ISO_19794_2011, COORD_NOTCHANGE);
    }

    static {
        System.loadLibrary("conversionsex");
    }
}
