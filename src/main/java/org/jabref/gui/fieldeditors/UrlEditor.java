package org.jabref.gui.fieldeditors;

import java.util.List;
import java.util.function.Supplier;

import javax.swing.undo.UndoManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

import org.jabref.gui.DialogService;
import org.jabref.gui.autocompleter.SuggestionProvider;
import org.jabref.gui.fieldeditors.contextmenu.EditorMenus;
import org.jabref.gui.keyboard.KeyBindingRepository;
import org.jabref.gui.preferences.GuiPreferences;
import org.jabref.gui.undo.RedoAction;
import org.jabref.gui.undo.UndoAction;
import org.jabref.logic.formatter.bibtexfields.CleanupUrlFormatter;
import org.jabref.logic.formatter.bibtexfields.TrimWhitespaceFormatter;
import org.jabref.logic.integrity.FieldCheckers;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.Field;

import com.airhacks.afterburner.views.ViewLoader;
import jakarta.inject.Inject;

public class UrlEditor extends HBox implements FieldEditorFX {

    @FXML private final UrlEditorViewModel viewModel;
    @FXML private EditorTextField textField;

    @Inject private DialogService dialogService;
    @Inject private GuiPreferences preferences;
    @Inject private KeyBindingRepository keyBindingRepository;
    @Inject private UndoManager undoManager;

    public UrlEditor(Field field,
                     SuggestionProvider<?> suggestionProvider,
                     FieldCheckers fieldCheckers,
                     UndoAction undoAction,
                     RedoAction redoAction) {
        ViewLoader.view(this)
                  .root(this)
                  .load();

        this.viewModel = new UrlEditorViewModel(field, suggestionProvider, dialogService, preferences, fieldCheckers, undoManager);

        establishBinding(textField, viewModel.textProperty(), keyBindingRepository, undoAction, redoAction);

        Supplier<List<MenuItem>> contextMenuSupplier = EditorMenus.getCleanupUrlMenu(textField);
        textField.initContextMenu(contextMenuSupplier, preferences.getKeyBindingRepository());

        // init paste handler for UrlEditor to format pasted url link in textArea
        textField.setAdditionalPasteActionHandler(() -> textField.setText(new CleanupUrlFormatter().format(new TrimWhitespaceFormatter().format(textField.getText()))));

        new EditorValidator(preferences).configureValidation(viewModel.getFieldValidator().getValidationStatus(), textField);
    }

    public UrlEditorViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void bindToEntry(BibEntry entry) {
        viewModel.bindToEntry(entry);
    }

    @Override
    public Parent getNode() {
        return this;
    }

    @FXML
    private void openExternalLink(ActionEvent event) {
        viewModel.openExternalLink();
    }
}
