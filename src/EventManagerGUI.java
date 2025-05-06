import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class EventManagerGUI extends JFrame {
    private final EventManagerApp app;
    // Colors
    private final Color BACKGROUND_COLOR = new Color(240, 255, 240); // Light green background
    private final Color PRIMARY_COLOR = new Color(34, 139, 34); // Forest green
    private final Color ACCENT_COLOR = new Color(0, 128, 0); // Green
    private final Color TEXT_COLOR = Color.black; // White text for buttons
    private final Color TABLE_HEADER_COLOR = new Color(50, 205, 50); // Lime green
    private final Color TABLE_ROW_COLOR = new Color(240, 255, 240); // Honeydew
    private final Color TABLE_ALT_ROW_COLOR = new Color(220, 255, 220); // Light green
    private final Color TAB_COLOR = new Color(144, 238, 144); // Light green tab color
    private final Color DIALOG_BACKGROUND = new Color(240, 255, 240); // Mint cream
    private final JTextArea outputArea;
    private final JTable eventTable;
    private final EventTableModel tableModel;

    public EventManagerGUI() {
        app = new EventManagerApp();
        app.loadEvents();

        setTitle("Event Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create menu bar
        createMenuBar();

        // Create components
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(DIALOG_BACKGROUND);
        outputArea.setForeground(Color.BLACK);
        outputArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        // Create table with colorful styling
        tableModel = new EventTableModel(app.getAllEvents());
        eventTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW_COLOR : TABLE_ALT_ROW_COLOR);
                }
                return c;
            }
        };

        eventTable.setAutoCreateRowSorter(true);
        eventTable.setBackground(TABLE_ROW_COLOR);
        eventTable.setForeground(Color.BLACK);
        eventTable.setSelectionBackground(PRIMARY_COLOR);
        eventTable.setSelectionForeground(TEXT_COLOR);
        eventTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        eventTable.setGridColor(PRIMARY_COLOR);
        eventTable.setShowGrid(true);
        eventTable.setRowHeight(25);

        // Style table header
        JTableHeader header = eventTable.getTableHeader();
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane tableScrollPane = new JScrollPane(eventTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        // Create tabbed pane with green styling
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(TAB_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(PRIMARY_COLOR);
                } else {
                    g.setColor(TAB_COLOR);
                }
                g.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(PRIMARY_COLOR);
                g.drawRect(x, y, w, h);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(PRIMARY_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        });

        tabbedPane.setBackground(TAB_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabbedPane.addTab("Table View", tableScrollPane);
        tabbedPane.addTab("Text View", scrollPane);

        // Create button panel with vibrant green buttons
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] buttonLabels = {"Manage Events", "Manage Attendees", "View All Events", "View Event Attendees", "Filter by Type", "Save Events", "Exit"};

        for (String label : buttonLabels) {
            JButton button = createStyledButton(label);
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        // Add components to frame
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        refreshEventTable();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));

        // File menu
        JMenu fileMenu = createStyledMenu("File");
        JMenuItem saveItem = createStyledMenuItem("Save Events");
        saveItem.addActionListener(e -> saveEvents());
        JMenuItem exitItem = createStyledMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = createStyledMenu("Edit");
        JMenuItem addEventItem = createStyledMenuItem("Add Event");
        addEventItem.addActionListener(e -> manageEventsDialog());
        JMenuItem deleteEventItem = createStyledMenuItem("Delete Event");
        deleteEventItem.addActionListener(e -> deleteSelectedEvent());
        editMenu.add(addEventItem);
        editMenu.add(deleteEventItem);

        // Help menu
        JMenu helpMenu = createStyledMenu("Help");
        JMenuItem aboutItem = createStyledMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_COLOR);
        menu.setFont(new Font("SansSerif", Font.BOLD, 14));
        menu.setBackground(PRIMARY_COLOR);
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setBackground(DIALOG_BACKGROUND);
        menuItem.setForeground(Color.BLACK);
        menuItem.setFont(new Font("SansSerif", Font.PLAIN, 14));
        menuItem.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return menuItem;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2), BorderFactory.createEmptyBorder(8, 20, 8, 20)));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
                button.setForeground(TEXT_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(TEXT_COLOR);
            }
        });

        return button;
    }

    private void refreshEventTable() {
        tableModel.setEvents(app.getAllEvents());
    }

    private void saveEvents() {
        app.saveEvents();
        JOptionPane.showMessageDialog(this, "Events saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Event Management System\nVersion 2.0\n\n By Kaustav, Rishita & Neha", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void manageEventsDialog() {
        // Create a styled panel for the dialog
        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setBackground(DIALOG_BACKGROUND);
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.setBackground(DIALOG_BACKGROUND);

        JButton createButton = createStyledButton("Create");
        JButton deleteButton = createStyledButton("Delete");
        JButton cancelButton = createStyledButton("Cancel");

        buttonPanel.add(createButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        JLabel titleLabel = new JLabel("Select an action:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);

        dialogPanel.add(titleLabel, BorderLayout.NORTH);
        dialogPanel.add(buttonPanel, BorderLayout.CENTER);

        // Create custom dialog
        JDialog dialog = new JDialog(this, "Event Management", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(dialogPanel, BorderLayout.CENTER);
        dialog.setSize(450, 150);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(DIALOG_BACKGROUND);

        // Button actions
        createButton.addActionListener(e -> {
            dialog.dispose();
            createEventDialog();
        });

        deleteButton.addActionListener(e -> {
            dialog.dispose();
            deleteSelectedEvent();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void createEventDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(DIALOG_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField titleField = createStyledTextField();
        JTextField dateField = createStyledTextField();
        JTextField locationField = createStyledTextField();
        JTextField typeField = createStyledTextField();

        panel.add(createStyledLabel("Title:"));
        panel.add(titleField);
        panel.add(createStyledLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(createStyledLabel("Location:"));
        panel.add(locationField);
        panel.add(createStyledLabel("Type:"));
        panel.add(typeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            if (!app.isValidEvent(titleField.getText())) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!app.isValidDate(dateField.getText())) {
                JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            app.createEvent(titleField.getText(), dateField.getText(), locationField.getText(), typeField.getText());
            refreshEventTable();
            outputArea.setText(app.getEventDetailsDisplay());
        }
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return field;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(PRIMARY_COLOR);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        return label;
    }

    private void deleteSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = eventTable.convertRowIndexToModel(selectedRow);
            Event event = app.getAllEvents().get(modelRow);

            int confirm = JOptionPane.showConfirmDialog(this, "Delete event: " + event.getTitle() + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                app.deleteEvent(event.getTitle());
                refreshEventTable();
                outputArea.setText(app.getEventDetailsDisplay());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void manageAttendeesDialog() {
        if (!checkAdminAccess()) {
            JOptionPane.showMessageDialog(this, "Admin access required", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events available. Create an event first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create styled combo box for event selection
        JComboBox<String> eventCombo = new JComboBox<>(app.getEventTitles());
        styleComboBox(eventCombo);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        selectionPanel.setBackground(DIALOG_BACKGROUND);
        selectionPanel.add(createStyledLabel("Select an event:"));
        selectionPanel.add(eventCombo);

        int selectionResult = JOptionPane.showConfirmDialog(this, selectionPanel, "Manage Attendees", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (selectionResult == JOptionPane.OK_OPTION) {
            String selectedEvent = (String) eventCombo.getSelectedItem();

            // Create action buttons panel
            JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            actionPanel.setBackground(DIALOG_BACKGROUND);

            JButton addButton = createStyledButton("Add ");
            JButton removeButton = createStyledButton("Remove");
            JButton cancelButton = createStyledButton("Cancel");

            actionPanel.add(addButton);
            actionPanel.add(removeButton);
            actionPanel.add(cancelButton);

            JLabel titleLabel = new JLabel("Attendee Management for " + selectedEvent, SwingConstants.CENTER);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setForeground(PRIMARY_COLOR);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBackground(DIALOG_BACKGROUND);
            dialogPanel.add(titleLabel, BorderLayout.NORTH);
            dialogPanel.add(actionPanel, BorderLayout.CENTER);
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // Create custom dialog
            JDialog dialog = new JDialog(this, "Manage Attendees", true);
            dialog.setLayout(new BorderLayout());
            dialog.add(dialogPanel, BorderLayout.CENTER);
            dialog.setSize(500, 150);
            dialog.setLocationRelativeTo(this);
            dialog.getContentPane().setBackground(DIALOG_BACKGROUND);

            // Button actions
            addButton.addActionListener(e -> {
                dialog.dispose();
                addAttendeeDialog(selectedEvent);
            });

            removeButton.addActionListener(e -> {
                dialog.dispose();
                removeAttendeeDialog(selectedEvent);
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            dialog.setVisible(true);
        }
    }

    private void addAttendeeDialog(String eventTitle) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(DIALOG_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = createStyledTextField();
        JTextField emailField = createStyledTextField();

        panel.add(createStyledLabel("Name:"));
        panel.add(nameField);
        panel.add(createStyledLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Attendee to " + eventTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            if (!app.isValidEmail(emailField.getText())) {
                JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = app.registerAttendee(eventTitle, nameField.getText(), emailField.getText());

            if (success) {
                refreshEventTable();
                outputArea.setText(app.getEventDetailsDisplay());
            }
        }
    }

    private void removeAttendeeDialog(String eventTitle) {
        List<Attendee> attendees = app.getEventAttendees(eventTitle);
        if (attendees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No attendees to remove.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] attendeeNames = attendees.stream().map(a -> a.getName() + " <" + a.getEmail() + ">").toArray(String[]::new);

        // Create styled combo box for attendee selection
        JComboBox<String> attendeeCombo = new JComboBox<>(attendeeNames);
        styleComboBox(attendeeCombo);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DIALOG_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(createStyledLabel("Select attendee to remove:"), BorderLayout.NORTH);
        panel.add(attendeeCombo, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Remove Attendee from " + eventTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            int index = attendeeCombo.getSelectedIndex();
            boolean success = app.removeAttendee(eventTitle, index);
            if (success) {
                refreshEventTable();
                outputArea.setText(app.getEventDetailsDisplay());
            }
        }
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    private void viewAttendeesDialog() {
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events available.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create styled combo box for event selection
        JComboBox<String> eventCombo = new JComboBox<>(app.getEventTitles());
        styleComboBox(eventCombo);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        selectionPanel.setBackground(DIALOG_BACKGROUND);
        selectionPanel.add(createStyledLabel("Select an event:"));
        selectionPanel.add(eventCombo);

        int result = JOptionPane.showConfirmDialog(this, selectionPanel, "View Attendees", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            String selectedEvent = (String) eventCombo.getSelectedItem();
            List<Attendee> attendees = app.getEventAttendees(selectedEvent);

            if (attendees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No attendees for this event.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create table model
            String[] columnNames = {"Name", "Email"};
            Object[][] data = attendees.stream().map(a -> new Object[]{a.getName(), a.getEmail()}).toArray(Object[][]::new);

            JTable attendeeTable = new JTable(data, columnNames);
            attendeeTable.setBackground(TABLE_ROW_COLOR);
            attendeeTable.setForeground(Color.BLACK);
            attendeeTable.setSelectionBackground(PRIMARY_COLOR);
            attendeeTable.setSelectionForeground(TEXT_COLOR);
            attendeeTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
            attendeeTable.setRowHeight(25);

            // Style table header
            JTableHeader attendeeHeader = attendeeTable.getTableHeader();
            attendeeHeader.setBackground(TABLE_HEADER_COLOR);
            attendeeHeader.setForeground(TEXT_COLOR);
            attendeeHeader.setFont(new Font("SansSerif", Font.BOLD, 14));

            JScrollPane scrollPane = new JScrollPane(attendeeTable);
            scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
            attendeeTable.setFillsViewportHeight(true);

            JOptionPane.showMessageDialog(this, scrollPane, "Attendees for " + selectedEvent, JOptionPane.PLAIN_MESSAGE, null);
        }
    }

    private void filterByTypeDialog() {
        if (app.getAllEvents().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events available to filter.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> types = app.getAllEvents().stream().map(Event::getType).distinct().toList();

        // Create styled combo box for type selection
        JComboBox<String> typeCombo = new JComboBox<>(types.toArray(new String[0]));
        styleComboBox(typeCombo);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        selectionPanel.setBackground(DIALOG_BACKGROUND);
        selectionPanel.add(createStyledLabel("Select event type:"));
        selectionPanel.add(typeCombo);

        int result = JOptionPane.showConfirmDialog(this, selectionPanel, "Filter by Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if (result == JOptionPane.OK_OPTION) {
            String selectedType = (String) typeCombo.getSelectedItem();
            List<Event> filteredEvents = app.findEventsByType(selectedType);

            if (filteredEvents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No events of this type.", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            EventTableModel filteredModel = new EventTableModel(filteredEvents);
            JTable filteredTable = new JTable(filteredModel) {
                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    if (!isRowSelected(row)) {
                        c.setBackground(row % 2 == 0 ? TABLE_ROW_COLOR : TABLE_ALT_ROW_COLOR);
                    }
                    return c;
                }
            };

            filteredTable.setBackground(TABLE_ROW_COLOR);
            filteredTable.setForeground(Color.BLACK);
            filteredTable.setSelectionBackground(PRIMARY_COLOR);
            filteredTable.setSelectionForeground(TEXT_COLOR);
            filteredTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
            filteredTable.setRowHeight(25);

            // Style table header
            JTableHeader filteredHeader = filteredTable.getTableHeader();
            filteredHeader.setBackground(TABLE_HEADER_COLOR);
            filteredHeader.setForeground(TEXT_COLOR);
            filteredHeader.setFont(new Font("SansSerif", Font.BOLD, 14));

            JScrollPane scrollPane = new JScrollPane(filteredTable);
            scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
            filteredTable.setFillsViewportHeight(true);

            JOptionPane.showMessageDialog(this, scrollPane, "Events of type: " + selectedType, JOptionPane.PLAIN_MESSAGE, null);
        }
    }

    private boolean checkAdminAccess() {
        return true;
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Manage Events" -> manageEventsDialog();
                case "Manage Attendees" -> manageAttendeesDialog();
                case "View All Events" -> outputArea.setText(app.getEventDetailsDisplay());
                case "View Event Attendees" -> viewAttendeesDialog();
                case "Filter by Type" -> filterByTypeDialog();
                case "Save Events" -> saveEvents();
                case "Exit" -> System.exit(0);
            }
        }
    }


}