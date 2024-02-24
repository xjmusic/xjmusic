package io.xj.gui.controllers.content.common;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

/**
 * {@link DragResizer} can be used to add mouse listeners to a {@link Region}
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 * <p>
 * <pre>
 * DragResizer.makeResizable(myAnchorPane);
 * </pre>
 */
public class DragResizer {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 10;

    private final AnchorPane region;
    private final Node parent;

    private double y;

    private double x;

    private boolean initMinHeight;

    private boolean initMinWidth;

    private boolean draggableZoneX, draggableZoneY, draggableZoneZ, draggableZoneN;

    private boolean dragging;

    private double orgSceneX;
    private double orgTranslateX;

    private DragResizer(AnchorPane aRegion, Node aParent) {
        parent = aParent;
        region = aRegion;
    }

    public static void makeResizable(AnchorPane region, Node parent) {
        final DragResizer resizer = new DragResizer(region, parent);

        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
        region.setOnMouseReleased(resizer::mouseReleased);
    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
        if (isInDraggableZone(event) || dragging) {
            if (draggableZoneY) {
                region.setCursor(Cursor.S_RESIZE);
            }

            if (draggableZoneX) {
                region.setCursor(Cursor.E_RESIZE);
            }
            if (draggableZoneZ) {
                region.setCursor(Cursor.W_RESIZE);
            }
            if (draggableZoneN) {
                region.setCursor(Cursor.N_RESIZE);
            }
        } else {
            region.setCursor(Cursor.OPEN_HAND);
        }
    }

    protected boolean isInDraggableZone(MouseEvent event) {
        draggableZoneY = event.getY() > (region.getHeight() - RESIZE_MARGIN);
        draggableZoneX = event.getX() > (region.getWidth() - RESIZE_MARGIN);
        draggableZoneZ = event.getSceneX() <= (getLeftBorderPosition(region) + RESIZE_MARGIN);
        draggableZoneN = event.getSceneY() <= (getTopBorderPosition(region) + RESIZE_MARGIN);
        return (draggableZoneY || draggableZoneX || draggableZoneZ || draggableZoneN);
    }

    public static double getLeftBorderPosition(Node node) {
        // Get the local bounds of the AnchorPaneR
        Bounds localBounds = node.getBoundsInLocal();

        // Transform the local bounds to scene coordinates
        Bounds sceneBounds = node.localToScene(localBounds);

        // Calculate the position of the left border
        return sceneBounds.getMinX();
    }

    public static double getTopBorderPosition(Node node) {
        // Get the local bounds of the AnchorPane
        Bounds localBounds = node.getBoundsInLocal();

        // Transform the local bounds to scene coordinates
        Bounds sceneBounds = node.localToScene(localBounds);

        // Calculate the position of the left border
        return sceneBounds.getMinY();
    }

    protected void mouseDragged(MouseEvent event) {
        if (!dragging) {
            System.out.println("fjfjd "+getLeftBorderPosition(parent)+" lower y "+event.getX());
            double offsetX = event.getSceneX() - orgSceneX;
            double newTranslateX = orgTranslateX + offsetX;
            ((Node) (event.getSource())).setTranslateX(newTranslateX);
            return;
        }

        if (draggableZoneY) {
            double mousey = event.getY();

            double newHeight = region.getMinHeight() + (mousey - y);

            region.setMinHeight(newHeight);

            y = mousey;
        }

        if (draggableZoneX) {
            double mousex = event.getX();

            double newWidth = region.getMinWidth() + (mousex - x);

            region.setMinWidth(newWidth);
            x = mousex;

        }

        if (draggableZoneZ) {
            double deltaX = event.getSceneX() - dragStartX;
            // Adjust the width of the AnchorPane
            double newWidth = region.getWidth() - deltaX;
            region.setMinWidth(newWidth);

            double offsetX = event.getSceneX() - orgSceneX_Resize;
            double newTranslateX = orgTranslateX_Resize + offsetX;
            ((Node) (event.getSource())).setTranslateX(newTranslateX + 10);

            // Update the drag start position for the next drag event
            dragStartX = event.getSceneX();
        }
    }

    private double dragStartX;
    private double orgSceneX_Resize, orgTranslateX_Resize;

    protected void mousePressed(MouseEvent event) {
        // ignore clicks outside of the draggable margin
        if (!isInDraggableZone(event)) {
            orgSceneX = event.getSceneX();
            orgTranslateX = ((Node) (event.getSource())).getTranslateX();
            return;
        }

        dragging = true;

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinHeight) {
            region.setMinHeight(region.getHeight());
            initMinHeight = true;
        }

        y = event.getY();

        if (!initMinWidth) {
            region.setMinWidth(region.getWidth());
            initMinWidth = true;
        }

        x = event.getX();
        dragStartX = event.getSceneX();
        orgSceneX_Resize = event.getSceneX();
        orgTranslateX_Resize = ((Node) (event.getSource())).getTranslateX();
    }
}