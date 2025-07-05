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
        return layoutSize(target, true);
    }

    /**
     * Retorna o tamanho mínimo deste layout para o contêiner especificado.
     * @param target O contêiner para o qual este layout é executado.
     * @return O tamanho mínimo do layout.
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1); // Subtrai o espaçamento horizontal para um ajuste fino
        return minimum;
    }

    /**
     * Calcula o tamanho do layout.
     * @param target O contêiner.
     * @param preferred True para o tamanho preferido, false para o tamanho mínimo.
     * @return O tamanho calculado.
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;

            // Quando a janela está sendo criada, o tamanho do target pode ser 0.
            // Para evitar divisões por zero ou layouts estranhos, use um valor padrão.
            if (targetWidth == 0)
                targetWidth = Integer.MAX_VALUE;

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int verticalInsetsAndGap = insets.top + insets.bottom + (vgap * 2);

            int maxWidth = targetWidth - horizontalInsetsAndGap;

            // Calcular o tamanho dos componentes
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    if (rowWidth + d.width + hgap > maxWidth) {
                        // Nova linha
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = d.width + hgap;
                        rowHeight = d.height;
                    } else {
                        // Mesma linha
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
            }

            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight;

            return new Dimension(dim.width + horizontalInsetsAndGap, dim.height + verticalInsetsAndGap);
        }
    }
}