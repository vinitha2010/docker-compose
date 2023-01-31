package org.jenkinsci.plugins.vncviewer;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

public class ConsoleNoteButton extends ConsoleNote {
	private static final long serialVersionUID = 1L;
	private final String caption;
    private final String html;

    public ConsoleNoteButton(String caption, String html) {
        this.caption = caption;
        this.html = html;
    }

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        text.addMarkup(charPos, "<a href=\""+  html + "\" target=\"new\"><button>" + caption + "</button></a>");
        return null;
    }
}
