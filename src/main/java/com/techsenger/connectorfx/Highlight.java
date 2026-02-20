
package com.techsenger.connectorfx;

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Contains the highlighting visual parameters.
 *
 * @author Pavel Castornii
 */
@NullMarked
public final class Highlight {

    public abstract static class AbstractHighlight {
        private final String stroke;
        private final String strokeType;
        private final List<Double> strokeDashArray;
        private final double strokeDashOffset;
        private final String strokeLineCap;
        private final String strokeLineJoin;
        private final double strokeMiterLimit;
        private final double strokeWidth;
        private final double opacity;

        private AbstractHighlight(AbstractBuilder<?> builder) {
            this.stroke = builder.stroke;
            this.strokeType = builder.strokeType;
            this.strokeDashArray = builder.strokeDashArray;
            this.strokeDashOffset = builder.strokeDashOffset;
            this.strokeLineCap = builder.strokeLineCap;
            this.strokeLineJoin = builder.strokeLineJoin;
            this.strokeMiterLimit = builder.strokeMiterLimit;
            this.strokeWidth = builder.strokeWidth;
            this.opacity = builder.opacity;
        }

        public String getStroke() {
            return stroke;
        }

        public String getStrokeType() {
            return strokeType;
        }

        public List<Double> getStrokeDashArray() {
            return strokeDashArray;
        }

        public double getStrokeDashOffset() {
            return strokeDashOffset;
        }

        public String getStrokeLineCap() {
            return strokeLineCap;
        }

        public String getStrokeLineJoin() {
            return strokeLineJoin;
        }

        public double getStrokeMiterLimit() {
            return strokeMiterLimit;
        }

        public double getStrokeWidth() {
            return strokeWidth;
        }

        public double getOpacity() {
            return opacity;
        }

        protected String fieldsToString() {
            return "stroke=" + stroke
                    + ", strokeType=" + strokeType
                    + ", strokeDashArray=" + strokeDashArray
                    + ", strokeDashOffset=" + strokeDashOffset
                    + ", strokeLineCap=" + strokeLineCap
                    + ", strokeLineJoin=" + strokeLineJoin
                    + ", strokeMiterLimit=" + strokeMiterLimit
                    + ", strokeWidth=" + strokeWidth
                    + ", opacity=" + opacity;
        }
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private String stroke;
        private String strokeType = "CENTERED";
        private List<Double> strokeDashArray = Collections.emptyList();
        private double strokeDashOffset = 0.0;
        private String strokeLineCap = "SQUARE";
        private String strokeLineJoin = "MITER";
        private double strokeMiterLimit = 10.0;
        private double strokeWidth = 1.0;
        private double opacity = 1.0;

        private AbstractBuilder() {

        }

        public T stroke(@Nullable String stroke) {
            this.stroke = stroke;
            return (T) this;
        }

        public T strokeType(String strokeType) {
            this.strokeType = strokeType;
            return (T) this;
        }

        public T strokeDashArray(List<Double> strokeDashArray) {
            this.strokeDashArray = List.copyOf(strokeDashArray);
            return (T) this;
        }

        public T strokeDashOffset(double strokeDashOffset) {
            this.strokeDashOffset = strokeDashOffset;
            return (T) this;
        }

        public T strokeLineCap(String strokeLineCap) {
            this.strokeLineCap = strokeLineCap;
            return (T) this;
        }

        public T strokeLineJoin(String strokeLineJoin) {
            this.strokeLineJoin = strokeLineJoin;
            return (T) this;
        }

        public T strokeMiterLimit(double strokeMiterLimit) {
            this.strokeMiterLimit = strokeMiterLimit;
            return (T) this;
        }

        public T strokeWidth(double strokeWidth) {
            this.strokeWidth = strokeWidth;
            return (T) this;
        }

        public T opacity(double opacity) {
            this.opacity = opacity;
            return (T) this;
        }

        public abstract AbstractHighlight build();

        protected String getStroke() {
            return stroke;
        }
    }

    public static final class BoundsHighlight extends AbstractHighlight {

        public static BoundsBuilder builder() {
            return new BoundsBuilder();
        }

        private final String fill;

        private BoundsHighlight(BoundsBuilder builder) {
            super(builder);
            this.fill = builder.fill;
        }

        public String getFill() {
            return fill;
        }

        @Override
        public String toString() {
            return "BoundsHighlight{fill=" + fill + ", " + fieldsToString() + "}";
        }
    }

    public static final class BoundsBuilder extends AbstractBuilder<BoundsBuilder> {
        private String fill;

        private BoundsBuilder() {
            // empty
        }

        public BoundsBuilder fill(@Nullable String fill) {
            this.fill = fill;
            return this;
        }

        @Override
        public BoundsHighlight build() {
            return new BoundsHighlight(this);
        }
    }

    public static final class BaselineHighlight extends AbstractHighlight {

        public static BaselineBuilder builder() {
            return new BaselineBuilder();
        }

        private BaselineHighlight(BaselineBuilder builder) {
            super(builder);
        }

        @Override
        public String toString() {
            return "BaselineHighlight{" + fieldsToString() + "}";
        }
    }

    public static final class BaselineBuilder extends AbstractBuilder<BaselineBuilder> {

        private BaselineBuilder() {
            // empty
        }

        @Override
        public BaselineHighlight build() {
            if (getStroke() == null) {
                throw new IllegalStateException("Stroke must be set for BaselineHighlight");
            }
            return new BaselineHighlight(this);
        }

        @Override
        public BaselineBuilder stroke(String stroke) { // not nullable
            super.stroke(stroke);
            return this;
        }
    }

    public static Highlight defaults() {
        var inspectBounds = BoundsHighlight.builder()
                .fill("#008000")
                .opacity(0.5)
                .build();
        var layoutBounds = BoundsHighlight.builder()
                .fill("#FFFF00")
                .opacity(0.5)
                .build();
        var boundsInParent = BoundsHighlight.builder()
                .stroke("#00FF00")
                .strokeType("INSIDE")
                .opacity(0.8)
                .strokeDashArray(List.of(3.0, 3.0))
                .build();

        var baseline = BaselineHighlight.builder()
                .stroke("#FF0000")
                .opacity(0.75)
                .build();
        return new Highlight(inspectBounds, layoutBounds, boundsInParent, baseline);
    }

    private final BoundsHighlight inspectBounds;
    private final BoundsHighlight layoutBounds;
    private final BoundsHighlight boundsInParent;
    private final BaselineHighlight baseline;

    public Highlight(BoundsHighlight inspectBounds, BoundsHighlight layoutBounds, BoundsHighlight boundsInParent,
            BaselineHighlight baseline) {
        this.inspectBounds = inspectBounds;
        this.layoutBounds = layoutBounds;
        this.boundsInParent = boundsInParent;
        this.baseline = baseline;
    }

    public BoundsHighlight getInspectBounds() {
        return inspectBounds;
    }

    public BoundsHighlight getLayoutBounds() {
        return layoutBounds;
    }

    public BoundsHighlight getBoundsInParent() {
        return boundsInParent;
    }

    public BaselineHighlight getBaseline() {
        return baseline;
    }
}
