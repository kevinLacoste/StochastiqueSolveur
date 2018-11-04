package view;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class JTextAreaStream extends OutputStream{
	private JTextArea txtArea;

    public JTextAreaStream(JTextArea txtArea) {
        this.txtArea = txtArea;
    }

    @Override
    public void write(int b) throws IOException {
        txtArea.append(String.valueOf((char)b));
        txtArea.setCaretPosition(txtArea.getDocument().getLength());
        txtArea.update(txtArea.getGraphics());
    }
}
