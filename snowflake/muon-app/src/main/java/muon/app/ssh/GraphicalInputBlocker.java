/**
 * 
 */
package muon.app.ssh;

import muon.app.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author subhro
 *
 */
public class GraphicalInputBlocker extends JDialog implements InputBlocker {
	public static final Logger log = LoggerFactory.getLogger(GraphicalInputBlocker.class);
	private JFrame window;

	/**
	 * 
	 */
	public GraphicalInputBlocker(JFrame window) {
		super(window);
		this.window = window;
		setModal(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(400, 300);
	}

	@Override
	public void blockInput() {
		SwingUtilities.invokeLater(() -> {
			log.info("Making visible...");
			this.setLocationRelativeTo(window);
			this.setVisible(true);
		});
	}

	@Override
	public void unblockInput() {
		SwingUtilities.invokeLater(() -> {
			this.setVisible(false);
		});
	}

}
