package com.kz.pipeCutter.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public class NumberedEditorKit extends StyledEditorKit {
    public ViewFactory getViewFactory() {
        return new NumberedViewFactory();
    }
}