package editor;
import java.util.ArrayList;

//An ArrayList of all lines

public class LineRenderer {

	private ArrayList<TextNode> lineArray;
	private int numLines;
	private double lateralLocation; //for wordwrap
	private double maxLineWidth;
	private double resizeFactor;
	public TextList textList;
    private int textHeight;
    private int topLocation = 0;

	public LineRenderer(int maxWidth) {
		lineArray = new ArrayList<>();
		numLines = 1;
		maxLineWidth = maxWidth;
		resizeFactor = 1;
	}

    public void setMaxLineWidth(int width){
        maxLineWidth = width;
    } //For window resizing

    public int totalHeight() {
        return numLines * textHeight;
    }

	public void setTextList(TextList t) {
		textList = t;
	}

    public void setTopLocation(int location) {
        topLocation = location;
    }

    public int getTopLocation() {
        return topLocation;
    }

    public void render(int height) {
        assignLines();
        reAlignAll(height);
    }

    public void reAlignAll(int height) {
        textHeight = height;
        int i = 0;
        int h = 0;
        while (i < numLines) {
            reAlign(i, h);
            i += 1;
            h += height;
        }
    }

    public void reAlign(int index, int height) {
        TextNode t = lineArray.get(index);
        t.setX(5);
        t.setY(height);
        if (isLastLine(index)) {
            while (!textList.isLastTextNode(t)) {
                t.getNext().setX(t.getX() + t.getWidth());
                t.getNext().setY(height);
                t = t.next;
            }
        } else {
            while (t.getNext() != getNextLine(index)) {
                t.getNext().setX(t.getX() + t.getWidth());
                t.getNext().setY(height);
                t = t.getNext();
            }
        }
    }

	public void setFirstTextNode(TextNode t) {
		setLine(0, t);
	}

	public void setLine(int index, TextNode t) {
		lineArray.add(index, t);
	}

	public void assignLines() {
        //Assigns lines
        numLines = 1;
        int index = 0;
        int currentWidth = 0;
        if (textList.isEmpty()) {
            return;
        }
        TextNode textNode = textList.getFirstTextNode();
        setLine(index, textNode);
        currentWidth += textNode.getWidth();
        textNode = textNode.getNext();
        index += 1;
        while (!textList.reachedEnd(textNode)) {
            if (!willFit(currentWidth, textNode.getWidth())) {
                if (!textNode.isNewLine() && !textNode.isSpace()) {  //After above iteration, we must check if we reached the end
                    textNode = getWordWrapNode(textNode, index - 1);
                    setLine(index, textNode);
                    index += 1;
                    numLines += 1;
                    currentWidth = textNode.getWidth();
                }
            } else if (textNode.getPrev().isNewLine()) {
                setLine(index, textNode);
                index += 1;
                numLines += 1;
                currentWidth = textNode.getWidth();
            } else {
                currentWidth += textNode.getWidth();
            }
                textNode = textNode.getNext();
            }
        }

    private TextNode getWordWrapNode(TextNode textNode, int currentIndex) {
        TextNode t = textNode;
        t = t.getPrev();
        while (!textList.reachedBeginning(t)) {
            if (t.isSpace()) {
                return t.getNext();
            } else if (lineArray.get(currentIndex) == t) { //Word is larger than line width, so wrap the original textNode
                return textNode;
            }
            t = t.getPrev();
        }
        return t;
    }

    public TextNode getFirstOfLine(int index) {
        return lineArray.get(index);
    }

	public TextNode getNextLine(int index) {
        if (isLastLine(index)) {
            throw new NullPointerException("First line; no lines above");
        }
		return lineArray.get(index + 1);
	}

	public TextNode getPrevLine(int index) {
        if (index == 0) {
            throw new NullPointerException("Reaches last line; no lines below");
        }
		return lineArray.get(index - 1);
	}


	private boolean willFit(int currentWidth, int textWidth) {
		return currentWidth + textWidth <= maxLineWidth;
	}

	private boolean isLastLine(int index) {
		return index == numLines - 1;
	}


    public TextNode getLineBelow(int currentLocation) {
        int currLine = Math.round(currentLocation / textHeight);
        if (currLine < numLines - 1) {
            return lineArray.get(currLine + 1);
        } else {
            return lineArray.get(currLine);
        }
    }

    public TextNode getLineAbove(int currentLocation) {
        int currLine = Math.round(currentLocation / textHeight);
        if (currLine > 0) {
            return lineArray.get(currLine - 1);
        } else {
            return lineArray.get(currLine);
        }
    }

    public TextNode getLineAt(int yLocation) {
        int currLine = Math.round((yLocation + topLocation) / textHeight);
        if (currLine > numLines - 1) {
            return lineArray.get(numLines - 1);
        } else {
            return lineArray.get(currLine);
        }
    }


//	public void shiftLineLeft(int distance, int index, TextNode t) {
//		if (isLastLine(index)) {
//			while (!textList.isLastTextNode(t.prev)) {
//				t.shiftLeft(distance);
//				t = t.next;
//			}
//		} else {
//			while (t != getNextLine(index)) {
//				t.shiftLeft(distance);
//				t = t.next;
//			}
//		}
//	}

//	public void shiftLineRight(int distance, int index, TextNode t) {
//		if (isLastLine(index)) {
//			while (!textList.isLastTextNode(t.prev)) {
//				t.shiftRight(distance);
//				t = t.next;
//			}
//		} else {
//            TextNode nextLineNode = getNextLine(index);
//			while (t != getNextLine(index)) {
//				t.shiftRight(distance);
//				t = t.next;
//			}
//		}
//	}

}
