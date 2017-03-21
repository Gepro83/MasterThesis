package ac.at.wu.conceptfinder.userinterface;

import java.util.EventListener;

public interface CommandListener extends EventListener {
	public void commandDetected(CFCommand command);
}
