package shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class Util {
    public static void writeIntBe(int i, OutputStream ostream) throws IOException {
        ostream.write((byte)(i >> 24));
        ostream.write((byte)(i >> 16));
        ostream.write((byte)(i >> 8));
        ostream.write((byte)(i >> 0));
    }
    public static void writeLongBe(long i, OutputStream ostream) throws IOException {
        writeIntBe((int)(i >> 32), ostream);
        writeIntBe((int)i, ostream);
    }
    public static byte[] strBytes(String str) {
        return str.getBytes(Charset.forName("UTF-8"));
    }
	public static String readAscii(InputStream st, int len) throws IOException
	{
		char[] chars = new char[len];
		for (int i=0; i < chars.length; i++)
			chars[i]=(char)st.read();
		return new String(chars);
	}
	public static long readLong(InputStream st) throws IOException
	{
		long out = (st.read() << 56)
            + (st.read() << 48)
            + (st.read() << 40)
            + (st.read() << 32)
            + (st.read() << 24)
            + (st.read() << 16)
            + (st.read() << 8)
            + (st.read());
            
		return out;
	}
	public static int readInt(InputStream st) throws IOException
	{
		int out = (st.read() << 24)
            + (st.read() << 16)
            + (st.read() << 8)
            + (st.read());
		return out;
	}
}
