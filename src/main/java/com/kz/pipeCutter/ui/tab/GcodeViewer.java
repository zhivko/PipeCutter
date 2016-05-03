package com.kz.pipeCutter.ui.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.EditorKit;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import com.kz.pipeCutter.BBB.commands.AbortGCode;
import com.kz.pipeCutter.BBB.commands.CloseGCode;
import com.kz.pipeCutter.BBB.commands.OpenGCode;
import com.kz.pipeCutter.BBB.commands.StepGCode;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.NumberedEditorKit;
import com.kz.pipeCutter.ui.Settings;

public class GcodeViewer extends JPanel {

	WatchKey key;
	WatchService watcher;

	final JTextPane textArea;
	String folder;

	JTextField currentLine;

	public GcodeViewer() {
		super();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		this.setLayout(new MyVerticalFlowLayout());

		textArea = new JTextPane();
		textArea.setContentType("application/html");
		textArea.setEditorKit(new NumberedEditorKit());


		JScrollPane scroll = new JScrollPane(textArea); // place the JTextArea
														// in a
														// scroll pane
		scroll.setPreferredSize(new Dimension(800, 400));
		this.add(scroll, BorderLayout.WEST);

		this.add(buttonPanel);

		JButton buttonOpen = new JButton("Open GCode");
		buttonOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					new AbortGCode().start();
					Thread.sleep(1000);
					new CloseGCode().start();
					Thread.sleep(1000);
					new OpenGCode().start();
					Thread.sleep(1000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});
		buttonPanel.add(buttonOpen);

		JButton buttonPrevious = new JButton("Previous");
		buttonPrevious.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int lineNumber = Integer.valueOf(currentLine.getText());
				if (lineNumber > 0)
					currentLine.setText(String.valueOf(lineNumber - 1));
			}
		});
		buttonPanel.add(buttonPrevious, BorderLayout.EAST);

		currentLine = new JTextField();
		currentLine.setPreferredSize(new Dimension(50, 20));
		currentLine.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				// System.out.println("removeUpdate");
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					if (textArea.getDocument().getLength() > 1) {
						int lineNumber = Integer.valueOf(currentLine.getText());

						
						int startIndex = textArea.getDocument().getDefaultRootElement().getElement( lineNumber - 1 ).getStartOffset();
						int endIndex = textArea.getDocument().getDefaultRootElement().getElement( lineNumber ).getStartOffset();

						DefaultHighlightPainter painterWhite = new DefaultHighlighter.DefaultHighlightPainter(
								Color.WHITE);
						DefaultHighlightPainter painterGray = new DefaultHighlighter.DefaultHighlightPainter(
								Color.GRAY);

						textArea.getHighlighter().removeAllHighlights();

						textArea.getHighlighter().addHighlight(0, startIndex, painterWhite);
						textArea.getHighlighter().addHighlight(startIndex, endIndex, painterGray);

						textArea.getHighlighter().addHighlight(endIndex + 1, textArea.getDocument().getLength() - 1,
								painterWhite);

						Rectangle rect = textArea.modelToView(startIndex);
						textArea.scrollRectToVisible(rect);
					}

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub

			}
		});
		currentLine.setText("0");
		buttonPanel.add(currentLine);

		JButton buttonNext = new JButton("Next");
		buttonNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int lineNumber = Integer.valueOf(currentLine.getText());
				currentLine.setText(String.valueOf(lineNumber + 1));

				new StepGCode(lineNumber + 1).start();
			}
		});
		buttonPanel.add(buttonNext);

		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				folder = Settings.getInstance().getSetting("gcode_folder");
				refresh();

				try {
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							while (true) {
								try {
									File f = new File(folder);
									if (f.exists()) {
										refresh();
										watcher = FileSystems.getDefault().newWatchService();
										Path dir = Paths.get(f.getAbsolutePath());
										// key = dir.register(watcher,
										// ENTRY_CREATE, ENTRY_DELETE,
										// ENTRY_MODIFY);
										key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

										final WatchKey wk = watcher.take();

										for (WatchEvent<?> event : wk.pollEvents()) {
											WatchEvent.Kind<?> kind = event.kind();

											WatchEvent<Path> ev = (WatchEvent<Path>) event;
											Path filename = ev.context();

											if (filename.endsWith("prog.gcode")) {
												GcodeViewer.this.refresh();
											}
										}

										// Reset the key -- this step is
										// critical if you want to
										// receive further watch events. If the
										// key is no longer
										// valid,
										// the directory is inaccessible so exit
										// the loop.
										boolean valid = key.reset();
										if (!valid) {
											break;
										}
									}
									Thread.sleep(5000);

								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}
					}).start();

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
		});
	}

	protected void refresh() {
		FileReader reader;
		try {
			reader = new FileReader(new File(folder + File.separatorChar + "prog.gcode"));
			this.textArea.read(reader, "The force is strong with this one");
			this.textArea.repaint();
			currentLine.setText("0");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
