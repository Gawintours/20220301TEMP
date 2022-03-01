/**
 * 
 */
package muon.app.ui.components.settings;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import muon.app.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FontUtils;

/**
 * @author subhro
 *
 */
public class FontItemRenderer extends JLabel implements ListCellRenderer<String> {
	public static final Logger log = LoggerFactory.getLogger(FontItemRenderer.class);

	/**
	 * 
	 */
	public FontItemRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
			boolean isSelected, boolean cellHasFocus) {
		log.info("Creating font in renderer: " + value);
		Font font = FontUtils.loadTerminalFont(value).deriveFont(Font.PLAIN, 14);
		setFont(font);
		setText(FontUtils.TERMINAL_FONTS.get(value));
		setBackground(isSelected ? App.SKIN.getAddressBarSelectionBackground() : App.SKIN.getSelectedTabColor());
		setForeground(isSelected ? App.SKIN.getDefaultSelectionForeground() : App.SKIN.getDefaultForeground());
		return this;
	}

}
