/**
 * 
 */
package muon.app.ui.components.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.common.InputTransferChannel;
import muon.app.common.local.LocalFileSystem;
import muon.app.ssh.SSHRemoteFileInputStream;
import muon.app.ssh.SSHRemoteFileOutputStream;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.session.FileChangeWatcher.FileModificationInfo;
import muon.app.ui.components.session.files.transfer.FileTransfer;
import muon.app.ui.components.session.files.transfer.FileTransferProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.OptionPaneUtils;
import util.PathUtils;
import util.PlatformUtils;
import util.TimeUtils;

/**
 * @author subhro
 *
 */
public class ExternalEditorHandler extends JDialog {

	public static final Logger log = LoggerFactory.getLogger(ExternalEditorHandler.class);
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JButton btnCanel;
	private JFrame frame;
	private FileChangeWatcher fileWatcher;
	private AtomicBoolean stopFlag = new AtomicBoolean(false);

	/**
	 * 
	 */
	public ExternalEditorHandler(JFrame frame) {
		super(frame);
		setModal(true);
		this.frame = frame;
		setSize(400, 200);

		progressBar = new JProgressBar();
		progressLabel = new JLabel("Transferring...");
		progressLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
		progressLabel.setFont(App.SKIN.getDefaultFont().deriveFont(18.0f));
		btnCanel = new JButton("Cancel");
		Box bottomBox = Box.createHorizontalBox();
		bottomBox.add(Box.createHorizontalGlue());
		bottomBox.add(btnCanel);

		progressLabel.setAlignmentX(Box.LEFT_ALIGNMENT);
		progressBar.setAlignmentX(Box.LEFT_ALIGNMENT);
		bottomBox.setAlignmentX(Box.LEFT_ALIGNMENT);

		Box box = Box.createVerticalBox();
		box.add(progressLabel);
		box.add(progressBar);
		box.add(Box.createVerticalGlue());
		box.add(bottomBox);

		box.setBorder(new EmptyBorder(10, 10, 10, 10));

		this.add(box);
		this.fileWatcher = new FileChangeWatcher(files -> {
			List<String> messages = new ArrayList<>();
			messages.add("Some file(s) have been modified, upload changes to server?\n");
			messages.add("Changed file(s):");
			messages.addAll(files.stream().map(e -> e.toString()).collect(Collectors.toList()));
			if (OptionPaneUtils.showOptionDialog(this.frame, messages.toArray(new String[0]),
					"File changed") == JOptionPane.OK_OPTION) {
				this.fileWatcher.stopWatching();
				App.EXECUTOR.submit(() -> {
					try {
						log.info("In app executor");
						this.saveRemoteFiles(files);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}, 1000);

		this.fileWatcher.startWatching();

	}

	private void saveRemoteFiles(List<FileModificationInfo> files) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(0);
			setVisible(true);
		});
		this.fileWatcher.stopWatching();
		long totalSize = 0L;
		for (FileModificationInfo info : files) {
			totalSize += info.localFile.length();
		}
		log.info("Total size: " + totalSize);
		long totalBytes = 0L;
		for (FileModificationInfo info : files) {
			log.info("Total size: " + totalSize + " opcying: " + info);
			totalBytes += saveRemoteFile(info, totalSize, totalBytes);
		}
		fileWatcher.resumeWatching();
		log.info("Transfer complete");
		SwingUtilities.invokeLater(() -> {
			setVisible(false);
		});
	}

	/**
	 * @param info
	 * @param total
	 * @param totalBytes
	 * @return
	 */
	private long saveRemoteFile(FileModificationInfo info, long total, long totalBytes) {
		log.info("Init transfer...1");
		SessionContentPanel scp = App.getSessionContainer(info.activeSessionId);
		if (scp == null) {
			log.info("No session found");
			return info.remoteFile.getSize();
		}

		log.info("Init transfer...2");
		try (OutputStream out = scp.getRemoteSessionInstance().getSshFs().outputTransferChannel()
				.getOutputStream(info.remoteFile.getPath()); InputStream in = new FileInputStream(info.localFile)) {
			int cap = 8192;
			if (out instanceof SSHRemoteFileOutputStream) {
				cap = ((SSHRemoteFileOutputStream) out).getBufferCapacity();
			}
			byte[] b = new byte[cap];
			log.info("Init transfer...");
			while (!this.stopFlag.get()) {
				int x = in.read(b);
				if (x == -1) {
					break;
				}
				totalBytes += x;
				out.write(b, 0, x);
				final int progress = (int) ((totalBytes * 100) / total);
				SwingUtilities.invokeLater(() -> {
					progressBar.setValue(progress);
				});
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info.remoteFile.getSize();
	}

	/**
	 * Downloads a remote file using SFTP in a temporary directory and if download
	 * completes successfully, adds it for monitoring.
	 * 
	 * @param remoteFile
	 * @param remoteFs
	 * @param activeSessionId
	 * @param openWith        should show windows open with dialog
	 * @param app             should open with specified app
	 * @throws IOException
	 */
	public void openRemoteFile(FileInfo remoteFile, SshFileSystem remoteFs, int activeSessionId, boolean openWith,
			String app) throws IOException {
		this.fileWatcher.stopWatching();
		Path tempFolderPath = Files.createTempDirectory(UUID.randomUUID().toString());
		Path localFilePath = tempFolderPath.resolve(remoteFile.getName());
		this.stopFlag.set(false);
		this.progressLabel.setText(remoteFile.getName());
		File localFile = localFilePath.toFile();

		App.EXECUTOR.submit(() -> {
			try (InputStream in = remoteFs.inputTransferChannel().getInputStream(remoteFile.getPath());
					OutputStream out = new FileOutputStream(localFile)) {
				int cap = 8192;
				if (in instanceof SSHRemoteFileInputStream) {
					cap = ((SSHRemoteFileInputStream) in).getBufferCapacity();
				}
				byte[] b = new byte[cap];
				long totalBytes = 0L;
				while (!this.stopFlag.get()) {
					int x = in.read(b);
					if (x == -1) {
						break;
					}
					totalBytes += x;
					out.write(b, 0, x);
					final int progress = (int) ((totalBytes * 100) / remoteFile.getSize());
					SwingUtilities.invokeLater(() -> {
						progressBar.setValue(progress);
					});
				}
				localFile.setLastModified(TimeUtils.toEpochMilli(remoteFile.getLastModified()));
				fileWatcher.addForMonitoring(remoteFile, localFilePath.toAbsolutePath().toString(), activeSessionId);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fileWatcher.resumeWatching();
				SwingUtilities.invokeLater(() -> {
					try {
						if (app == null) {
							PlatformUtils.openWithDefaultApp(localFilePath.toFile(), openWith);
						} else {
							PlatformUtils.openWithApp(localFilePath.toFile(), app);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					setVisible(false);
				});
			}
		});

		setLocationRelativeTo(frame);
		setVisible(true);
	}
}
