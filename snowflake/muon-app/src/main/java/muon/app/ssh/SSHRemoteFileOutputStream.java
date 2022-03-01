/**
 * 
 */
package muon.app.ssh;

import java.io.IOException;
import java.io.OutputStream;

import muon.app.App;
import net.schmizz.sshj.sftp.RemoteFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author subhro
 *
 */
public class SSHRemoteFileOutputStream extends OutputStream {
	public static final Logger log = LoggerFactory.getLogger(SSHRemoteFileOutputStream.class);
	private int bufferCapacity;

	/**
	 * @param remoteFile
	 */
	public SSHRemoteFileOutputStream(RemoteFile remoteFile, int remoteMaxPacketSize) {
		this.remoteFile = remoteFile;
		this.bufferCapacity = remoteMaxPacketSize - this.remoteFile.getOutgoingPacketOverhead();
		this.remoteFileOutputStream = this.remoteFile.new RemoteFileOutputStream(0, 16);
	}

	private RemoteFile remoteFile;
	private OutputStream remoteFileOutputStream;

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.remoteFileOutputStream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		this.remoteFileOutputStream.write(b);
	}

	@Override
	public void close() throws IOException {
		log.info(this.getClass().getName() + " closing");
		try {
			this.remoteFile.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		try {
			this.remoteFileOutputStream.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@Override
	public void flush() throws IOException {
		log.info(this.getClass().getName() + " flushing");
		this.remoteFileOutputStream.flush();
	}

	public int getBufferCapacity() {
		return bufferCapacity;
	}

	public void setBufferCapacity(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
	}

}
