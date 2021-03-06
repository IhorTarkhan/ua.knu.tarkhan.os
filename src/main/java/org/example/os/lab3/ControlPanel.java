package org.example.os.lab3;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlPanel extends Frame {
    public final Button runButton = new Button("run");
    public final Button stepButton = new Button("step");
    public final Button resetButton = new Button("reset");
    public final Button exitButton = new Button("exit");
    public final List<Button> buttons;
    public final List<Label> labels;
    public final Label statusValueLabel = new Label("STOP", Label.LEFT);
    public final Label timeValueLabel = new Label("0", Label.LEFT);
    public final Label instructionValueLabel = new Label("NONE", Label.LEFT);
    public final Label addressValueLabel = new Label("NULL", Label.LEFT);
    public final Label pageFaultValueLabel = new Label("NO", Label.LEFT);
    public final Label virtualPageValueLabel = new Label("x", Label.LEFT);
    public final Label physicalPageValueLabel = new Label("0", Label.LEFT);
    public final Label RValueLabel = new Label("0", Label.LEFT);
    public final Label MValueLabel = new Label("0", Label.LEFT);
    public final Label inMemTimeValueLabel = new Label("0", Label.LEFT);
    public final Label lastTouchTimeValueLabel = new Label("0", Label.LEFT);
    public final Label lowValueLabel = new Label("0", Label.LEFT);
    public final Label highValueLabel = new Label("0", Label.LEFT);
    private final String commands;
    private final String config;
    private Kernel kernel;

    public ControlPanel(String title, String commands, String config) {
        super(title);
        setLayout(null);
        setBackground(Color.white);
        setForeground(Color.black);
        setSize(635, 545);
        setFont(new Font("Courier", Font.PLAIN, 12));

        this.commands = commands;
        this.config = config;

        runButton.setForeground(Color.blue);
        runButton.setBackground(Color.lightGray);
        runButton.setBounds(0, 25, 70, 15);
        add(runButton);

        stepButton.setForeground(Color.blue);
        stepButton.setBackground(Color.lightGray);
        stepButton.setBounds(70, 25, 70, 15);
        add(stepButton);

        resetButton.setForeground(Color.blue);
        resetButton.setBackground(Color.lightGray);
        resetButton.setBounds(140, 25, 70, 15);
        add(resetButton);

        exitButton.setForeground(Color.blue);
        exitButton.setBackground(Color.lightGray);
        exitButton.setBounds(210, 25, 70, 15);
        add(exitButton);

        buttons = IntStream.range(0, 64)
                .boxed()
                .map(i -> {
                    Button button = new Button("page " + i);
                    button.setBounds(i / 32 == 0 ? 0 : 140, (i % 32 + 2) * 15 + 25, 70, 15);
                    button.setForeground(Color.magenta);
                    button.setBackground(Color.lightGray);
                    add(button);
                    return button;
                })
                .collect(Collectors.toList());

        statusValueLabel.setBounds(345, 25, 100, 15);
        add(statusValueLabel);

        timeValueLabel.setBounds(345, 15 + 25, 100, 15);
        add(timeValueLabel);

        instructionValueLabel.setBounds(385, 45 + 25, 100, 15);
        add(instructionValueLabel);

        addressValueLabel.setBounds(385, 60 + 25, 230, 15);
        add(addressValueLabel);

        pageFaultValueLabel.setBounds(385, 90 + 25, 100, 15);
        add(pageFaultValueLabel);

        virtualPageValueLabel.setBounds(395, 120 + 25, 200, 15);
        add(virtualPageValueLabel);

        physicalPageValueLabel.setBounds(395, 135 + 25, 200, 15);
        add(physicalPageValueLabel);

        RValueLabel.setBounds(395, 150 + 25, 200, 15);
        add(RValueLabel);

        MValueLabel.setBounds(395, 165 + 25, 200, 15);
        add(MValueLabel);

        inMemTimeValueLabel.setBounds(395, 180 + 25, 200, 15);
        add(inMemTimeValueLabel);

        lastTouchTimeValueLabel.setBounds(395, 195 + 25, 200, 15);
        add(lastTouchTimeValueLabel);

        lowValueLabel.setBounds(395, 210 + 25, 230, 15);
        add(lowValueLabel);

        highValueLabel.setBounds(395, 225 + 25, 230, 15);
        add(highValueLabel);

        Label virtualOneLabel = new Label("virtual", Label.CENTER);
        virtualOneLabel.setBounds(0, 15 + 25, 70, 15);
        add(virtualOneLabel);

        Label virtualTwoLabel = new Label("virtual", Label.CENTER);
        virtualTwoLabel.setBounds(140, 15 + 25, 70, 15);
        add(virtualTwoLabel);

        Label physicalOneLabel = new Label("physical", Label.CENTER);
        physicalOneLabel.setBounds(70, 15 + 25, 70, 15);
        add(physicalOneLabel);

        Label physicalTwoLabel = new Label("physical", Label.CENTER);
        physicalTwoLabel.setBounds(210, 15 + 25, 70, 15);
        add(physicalTwoLabel);

        Label statusLabel = new Label("status: ", Label.LEFT);
        statusLabel.setBounds(285, 25, 65, 15);
        add(statusLabel);

        Label timeLabel = new Label("time: ", Label.LEFT);
        timeLabel.setBounds(285, 15 + 25, 50, 15);
        add(timeLabel);

        Label instructionLabel = new Label("instruction: ", Label.LEFT);
        instructionLabel.setBounds(285, 45 + 25, 100, 15);
        add(instructionLabel);

        Label addressLabel = new Label("address: ", Label.LEFT);
        addressLabel.setBounds(285, 60 + 25, 85, 15);
        add(addressLabel);

        Label pageFaultLabel = new Label("page fault: ", Label.LEFT);
        pageFaultLabel.setBounds(285, 90 + 25, 100, 15);
        add(pageFaultLabel);

        Label virtualPageLabel = new Label("virtual page: ", Label.LEFT);
        virtualPageLabel.setBounds(285, 120 + 25, 110, 15);
        add(virtualPageLabel);

        Label physicalPageLabel = new Label("physical page: ", Label.LEFT);
        physicalPageLabel.setBounds(285, 135 + 25, 110, 15);
        add(physicalPageLabel);

        Label RLabel = new Label("R: ", Label.LEFT);
        RLabel.setBounds(285, 150 + 25, 110, 15);
        add(RLabel);

        Label MLabel = new Label("M: ", Label.LEFT);
        MLabel.setBounds(285, 165 + 25, 110, 15);
        add(MLabel);

        Label inMemTimeLabel = new Label("inMemTime: ", Label.LEFT);
        inMemTimeLabel.setBounds(285, 180 + 25, 110, 15);
        add(inMemTimeLabel);

        Label lastTouchTimeLabel = new Label("lastTouchTime: ", Label.LEFT);
        lastTouchTimeLabel.setBounds(285, 195 + 25, 110, 15);
        add(lastTouchTimeLabel);

        Label lowLabel = new Label("low: ", Label.LEFT);
        lowLabel.setBounds(285, 210 + 25, 110, 15);
        add(lowLabel);

        Label highLabel = new Label("high: ", Label.LEFT);
        highLabel.setBounds(285, 225 + 25, 110, 15);
        add(highLabel);

        labels = IntStream.range(0, 64)
                .boxed()
                .map(i -> {
                    Label label = new Label(null, Label.CENTER);
                    label.setBounds(i / 32 == 0 ? 70 : 210, (i % 32 + 2) * 15 + 25, 60, 15);
                    label.setForeground(Color.red);
                    label.setFont(new Font("Courier", Font.PLAIN, 10));
                    add(label);
                    return label;
                })
                .collect(Collectors.toList());

        kernel = new Kernel(this, config, commands);
        setVisible(true);
    }


    public void reset() {
        statusValueLabel.setText("STOP");
        timeValueLabel.setText("0");
        instructionValueLabel.setText("NONE");
        addressValueLabel.setText("NULL");
        pageFaultValueLabel.setText("NO");
        virtualPageValueLabel.setText("x");
        physicalPageValueLabel.setText("0");
        RValueLabel.setText("0");
        MValueLabel.setText("0");
        inMemTimeValueLabel.setText("0");
        lastTouchTimeValueLabel.setText("0");
        lowValueLabel.setText("0");
        highValueLabel.setText("0");
        runButton.setEnabled(true);
        stepButton.setEnabled(true);
    }

    public boolean action(Event e, Object arg) {
        Button target = (Button) e.target;
        if (target == runButton) {
            kernel.start();
            return true;
        }
        if (target == stepButton) {
            kernel.step(false);
            return true;
        }
        if (target == resetButton) {
            kernel = new Kernel(this, config, commands);
            return true;
        }
        if (target == exitButton) {
            System.exit(0);
            return true;
        }
        if (buttons.contains(target)) {
            kernel.paintPage(kernel.getPage(buttons.indexOf(target)));
            return true;
        }
        return false;
    }
}
