package com.kz.pipeCutter.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.Random;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.google.common.collect.Sets;


public class MyVerticalFlowLayout implements LayoutManager2
{
    final private Set<Component> components = Sets.newLinkedHashSet();
    private int hgap = 0;
    private int vgap = 0;

    public void setHGap(int hgap) { this.hgap = hgap; }
    public void setVGap(int vgap) { this.vgap = vgap; }

    @Override public void addLayoutComponent(Component comp, Object constraints) {
        this.components.add(comp);
    }

    /* these 3 methods need to be overridden properly */
    @Override public float getLayoutAlignmentX(Container target) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override public float getLayoutAlignmentY(Container target) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override public void invalidateLayout(Container target) {
        // TODO Auto-generated method stub

    }


    @Override public void addLayoutComponent(String name, Component comp) {
        this.components.add(comp);
    }

    @Override public void layoutContainer(Container parent) {
        int x = 0;
        int y = 0;
        int columnWidth = 0;
        for (Component c : this.components)
        {
            if (c.isVisible())
            {
                Dimension d = c.getPreferredSize();
                columnWidth = Math.max(columnWidth, d.width);
                if (y+d.height > parent.getHeight())
                {
                    x += columnWidth + this.hgap;
                    y = 0;
                }
                c.setBounds(x, y, d.width, d.height);
                y += d.height + this.vgap;              
            }
        }       
    }

    /* these 3 methods need to be overridden properly */
    @Override public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0,0);
    }

    @Override public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(200,200);
    }

    @Override public Dimension maximumLayoutSize(Container target) {
        return new Dimension(600,600);
    }


    @Override public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("VerticalFlowLayoutTest");    
        MyVerticalFlowLayout vfl = new MyVerticalFlowLayout();
        JPanel panel = new JPanel(vfl);
        vfl.setHGap(20);
        vfl.setVGap(2);
        int n = 19;
        Random r = new Random(12345);
        for (int i = 0; i < n; ++i)
        {
            JLabel label = new JLabel(labelName(i,r));
            panel.add(label);
        }

        frame.setContentPane(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static String labelName(int i, Random r) {
        StringBuilder sb = new StringBuilder();
        sb.append("label #");
        sb.append(i);
        sb.append(" ");
        int n = r.nextInt(26);
        for (int j = 0; j < n; ++j)
            sb.append("_");
        return sb.toString();
    }
}