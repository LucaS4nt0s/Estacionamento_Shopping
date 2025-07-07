package estacionamento.UI;

import java.awt.*;

// Um layout manager que estende FlowLayout mas com melhor controle de quebra de linha
public class WrapLayout extends FlowLayout {

    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * Retorna o tamanho preferido deste layout para o contêiner especificado.
     * @param target O contêiner para o qual este layout é executado.
     * @return O tamanho preferido do layout.
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        // Use the current width of the target if available, otherwise a very large number.
        // This ensures preferredLayoutSize doesn't force wrapping prematurely for ideal sizing,
        // and actual wrapping is handled by layoutContainer using the actual width.
        int targetWidth = target.getWidth();
        if (targetWidth == 0) {
            targetWidth = Integer.MAX_VALUE; // If width is not yet determined, assume infinite width
        }
        return layoutSize(target, true, targetWidth);
    }

    /**
     * Retorna o tamanho mínimo deste layout para o contêiner especificado.
     * @param target O contêiner para o qual este layout é executado.
     * @return O tamanho mínimo do layout.
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        // For minimum size, we still consider the current width as a hint, but it's often more about component minimums.
        Dimension minimum = layoutSize(target, false, target.getWidth());
        minimum.width -= (getHgap() + 1); // Subtrai o espaçamento horizontal para um ajuste fino
        return minimum;
    }

    /**
     * Calcula o tamanho do layout.
     * @param target O contêiner.
     * @param preferred True para o tamanho preferido, false para o tamanho mínimo.
     * @param targetWidth The width to use for calculations. This represents the available width for wrapping.
     * @return O tamanho calculado.
     */
    private Dimension layoutSize(Container target, boolean preferred, int targetWidth) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (getHgap() * 2);
            int verticalInsetsAndGap = insets.top + insets.bottom + (getVgap() * 2);

            int maxWidth = targetWidth - horizontalInsetsAndGap;
            if (maxWidth < 0) maxWidth = 0; // Ensure maxWidth is not negative

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    // Check if adding the current component would exceed the maxWidth for the current row.
                    // Also check if rowWidth is greater than 0 to avoid wrapping an empty first row.
                    if (rowWidth + d.width + getHgap() > maxWidth && rowWidth > 0) {
                        // Start a new line
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + getVgap();
                        rowWidth = d.width + getHgap();
                        rowHeight = d.height;
                    } else {
                        // Continue on the same line
                        rowWidth += d.width + getHgap();
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

            return new Dimension(dim.width + horizontalInsetsAndGap, dim.height + verticalInsetsAndGap);
        }
    }

    /**
     * Posiciona os componentes dentro do contêiner.
     * This method is crucial for the actual visual wrapping behavior.
     */
    @Override
    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        // Calculate the actual available width for layout
        int maxWidth = target.getWidth() - (insets.left + insets.right + getHgap() * 2);
        if (maxWidth < 0) maxWidth = 0; // Ensure maxWidth is not negative

        int x = insets.left + getHgap();
        int y = insets.top + getVgap();
        int rowHeight = 0;

        for (int i = 0; i < target.getComponentCount(); i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                Dimension d = m.getPreferredSize();

                // Check if the component needs to wrap to a new line
                if (x + d.width + getHgap() > maxWidth && x > (insets.left + getHgap())) {
                    x = insets.left + getHgap(); // Reset x to the start of the new line
                    y += rowHeight + getVgap(); // Move y down by the height of the previous row
                    rowHeight = 0; // Reset row height for the new row
                }

                // Set component bounds (position and size)
                m.setBounds(x, y, d.width, d.height);
                x += d.width + getHgap(); // Move x for the next component
                rowHeight = Math.max(rowHeight, d.height); // Update the maximum height in the current row
            }
        }
    }
}