package com.example.finacneapp;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void  decrypt(){
        try{
            String data = "CWEk+U32Jd4m6Yx+pcKSCw==";
            String key = "1234567823435678";
            String iv = "1234567282345678";

            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encrypted1 = decoder.decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/Pkcs7Padding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            Log.d("Sr", originalString.trim());
        }
        catch (Exception e){
            Log.e("Sr", e.toString());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void encrypt(){
        try {
            byte[] encval=null;
            String data = "Hello";
            String key = "1234567812345678";
            String iv = "1234567812345678";
            Cipher cipher = Cipher.getInstance("AES/CBC/Pkcs7Padding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encval=cipher.doFinal(data.getBytes());
            String encryptedValue = Base64.getEncoder().encodeToString(encval);
            System.out.println(encryptedValue);
        }
        catch (Exception e){
            Log.e("Sr", e.toString());
        }
    }
}
