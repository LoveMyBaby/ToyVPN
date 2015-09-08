package com.example.hackeris.toyvpn;

/**
 * Created by hackeris on 15/9/7.
 */
public class Encrypter {

    public void encrypt(byte[] array, int length){
        Encrypt.encrypt(array, length);
    }

    public void decrypt(byte[] array,int length){

        Encrypt.decrypt(array, length);
    }
}
