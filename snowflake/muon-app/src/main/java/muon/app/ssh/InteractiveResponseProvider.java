/**
 * 
 */
package muon.app.ssh;

import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import muon.app.App;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author subhro
 *
 */
public class InteractiveResponseProvider implements ChallengeResponseProvider {
	public static final Logger log = LoggerFactory.getLogger(InteractiveResponseProvider.class);
	private boolean retry = true;

	@Override
	public List<String> getSubmethods() {
		return Collections.emptyList();
	}

	@Override
	public void init(Resource resource, String name, String instruction) {
		log.info("ChallengeResponseProvider init - resource: "
				+ resource + " name: " + name + " instruction: " + instruction);
		if ((name != null && name.length() > 0)
				|| (instruction != null && instruction.length() > 0)) {
			JOptionPane.showMessageDialog(null, name + "\n" + instruction);
		}
	}

	@Override
	public char[] getResponse(String prompt, boolean echo) {
		log.info("prompt: " + prompt + " echo: " + echo);

		if (echo) {
			String str = JOptionPane.showInputDialog(prompt);
			if (str != null) {
				return str.toCharArray();
			}
			retry = false;
			return null;
		} else {
			JPasswordField passwordField = new JPasswordField(30);
			int ret = JOptionPane.showOptionDialog(null,
					new Object[] { prompt, passwordField }, "Input",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, null, null);
			if (ret == JOptionPane.OK_OPTION) {
				return passwordField.getPassword();
			}
			retry = false;
			return null;
		}
	}

	@Override
	public boolean shouldRetry() {
		return retry;
	}

}
