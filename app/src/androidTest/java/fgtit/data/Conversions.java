package fgtit.data;

import android.util.Base64;

/**
 * Conversion class provides the methods of
 * converting the formats of the fingerprint template
 * private standard and ISO standard
 */
public class Conversions {
	private static Conversions mCom=null;
	public static Conversions getInstance(){
		if(mCom==null){
			mCom=new Conversions();
		}
		return mCom;
	}

	/**
	 * convert private standard into ISO standard
	 * @param itype the type number of the standard that need to convert to.
	 *              normally it is set as 2 to convert into ISO standard
	 * @param input the input private standard template in byte[]
	 * @param output the output byte[] to store the converted ISO template.
	 * @return
	 */
	public native int StdToIso(int itype,byte[] input,byte[] output);

	/**
	 * convert ISO standard into private standard
	 * @param itype the type number of the standard that need to convert to.
	 * 	              normally it is set as 2
	 * @param input the input ISO standard template in byte[]
	 * @param output the output byte[] to store the converted private template.
	 * @return
	 */
	public native int IsoToStd(int itype,byte[] input,byte[] output);

	/**
	 * check the standard type
	 * @param input the template which requires to be checked.
	 * @return the type number for the template:
	 * 1 for private standard
	 * 3 for ISO standard
	 */
	public native int GetDataType(byte[] input);

	/**
	 * The method of changing the coordinate of the private standard fingerprint template.
	 * @param input the template which requires to be changed.
	 * @param size  the size of the input byte array
	 * @param output the changed template in byte array
	 * @param dk the direction need to change.
	 * @return
	 */
	public native int StdChangeCoord(byte[] input,int size,byte[] output,int dk);

	/**
	 * The method of changing the coordinate of the ISO standard fingerprint template.
	 * @param input the template which requires to be changed.
	 * @param dk the direction need to change.
	 * @return the changed ISO template in Base64 string
	 */
	public String IsoChangeCoord(byte[] input, int dk){
		int dt=GetDataType(input);
		switch(dt){
		case 1:{
				byte output[] =new byte[512];
				byte crddat[]=new byte[512];
				StdChangeCoord(input,256,crddat,dk);
				StdToIso(2,crddat,output);
				return Base64.encodeToString(output,0,378, Base64.DEFAULT);
			}			
		case 2:{
				byte output[] =new byte[512];
				byte stddat[]=new byte[512];
				byte crddat[]=new byte[512];
				IsoToStd(1,input,stddat);
				StdChangeCoord(stddat,256,crddat,dk);
				StdToIso(2,crddat,output);
				return Base64.encodeToString(output,0,378, Base64.DEFAULT);
			}
		case 3:{
				byte output[] =new byte[512];
				byte stddat[]=new byte[512];
				byte crddat[]=new byte[512];
				IsoToStd(2,input,stddat);
				StdChangeCoord(stddat,256,crddat,dk);
				StdToIso(2,crddat,output);
				return Base64.encodeToString(output,0,378, Base64.DEFAULT);
			}
		}
		return "";
	}

	static {
		System.loadLibrary("conversions");
	}
}
