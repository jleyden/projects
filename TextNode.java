package editor;

import javafx.scene.text.Text;
import javafx.scene.text.Font;

public class TextNode {

	public static Font font;
	public Text text;
	public TextNode prev;
	public TextNode next;
	public double width;

	public TextNode(Text t, TextNode p, TextNode n) {
		text = t;
		prev = p;
		next = n;
	}

    public void setFont(Font font){
        text.setFont(font);
    }

    public boolean isNewLine() {
        return text.getText().equals("\n");
    }

    public boolean isSpace() {
        return text.getText().equals(" ");
    }

	public TextNode getNext() {
		return next;
	}

	public TextNode getPrev() {
        return prev;
	}

	public void shiftLeft(int distance) {
		int currPos = getX();
		text.setX(currPos - distance);
	}

	public void shiftRight(int distance) {
		int currPos = getX();
		text.setX(currPos + distance);
	}

    public void setX(int position) {
        text.setX(position);
    }

    public void setY(int position) {
        text.setY(position);
    }

    public int getX() {
        return (int) Math.round(text.getX());
    }

    public int getY() {
        return (int) Math.round(text.getY());
    }

    public Text getText() {
        return text;
    }

    public void setPrevToNext() {
        this.prev.next = this;
    }

    public boolean isFront() {
        return getX() == 5;
    }

    public int getWidth() {
        return (int) Math.round(text.getLayoutBounds().getWidth());
    }

    public int getHeight() {
        if (isNewLine()) {
            return (int) Math.round(text.getLayoutBounds().getHeight() / 2);
        } else {
            return (int) Math.round(text.getLayoutBounds().getHeight());
        }
    }

    public String toString() {
        return text.getText();
    }
}
