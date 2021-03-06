/*
 * This source file is part of the Máni open source project
 *
 * Copyright (c) 2018 - 2019.
 *
 * Licensed under Mozilla Public License 2.0
 *
 * See https://github.com/mani-language/Mani/blob/master/LICENSE.md for license information
 */

package com.mani.lang.Modules.frame;

import com.mani.lang.core.Interpreter;
import com.mani.lang.domain.ManiCallable;
import com.mani.lang.Modules.Module;
import com.mani.lang.domain.ManiCallableInternal;
import com.mani.lang.domain.ManiFunction;
import com.mani.lang.exceptions.GeneralError;
import com.mani.lang.main.Std;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class frame implements Module {

    private static JFrame frame;
    private static CanvasPanel panel;
    private static Graphics2D graphics;
    private static BufferedImage img;

    private static double lastKey;
    private static List<Double> mouseHover;

    private void registerKeys(Interpreter interpreter) {
        interpreter.define("KEY_UP", Std.makeDouble(KeyEvent.VK_UP));
        interpreter.define("KEY_DOWN", Std.makeDouble(KeyEvent.VK_DOWN));
        interpreter.define("KEY_LEFT", Std.makeDouble(KeyEvent.VK_LEFT));
        interpreter.define("KEY_RIGHT", Std.makeDouble(KeyEvent.VK_RIGHT));
        interpreter.define("KEY_FIRE", Std.makeDouble(KeyEvent.VK_ENTER));
        interpreter.define("KEY_ESCAPE", Std.makeDouble(KeyEvent.VK_ESCAPE));
    }

    @Override
    public void init(Interpreter interpreter) {
        registerKeys(interpreter);
        mouseHover = new ArrayList<>();
        mouseHover.add(0.00);
        mouseHover.add(0.00);

        interpreter.addSTD("window", new CreateWindow());
        interpreter.addSTD("windowVis", new setVisibility());
        interpreter.addSTD("windowButton", new windowButton());
        interpreter.addSTD("keyPressed", new keyPressed());
        interpreter.addSTD("windowRepaint", new windowRepaint());
        interpreter.addSTD("windowPrompt", new windowPrompt());
        interpreter.addSTD("mouseHover", new MouseHover());
        interpreter.addSTD("drawstring", new DrawString());
        interpreter.addSTD("line", intConsumer4Convert(com.mani.lang.Modules.frame.frame::line));
        interpreter.addSTD("oval", intConsumer4Convert(com.mani.lang.Modules.frame.frame::oval));
        interpreter.addSTD("foval", intConsumer4Convert(com.mani.lang.Modules.frame.frame::foval));
        interpreter.addSTD("rect", intConsumer4Convert(com.mani.lang.Modules.frame.frame::rect));
        interpreter.addSTD("frect", intConsumer4Convert(com.mani.lang.Modules.frame.frame::frect));
        interpreter.addSTD("clip", intConsumer4Convert(com.mani.lang.Modules.frame.frame::clip));
        interpreter.addSTD("color", new setColor());
    }

    @FunctionalInterface
    private interface IntConsumer4 {
        void accept(int i1, int i2, int i3, int i4);
    }
    
    private static ManiCallable intConsumer4Convert(IntConsumer4 consumer) {
        return new ManiCallable() {
            @Override
            public int arity() { return 4; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                consumer.accept(((Double) arguments.get(0)).intValue() , ((Double) arguments.get(1)).intValue(), ((Double) arguments.get(2)).intValue(), ((Double) arguments.get(3)).intValue());
                return (double) 0;
            }
        };
    }

    private static class setColor implements ManiCallable {

        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (arguments.size() > 1) {
                int r = Std.DoubleToInt((double) arguments.get(0));
                int g = Std.DoubleToInt((double) arguments.get(1));
                int b = Std.DoubleToInt((double) arguments.get(2));
                graphics.setColor(new Color(r, g, b));
                return null;
            }
            graphics.setColor(new Color(Std.DoubleToInt((double) arguments.get(0))));
            return null;
        }
    }

    private static void line(int x1, int y1, int x2, int y2) { graphics.drawLine(x1, y1, x2, y2); }
    private static void oval(int x, int y, int w, int h) { graphics.fillOval(x, y, w, h); }
    private static void foval(int x, int y, int w, int h) { graphics.fillOval(x, y, w, h); }
    private static void rect(int x, int y, int w, int h) { graphics.drawRect(x, y, w, h); }
    private static void frect(int x, int y, int w, int h) { graphics.fillRect(x, y, w, h); }
    private static void clip(int x, int y, int w, int h) { graphics.setClip(x, y, w, h); }

    private static class CreateWindow implements ManiCallable {

        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
			String title = "";
            double width = 1000;
            double height = 700;
            switch (arguments.size()) {
                case 1:

                    // TITLE
                    if (!(arguments.get(0) instanceof String)) { return "Please make sure argument is a string"; }
                    title = arguments.get(0).toString();
                    break;
                case 2:

                    // WIDTH, HEIGHT
                    if (!(arguments.get(0) instanceof Double || arguments.get(1) instanceof Double)) {
                        return "Both arguments must be a number, width and height";
                    }
                    width = (double) arguments.get(0);
                    height = (double) arguments.get(1);
                    break;
                case 3:

                    // TITLE, WIDTH, HEIGHT
                    if (!(arguments.get(0) instanceof String || arguments.get(1) instanceof Double || arguments.get(2) instanceof Double)) {
                        return "First argument must be a String, Other 2 must be a number. Width and height.";
                    }
                    title = arguments.get(0).toString();
                    width = (double) arguments.get(1);
                    height = (double) arguments.get(2);
                    break;
            }
            panel = new CanvasPanel(new Double(width).intValue(), new Double(height).intValue());
            frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
            return null;
		}
    }

    private static class setVisibility implements ManiCallable {

        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (arguments.size() != 1 || arguments.size() == 1 && !(arguments.get(1) instanceof Boolean)) {
                return "Argument must be a boolean. Use the current visibility status, true if displaying, false if not.";
            }
            frame.setVisible((boolean) arguments.get(0) ? false : true );
            return null;
        }
    }

    private static class keyPressed implements ManiCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return lastKey;
        }
    }

    private static class MouseHover implements ManiCallable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return mouseHover;
        }
    }

    private static class windowButton implements ManiCallable {

        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (arguments.size() < 1) {
                return "Please provide at least a button name.";
            }
            JButton b = new JButton(arguments.get(0).toString());
            if (arguments.size() == 5) {
                int x = Std.DoubleToInt((double) arguments.get(1));
                int y = Std.DoubleToInt((double) arguments.get(2));
                int w = Std.DoubleToInt((double) arguments.get(3));
                int h = Std.DoubleToInt((double) arguments.get(4));
                b.setBounds(x, y, w, h);
            }
            panel.add(b);
            return b;
        }

    }

    private static class windowPrompt implements ManiCallable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            final String v = JOptionPane.showInputDialog(arguments.get(0).toString());
            return (v == null ? null : v);
        }
    }

    private static class windowRepaint implements ManiCallable {

        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            panel.invalidate();
            panel.repaint();
            return null;
        }
    }

    private static class DrawString implements ManiCallable {

        @Override
        public int arity() {
            return 3;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {

            int x = Std.DoubleToInt((Double) arguments.get(1));
            int y = Std.DoubleToInt((Double) arguments.get(2));
            graphics.drawString(String.valueOf( arguments.get(0) ), x, y);

            return null;
        }
    }

    private static class CanvasPanel extends JPanel {
        public CanvasPanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            graphics = img.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            setFocusable(true);
            requestFocus();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    lastKey = Double.valueOf(e.getKeyCode());
                }
                @Override
                public void keyReleased(KeyEvent e) {
                    lastKey = -1d;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseHover.set(0, Double.valueOf(e.getX()));
                    mouseHover.set(1, Double.valueOf(e.getY()));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, null);
        }
    }

    @Override
    public boolean hasExtensions() {
        return true;
    }

    @Override
    public Object extensions() {
        HashMap<String, HashMap<String, ManiCallableInternal>> db = new HashMap<>();
        HashMap<String, ManiCallableInternal> localsBtn = new HashMap<>();
        HashMap<String, ManiCallableInternal> localsFrame = new HashMap<>();

        /** Buttons **/
        localsBtn.put("del", new ManiCallableInternal() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                frame.remove(((JButton) this.workWith));
                return true;
            }
        });

        localsBtn.put("vis", new ManiCallableInternal() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (!(arguments.get(0) instanceof Boolean)) {
                    throw new GeneralError("Must be a boolean");
                }
                ((JButton) this.workWith).setVisible(((Boolean) arguments.get(0)));
                return ((JButton) this.workWith).isVisible();
            }
        });

        localsBtn.put("listener", new ManiCallableInternal() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                ((JButton) this.workWith).addMouseListener(new CustomMouseListener(interpreter, arguments.get(0)));
                return null;

            }
        });


        db.put("button", localsBtn);
        db.put("frame", localsFrame);
        return db;
    }

    private static class CustomMouseListener implements MouseListener {

        Interpreter interpreter;
        Object obj;


        public CustomMouseListener(Interpreter interpreter, Object obj) {
            this.interpreter = interpreter;
            this.obj = obj;
        }
        public void mouseClicked(MouseEvent e) {
            ManiFunction run = (ManiFunction) obj;
            List<Object> args = new ArrayList<>();
            run.call(interpreter, args);
        }
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
    }
}
