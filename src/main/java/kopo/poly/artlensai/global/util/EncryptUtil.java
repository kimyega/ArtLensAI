package kopo.poly.artlensai.global.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public class EncryptUtil {

    /*
     * 암호화 알고리즘에 추가시킬 암호화 문구
     *
     *
     * 일반적인 암호화 알고리즘  SHA-256을 통해서만 암호화 시킬 경우, 암호화 된 값만 보고 일반적인 비밀번호에 대한 값을 쉽게
     * 예측이 가능함 따라서, 암호화할 때 암호화되는 값에 추가적인 문자열을 붙여서 함께 암호화를 진행함
     */
    final static String addMessage = "PolyDataAnalysis"; // 임의 값


    /*
     * AES128-CBC 암호화 알고리즘에 사용되는 초기 백터와 암호화 키
     */

    //초기 백터(16바이트 크기를 가지며, 16바이트 단위로 암호화시, 암호화할 총 길이가 16바이트가 되지 못하면 뒤에 추가하는 바이트)
    final static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00 };

    //AES128-CBC 암호화 알고리즘에 사용되는 키 (16자리 문자만 가능함)
    final static String key = "PolyTechnic12345"; //16글자(영문자 1글자당 1바이트임)


    /**
     * 해시 알고리즘(단방향 암호화 알고리즘)-SHA-256
     *
     * @param str 암호화 시킬 값
     * @return 암호화된 값
     */
    public static String encHashSHA256(String str) {
        try {
            String plantText = addMessage + str;

            MessageDigest sh = MessageDigest.getInstance("SHA-256");

            sh.update(plantText.getBytes());

            byte[] byteData = sh.digest();

            StringBuilder sb = new StringBuilder();

            for (byte b : byteData) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("SHA-256 암호화 실패", e);
        }
    }



    /**
     * AES128 CBC 암호화 함수
     *
     * 128은 암호화 키 길이를 의미함 128비트는 = 16바이트(1바이트=8비트 * 16 = 128)
     */
    public static String encAES128CBC(String str) {
        try {
            byte[] textBytes = str.getBytes("UTF-8");

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

            return Base64.encodeBase64String(cipher.doFinal(textBytes));

        } catch (Exception e) {
            throw new RuntimeException("AES 암호화 실패", e);
        }
    }

    /**
     * AES128 CBC 복호화 함수
     *
     * 128은 암호화 키 길이를 의미함 128비트는 = 16바이트(1바이트=8비트 * 16 = 128)
     */
    public static String decAES128CBC(String str) {
        try {
            byte[] textBytes = Base64.decodeBase64(str);

            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);

            return new String(cipher.doFinal(textBytes), "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 실패", e);
        }
    }
}
