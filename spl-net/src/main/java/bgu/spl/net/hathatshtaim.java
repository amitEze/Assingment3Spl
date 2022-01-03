package bgu.spl.net;

public class hathatshtaim {
    public static void main(String[] args){
        byte[] bytes = new byte[1 << 10];
        short s=5;
        String result="\0";
        bytes=(result+';').getBytes();
        for(int i=0;i<1;i++)
            System.out.println(bytes[i]);
        String curious = new String(bytes,0,1);
        if(curious.charAt(0)=='\0')
            System.out.println("true");
    }
    public static byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
