package cloudStorage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 为了实现将程序执行的输出既输出到控制台，又输出到文件
 * @author HuaHero
 *
 */
public class MultiOutputStream extends OutputStream{
	OutputStream outputStream1,outputStream2;
	public MultiOutputStream(OutputStream stream1,OutputStream stream2) throws IOException{
		outputStream1 = stream1;
		outputStream2 = stream2;
	}
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		outputStream1.write(b);
		outputStream2.write(b);
	}

}
