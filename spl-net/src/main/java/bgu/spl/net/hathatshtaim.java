package bgu.spl.net;

import java.nio.charset.StandardCharsets;

public class hathatshtaim {
    public static void main(String[] args){
        byte []byt ={0};
        String result=new String(byt,0,1);
        byt=result.getBytes(StandardCharsets.UTF_8);
        System.out.println(byt[0]);
    }
    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
