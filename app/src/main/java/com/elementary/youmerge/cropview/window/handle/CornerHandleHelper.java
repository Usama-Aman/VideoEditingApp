package com.elementary.youmerge.cropview.window.handle;

import android.graphics.Rect;

import com.elementary.youmerge.cropview.window.edge.Edge;
import com.elementary.youmerge.cropview.window.edge.EdgePair;

class CornerHandleHelper extends HandleHelper {
    CornerHandleHelper(Edge horizontalEdge, Edge verticalEdge) {
        super(horizontalEdge, verticalEdge);
    }

    void updateCropWindow(float x, float y, float targetAspectRatio, Rect imageRect, float snapRadius) {
        EdgePair activeEdges = this.getActiveEdges(x, y, targetAspectRatio);
        Edge primaryEdge = activeEdges.primary;
        Edge secondaryEdge = activeEdges.secondary;
        primaryEdge.adjustCoordinate(x, y, imageRect, snapRadius, targetAspectRatio);
        secondaryEdge.adjustCoordinate(targetAspectRatio);
        if (secondaryEdge.isOutsideMargin(imageRect, snapRadius)) {
            secondaryEdge.snapToRect(imageRect);
            primaryEdge.adjustCoordinate(targetAspectRatio);
        }

    }
}