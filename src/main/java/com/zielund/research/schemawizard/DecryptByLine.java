package com.zielund.research.schemawizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DecryptByLine {

	private static final String DECRYPT_ALGORITHM = JDBCExtract.AES_ALGORITHM;

	/**
	 * @param args
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
		String filename = args[0];
		String keyString = args[1];
		byte[] key = JDBCExtract.HexStringToByteArr(keyString);
		Cipher cipher = Cipher.getInstance(DECRYPT_ALGORITHM);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line;
		while ((line = br.readLine()) != null) {
			byte[] b = JDBCExtract.HexStringToByteArr(line);
			b = cipher.doFinal(b);
			String bs = new String(b);
			System.out.println(bs);
		}
	}

}
