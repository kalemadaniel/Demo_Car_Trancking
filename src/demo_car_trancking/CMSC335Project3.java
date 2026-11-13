package demo_car_trancking;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Original traffic tracker by John Kaiser, revised for reliable Swing operation. */
public final class CMSC335Project3 extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Simulation simulation = new Simulation();
    private final Time clock = new Time();
    private final JLabel status = new JLabel();
    private final JButton start = new JButton("Démarrer");
    private final JButton pause = new JButton("Pause");
    private final JButton stop = new JButton("Arrêter / réinitialiser");
    private final DefaultTableModel data = new DefaultTableModel(new String[]{"Véhicule", "X (m)", "Y (m)", "Vitesse (km/h)", "Retard (s)", "À l’arrêt (s)", "File au carrefour"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel intersections = new DefaultTableModel(new String[]{"Carrefour", "Feu", "File actuelle", "File maximale", "Retard cumulé (véh·s)"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final RoadPanel road = new RoadPanel();
    private final Timer timer;
    private final JPanel laneButtons = new JPanel(null);
    private int displayedLanes = -1;
    private long lastTick;

    public CMSC335Project3() {
        super("Simulation de circulation routière");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(start); controls.add(pause); controls.add(stop);
        JButton addCar = new JButton("Ajouter une ligne");
        JButton addLight = new JButton("Ajouter un carrefour");
        controls.add(addCar); controls.add(addLight);
        JButton settings = new JButton("Configurer les carrefours");
        settings.addActionListener(e -> configureLights());
        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsRow.add(settings);
        JSpinner speed = new JSpinner(new SpinnerNumberModel(50, 1, 180, 5));
        controls.add(new JLabel("Vitesse cible (km/h) :")); controls.add(speed);
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(status, BorderLayout.NORTH); header.add(controls, BorderLayout.CENTER); header.add(settingsRow, BorderLayout.SOUTH);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        add(header, BorderLayout.NORTH);
        JTabbedPane tables = new JTabbedPane();
        tables.addTab("Véhicules", new JScrollPane(new JTable(data)));
        tables.addTab("Files et retards par carrefour", new JScrollPane(new JTable(intersections)));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, roadScroll(), tables);
        split.setResizeWeight(0.65); add(split, BorderLayout.CENTER);
        JLabel note = new JLabel("  Une ligne = une voie sans dépassement • espacement minimal : 7 m • retard = temps perdu par rapport à la vitesse cible • pause exclue");
        note.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4)); add(note, BorderLayout.SOUTH);
        start.addActionListener(e -> { simulation.start(); lastTick = System.nanoTime(); refresh(); });
        pause.addActionListener(e -> { simulation.pause(); refresh(); });
        stop.addActionListener(e -> { simulation.stop(); refresh(); });
        addCar.addActionListener(e -> {
            try { simulation.addLane(); refresh(); }
            catch (IllegalStateException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Route occupée", JOptionPane.INFORMATION_MESSAGE); }
        });
        addLight.addActionListener(e -> { simulation.addIntersection(); refresh(); });
        speed.addChangeListener(e -> simulation.setSpeedKmh(((Number) speed.getValue()).doubleValue()));
        timer = new Timer(50, e -> {
            long now = System.nanoTime();
            // Limit catch-up after a blocked UI or system sleep.
            simulation.advance(Math.min((now - lastTick) / 1_000_000_000.0, 0.25));
            lastTick = now;
            refresh();
        });
        refresh(); setSize(1080, 650); setLocationRelativeTo(null);
        lastTick = System.nanoTime(); timer.start();
    }

    @Override public void dispose() {
        if (timer != null) timer.stop();
        simulation.stop();
        super.dispose();
    }

    private void refresh() {
        Simulation.State state = simulation.getState();
        String label = state == Simulation.State.RUNNING ? "En cours" : state == Simulation.State.PAUSED ? "En pause" : "À l’arrêt";
        status.setText(String.format("Heure : %s | Simulation : %.1f s | %s | Retard total : %.1f véh·s | Moyenne : %.1f s/véhicule", clock.getTime(), simulation.getElapsed(), label,
                simulation.getTotalDelay(), simulation.getCars().isEmpty() ? 0 : simulation.getTotalDelay() / simulation.getCars().size()));
        start.setText(state == Simulation.State.PAUSED ? "Reprendre" : "Démarrer");
        start.setEnabled(state != Simulation.State.RUNNING);
        pause.setEnabled(state == Simulation.State.RUNNING);
        stop.setEnabled(state != Simulation.State.STOPPED);
        data.setRowCount(simulation.getCars().size());
        for (int i = 0; i < simulation.getCars().size(); i++) {
            Car car = simulation.getCars().get(i);
            data.setValueAt(car.getName() + " (ligne " + (car.getLane() + 1) + ")", i, 0); data.setValueAt(Math.round(car.getPosition()), i, 1);
            data.setValueAt(0, i, 2); data.setValueAt(Math.round(car.getSpeed()), i, 3);
            data.setValueAt(String.format("%.1f", car.getDelaySeconds()), i, 4);
            data.setValueAt(String.format("%.1f", car.getStoppedSeconds()), i, 5);
            data.setValueAt(car.getWaitingAt() < 0 ? "—" : simulation.getLights().get(car.getWaitingAt()).getName(), i, 6);
        }
        intersections.setRowCount(simulation.getLights().size());
        for (int i = 0; i < simulation.getLights().size(); i++) {
            Intersection light = simulation.getLights().get(i);
            intersections.setValueAt(light.getName() + " — " + light.getPosition() + " m", i, 0);
            intersections.setValueAt(light.getColor(), i, 1);
            intersections.setValueAt(light.getQueueLength(), i, 2);
            intersections.setValueAt(light.getMaxQueueLength(), i, 3);
            intersections.setValueAt(String.format("%.1f", light.getDelaySeconds()), i, 4);
        }
        Dimension size = new Dimension(Math.max(850, simulation.getLights().size() * 180), Math.max(250, 90 + simulation.getLaneCount() * 40));
        if (!size.equals(road.getPreferredSize())) { road.setPreferredSize(size); road.revalidate(); }
        if (displayedLanes != simulation.getLaneCount()) {
            displayedLanes = simulation.getLaneCount(); laneButtons.removeAll();
            for (int lane = 0; lane < displayedLanes; lane++) {
                final int selected = lane;
                JButton add = new JButton("Ligne " + (lane + 1) + " : + véhicule");
                add.setBounds(0, 85 + lane * 40, 175, 30);
                add.addActionListener(e -> {
                    try { simulation.addCar(selected); refresh(); }
                    catch (IllegalStateException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
                });
                laneButtons.add(add);
            }
        }
        laneButtons.setPreferredSize(new Dimension(180, size.height)); laneButtons.revalidate(); laneButtons.repaint();
        road.repaint();
    }

    private JScrollPane roadScroll() {
        JScrollPane scroll = new JScrollPane(road);
        scroll.setRowHeaderView(laneButtons);
        return scroll;
    }

    private void configureLights() {
        if (simulation.getState() != Simulation.State.STOPPED) {
            JOptionPane.showMessageDialog(this, "Cliquez sur Arrêter / réinitialiser avant de modifier les carrefours."); return;
        }
        JDialog dialog = new JDialog(this, "Paramètres des carrefours", true);
        JPanel rows = new JPanel(new GridLayout(0, 5, 8, 8));
        rows.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (String title : new String[]{"Carrefour", "Nom", "Depuis départ (m)", "Rouge (secondes)", ""}) rows.add(new JLabel(title));
        for (int i = 0; i < simulation.getLights().size(); i++) {
            final int index = i;
            Intersection light = simulation.getLights().get(i);
            JTextField name = new JTextField(light.getName());
            JSpinner metres = new JSpinner(new SpinnerNumberModel(light.getPosition(), 1, 10000000, 10));
            JSpinner seconds = new JSpinner(new SpinnerNumberModel(light.getRedSeconds(), 1, 3600, 1));
            JButton apply = new JButton("Appliquer");
            apply.addActionListener(e -> {
                try {
                    metres.commitEdit(); seconds.commitEdit();
                    simulation.configureIntersection(index, name.getText(), ((Number)metres.getValue()).intValue(), ((Number)seconds.getValue()).intValue());
                    refresh(); apply.setText("Enregistré");
                } catch (IllegalArgumentException | IllegalStateException | java.text.ParseException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Paramètres invalides", JOptionPane.WARNING_MESSAGE);
                }
            });
            rows.add(new JLabel("C" + (i + 1))); rows.add(name); rows.add(metres); rows.add(seconds); rows.add(apply);
        }
        dialog.add(new JScrollPane(rows)); dialog.setSize(850, 300); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private final class RoadPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 20, width = Math.max(1, getWidth() - left - 80);
                double length = simulation.getRoadLength();
                for (Intersection light : simulation.getLights()) {
                    int x = left + (int) (width * light.getPosition() / length);
                    g.setColor(Color.LIGHT_GRAY); g.drawLine(x, 65, x, getHeight() - 10);
                    g.setColor(light.isRed() ? new Color(190, 35, 35) : light.getColor().equals("Vert") ? new Color(0, 125, 70) : new Color(170, 100, 0));
                    g.fillOval(x - 7, 12, 14, 14); g.drawString(light.getColor(), x - 22, 42);
                    g.drawString(light.getName(), x - 30, 74);
                    g.drawString(light.getPosition() + " m", x - 25, 58);
                }
                for (int lane = 0; lane < simulation.getLaneCount(); lane++) {
                    int y = 100 + lane * 40;
                    g.setColor(new Color(210, 215, 220)); g.fillRect(left, y - 7, width, 14);
                }
                for (Car car : simulation.getCars()) {
                    int y = 100 + car.getLane() * 40;
                    int x = left + (int) (width * car.getPosition() / length);
                    g.setColor(car.getWaitingAt() >= 0 ? new Color(190, 70, 25) : new Color(35, 95, 180));
                    g.fillRoundRect(x - 5, y - 8, 10, 16, 4, 4);
                }
            } finally { g.dispose(); }
        }
    }

    static void installNimbus() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            System.err.println("Nimbus indisponible : utilisation du thème Swing par défaut.");
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { installNimbus(); new CMSC335Project3().setVisible(true); });
    }
}

