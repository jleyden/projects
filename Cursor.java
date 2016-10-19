package editor;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import javafx.scene.text.Text;

public class Cursor {

	private int xLocation;
	private int yLocation;
	private int height;
	private TextList textList;
	public TextNode cursorTextNode;
    private int maxXLocation;


	public Cursor() {
		xLocation = 5;
		yLocation = 0;
		height = 12;
	}

    public int getXLocation() {
        return xLocation;
    }
    public int getYLocation() {
        return yLocation;
    }

	public Text getCurrText() {
		return cursorTextNode.text;
	}

	public void setTextList(TextList t) {
		textList = t;
	}

    public void setMaxXLocation(int location) {
        maxXLocation = location;
    }

	public void delete() {
		textList.deleteText();
	}

	public void insertText(Text t) {
		textList.addText(t);
	}

	private void setTextLocation(Text t){
		t.setX(xLocation);
		t.setY(yLocation);
	}

	public void update() {
		cursorTextNode = textList.getCurrNode();
		if (cursorTextNode.isNewLine()) {
            xLocation = 5;
            yLocation = cursorTextNode.getY() + cursorTextNode.getHeight();

        } else if (cursorTextNode.isSpace() && cursorTextNode.getNext().getY() > cursorTextNode.getY()) {         //Deals with ambiguous case between lines
            xLocation = 5;
            yLocation = cursorTextNode.getNext().getY();
        } else {
            xLocation = cursorTextNode.getX() + cursorTextNode.getWidth();
            yLocation = cursorTextNode.getY();
            if (xLocation > maxXLocation) {
                xLocation = maxXLocation;
            }
        }
		cursorDisplay.setX(xLocation);
		cursorDisplay.setY(yLocation);
        cursorDisplay.setHeight((double) cursorTextNode.getHeight());
	}

	public void moveLeft() {
        if (!textList.reachedBeginning(cursorTextNode)) {
            textList.moveCurrLeft();
            update();
        }
	}

	public void moveRight() {
        if (!textList.isLastTextNode(cursorTextNode)) {
            textList.moveCurrRight();
            update();
        }
	}

    //Make a get X location function

    public int getCursorXLocation(TextNode t) {
        if (t.isNewLine()) {
            return 5;
        } else if ((t.isSpace() && t.getNext().getY() > t.getY())) {
            return 5;
        } else if ((t.getX() + t.getWidth()) > maxXLocation) {
            return maxXLocation;
        } else {
            return t.getX() + t.getWidth();
        }
    }

    public int getCursorYLocation(TextNode t) {
        if (t.isNewLine()) {
            return t.getY() + t.getHeight();
        } else if (t.isSpace() && t.getNext().getY() > t.getY()) {
            return t.getNext().getY();
        } else {
            return t.getY();
        }
    }

//    public void moveUp() {
//        int currentXLocation = getCursorXLocation(cursorTextNode);
//        TextNode destination = cursorTextNode;
//        if (textList.reachedBeginning(destination)) {
//            return;
//        }
//        if (currentXLocation != 5) {
//            while (getCursorXLocation(destination) > 5) { //Go through to hit first line
//                if (textList.reachedBeginning(destination)) {
//                    return;
//                } else if (destination.getX() == 5 && !destination.getPrev().isSpace() && !destination.getPrev().isNewLine()) {
//                    break;
//                }
//                destination = destination.getPrev();
//            }
//            destination = destination.getPrev();
//            if (textList.reachedBeginning(destination)) {
//                return;
//            }
//            if (getCursorXLocation(destination) > currentXLocation) {
//                while (getCursorXLocation(destination.getPrev()) > currentXLocation) {
//                    if (textList.reachedBeginning((destination.getPrev()))) {
//                        return;
//                    }
//                    destination = destination.getPrev();
//                }
//                if (!textList.reachedBeginning(destination)) {
//                    TextNode secondOption = destination.getPrev();
//                    if ((currentXLocation - getCursorXLocation(secondOption)) < (getCursorXLocation(destination) - currentXLocation)) {
//                        destination = secondOption;
//                    }
//                }
//            }
//        } else {
//            if (textList.reachedBeginning(destination.getPrev())) {
//                return;
//            }
//            destination = destination.getPrev();
//            while (getCursorXLocation(destination) != 5){
//                if (textList.reachedBeginning(destination)){
//                    return;
//                }
//                destination = destination.getPrev();
//            }
//        }
//        textList.setCurrNode(destination);
//        currLine -= 1;
//        update();
//    }

    public void moveRightTo(TextNode startNode, int location) {
        int lineYLocation = getCursorYLocation(startNode);
        if (startNode.isNewLine()) {
            textList.setCurrNode(startNode.getPrev());
            update();
            return;
        }
        if (getCursorXLocation(startNode) != 5 && (startNode.getPrev().isSpace() || startNode.getPrev().isNewLine() || textList.isFirstTextNode(startNode))) {
            startNode = startNode.getPrev();
        }
        TextNode destination = startNode;
        if (location == 5) {
            textList.setCurrNode(destination);
            update();
        } else {
            if (!textList.isLastTextNode(destination)) {
                destination = destination.getNext();
            }
            while (getCursorXLocation(destination) < location && getCursorYLocation(destination.getNext()) == lineYLocation) {
                if (textList.reachedEnd(destination)) {
                    textList.setCurrNode(destination);
                    update();
                    return;
                } else {
                    destination = destination.getNext();
                }
            }
            TextNode secondOption = destination.getPrev();
            if ((location - getCursorXLocation(secondOption)) < (getCursorXLocation(destination) - location)) {
                destination = secondOption;
            }
            textList.setCurrNode(destination);
            update();
        }
    }

	public boolean atBeginning() {
		return textList.reachedBeginning(cursorTextNode);
	}


	// Displaying cursor ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public Rectangle cursorDisplay = new Rectangle(1, 14);

	public class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        Color clear = new Color(0, 0, 0, 0);
        private Color[] cursorColors = {Color.BLACK, clear};

        CursorBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursorDisplay.setFill(cursorColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % cursorColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    /** Makes the text bounding box change color periodically. */
    public void makeCursorBlink() {
        // Create a Timeline that will call the "handle" function of CursorBlinkEventHandler
        // every 1 second.
        final Timeline timeline = new Timeline();
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

}
