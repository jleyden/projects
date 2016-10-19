package editor;

import java.util.LinkedList;

public class UndoRedo {

    private LinkedList<UndoAction> undoActionList;
    private LinkedList<UndoAction> redoActionList;
    private TextList textList;
    private int undoSize;
    private int redoSize;

    public class UndoAction {

        private TextNode textNode;
        private TextNode prevNode;
        private String type;

        public UndoAction(TextNode t, String typeString) {
            textNode = t;
            prevNode = t.getPrev();
            type = typeString;
        }

        public String getType() {
            return type;
        }

        public TextNode getTextNode() {
            return textNode;
        }
    }

    public UndoAction createUndoAction(TextNode t, String typeString) {
        UndoAction action = new UndoAction(t, typeString);
        return action;
    }

    public UndoRedo(TextList t) {
        undoActionList = new LinkedList<>();
        redoActionList = new LinkedList<>();
        textList = t;
        undoSize = 0;
        redoSize = 0;
    }

    public void resetRedo() {
        redoActionList = new LinkedList<>();
        redoSize = 0;
    }

    public boolean noUndos() {
        return undoSize == 0;
    }

    public boolean noRedos() {
        return redoSize == 0;
    }

    public UndoAction getCurrentUndoAction() {
        return undoActionList.getFirst();
    }

    public UndoAction getCurrentRedoAction() {
        return redoActionList.getFirst();
    }

    public void addUndo(UndoAction undoAction) {
        if (undoSize == 100) {
            undoActionList.removeLast();
        }
        undoActionList.addFirst(undoAction);
        undoSize += 1;
    }

    public void addRedo(UndoAction undoAction) {
        if (redoSize == 100) {
            redoActionList.removeLast();
        }
        redoActionList.addFirst(undoAction);
        redoSize += 1;
    }

    public void undo() {
        if (undoSize == 0) {
            return;
        }
        UndoAction action = undoActionList.getFirst();
        if (action.type.equals("insert")) {
            undoInsert(action);
        } else if (action.type.equals("delete")) {
            undoDelete(action);
        }
        addRedo(action);
        undoActionList.removeFirst();
        undoSize -= 1;
    }

    public void redo() {
        if (redoSize == 0) {
            return;
        }
        UndoAction action = redoActionList.getFirst();
        if (action.type.equals("insert")) {
            redoInsert(action);
        } else if (action.type.equals("delete")) {
            redoDelete(action);
        }
        addUndo(action);
        redoActionList.removeFirst();
        redoSize-= 1;
    }

    private void undoInsert(UndoAction action) {
        textList.setCurrNode(action.textNode);
        textList.deleteText();
    }

    private void undoDelete(UndoAction action) {
        textList.setCurrNode(action.prevNode);
        textList.addText(action.textNode.getText());
        textList.setPrevToNext();
    }

    private void redoInsert(UndoAction action) {
        textList.setCurrNode(action.prevNode);
        textList.addText(action.textNode.getText());
        textList.setPrevToNext();
    }

    private void redoDelete(UndoAction action) {
        textList.setCurrNode(action.textNode);
        textList.deleteText();
    }
}
