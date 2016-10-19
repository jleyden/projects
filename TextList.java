package editor;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

//A Linked List structure that stores all text information
public class TextList {

	private TextNode textSentinel;
	private TextNode currNode;
	private Text sentText;
	public int numChars;

    private String fontName;
    private int fontSize;

	public TextList() {
		sentText = new Text(5, 0, "");
		textSentinel = new TextNode(sentText, null, null);
		textSentinel.next = textSentinel;
		textSentinel.prev = textSentinel;
		currNode = textSentinel;
		numChars = 0;
	}

    public void setFont(String fontName, int fontSize) {
        Font newFont = new Font(fontName, fontSize);
        textSentinel.setFont(newFont);
        if (!isEmpty()) {
            TextNode t = getFirstTextNode();
            while (!reachedEnd(t)) {
                t.setFont(newFont);
                t = t.getNext();
            }
        }
    }

	public void addText(Text t) {
		if (currNode == textSentinel) {
			addTextFirst(t);
		} else {
			addTextAfter(currNode, t);
		}
		currNode = currNode.next;

	}

	public void deleteText() {
		if (currNode == textSentinel) {
			throw new NullPointerException("No more text");
		} else {
			currNode = currNode.prev;
			removeTextNode(currNode.next);
		}
	}

	public TextNode getCurrNode() {
		return currNode;
	}

    public void setPrevToNext() {
        currNode.prev.next = currNode;
    }

	public void moveCurrLeft() {
		currNode = currNode.prev;
	}

	public void moveCurrRight() {
		currNode = currNode.next;
	}

    public void setCurrNode (TextNode t) {
        currNode = t;
    }

	private void addTextFirst(Text t) {
		addTextAfter(textSentinel, t);
	}

	public void addTextAfter(TextNode existing, Text t) {
		TextNode oldNext = existing.next;
		TextNode newNext = new TextNode(t, existing, oldNext);
		oldNext.prev = newNext;
		existing.next = newNext;
		numChars += 1;
	}

	private void removeTextNode(TextNode t) {
		TextNode before = t.prev;
		TextNode after = t.next;
		before.next = after;
		after.prev = before;
		numChars -= 1;
	}

	public TextNode getNextTextNode(TextNode t) {
        if (isLastTextNode(t)) {
            throw new NullPointerException("End of line");
        }
        return t.next;
    }

	public TextNode getPrevTextNode(TextNode t) {
		if (isFirstTextNode(t)) {
			throw new NullPointerException("Beginning of line");
		}
		return t.prev;
	}

	public TextNode getFirstTextNode() {
		return textSentinel.next;
	}

	public TextNode getLastTextNode() {
		return textSentinel.prev;
	}

	public boolean isLastTextNode(TextNode t) {
		return t.next == textSentinel;
	}

	public boolean isFirstTextNode(TextNode t) {
		return t.prev == textSentinel;
	}

	public boolean reachedEnd(TextNode t) {
		return t == textSentinel;
	}

    public boolean reachedBeginning(TextNode t) {
        return t == textSentinel;
    }

    public int getWordWidth(TextNode FirstNode) {
        int width = 0;
        if (FirstNode.isSpace())  {
            return 0;
        } else {
            while (!FirstNode.isSpace()){
                width += FirstNode.getWidth();
            }
        }
        return width;
    }

	public boolean isEmpty() {
		return numChars == 0;
	}


//	public String toString() { // Figure out how to transform document into a string
//		String string = "";
//		TextNode t = getFirstTextNode();
//		int i = 0;
//		while (i < numChars) {
//			string = string + t.toString();
//			t = t.getNext();
//			i += 1;
//		}
//		return string;
//	}
}
