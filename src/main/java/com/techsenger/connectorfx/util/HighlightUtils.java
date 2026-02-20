package com.techsenger.connectorfx.util;

import com.techsenger.connectorfx.Highlight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;

/**
 *
 * @author Pavel Castornii
 */
public final class HighlightUtils {

    public static void updateBounds(Highlight.BoundsHighlight h, Rectangle rect) {
        if (h.getFill() != null) {
            rect.setFill(Color.web(h.getFill()));
        } else {
            rect.setFill(null);
        }

        if (h.getStroke() != null) {
            rect.setStroke(Color.web(h.getStroke()));
        } else {
            rect.setStroke(null);
        }
        updateShape(h, rect);
    }

    public static void updateBaseline(Highlight.BaselineHighlight h, Line line) {
        line.setStroke(Color.web(h.getStroke()));
        updateShape(h, line);
    }

    private static void updateShape(Highlight.AbstractHighlight h, Shape shape) {
        shape.setStrokeType(StrokeType.valueOf(h.getStrokeType()));

        shape.getStrokeDashArray().clear();
        for (var d : h.getStrokeDashArray()) {
            shape.getStrokeDashArray().add(d);
        }
        shape.setStrokeDashOffset(h.getStrokeDashOffset());
        shape.setStrokeLineCap(StrokeLineCap.valueOf(h.getStrokeLineCap()));
        shape.setStrokeLineJoin(StrokeLineJoin.valueOf(h.getStrokeLineJoin()));
        shape.setStrokeMiterLimit(h.getStrokeMiterLimit());

        shape.setStrokeWidth(h.getStrokeWidth());
        shape.setOpacity(h.getOpacity());
    }

    private HighlightUtils() {
        // empty
    }
}
