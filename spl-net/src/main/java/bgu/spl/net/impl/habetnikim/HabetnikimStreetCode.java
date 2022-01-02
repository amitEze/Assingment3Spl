package bgu.spl.net.impl.habetnikim;
import bgu.spl.net.api.MessageEncoderDecoder;

public class HabetnikimStreetCode implements MessageEncoderDecoder<String> {

    @Override
    public String decodeNextByte(byte nextByte) {
        return null;
    }

    @Override
    public byte[] encode(String message) {
        return new byte[0];
    }
}
