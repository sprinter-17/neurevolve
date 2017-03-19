package neurevolve.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class FileComboModel extends DefaultComboBoxModel<Path> implements PopupMenuListener {

    public static final String EXT = ".xml";
    private static final Logger LOG = Logger.getLogger(FileComboModel.class.getName());
    private final Path directory;

    public FileComboModel(String pathName) {
        this.directory = Paths.get(pathName);
        populateModel();
    }

    public Path getSelectedPath() {
        return (Path) getSelectedItem();
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        populateModel();
    }

    private void populateModel() {
        try {
            Path selectedPath = getSelectedPath();
            removeAllElements();
            for (Path path : Files.newDirectoryStream(directory, "*" + EXT)) {
                addElement(path);
                if (path.equals(selectedPath))
                    setSelectedItem(path);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }
}
